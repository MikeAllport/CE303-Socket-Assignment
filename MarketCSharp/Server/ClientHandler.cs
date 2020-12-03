using System;
using System.IO;
using System.Net.Sockets;
using System.Threading;
using Utils;
using System.Collections.Generic;
using static Utils.MessageEnums;

namespace Server
{
    /**
     * ClientHandler's purpose is to hold all business logic for a singular connected
     * client socket, including data members for sending/receiving data, the trader
     * associated with this thread, and initializing a seperate thread with the responsibility
     * of checking a traders outbox for messages from other client threads.
     */
    public class ClientHandler: IDisposable
    {
        private Trader _trader;
        public ref Trader Trader { get { return ref _trader; } }
        private StreamWriter writer;
        private StreamReader reader;
        private TcpClient tcpClient;

        public ClientHandler()
        {
            _trader = null;
        }

        // main initiation logic called from Thread().Start()
        public void HandleIncomingMessages(object param)
        {
            tcpClient = (TcpClient)param;           
            try
            {
                Stream stream = tcpClient.GetStream();
                writer = new StreamWriter(stream);
                writer.AutoFlush = true;
                reader = new StreamReader(stream);
                SendWelcomeMessage();
                StartListening();
            }
            catch (Exception e)
            {
                CloseConnection();
            }
        }

        private void SendWelcomeMessage()
        {
            if (Program.IsRestarting)
                SendMessage(Message.serverRebootBroadcast());
            else
                SendMessage(Message.connectSuccessful());
        }

        // sends string message to socket writer
        public void SendMessage(string message)
        {
            writer.WriteLine(message);
        }

        // main listen loop to get lines from socket reader
        protected void StartListening()
        {
            while(true)
            {
                string line = reader.ReadLine();
                if (line == null)
                {
                    throw new Exception();
                }
                ProcessLine(line);
            }
        }

        protected void CloseConnection()
        {
            if (Trader != null)
                Trader.Reconnected = false;
            Market.KillTrader(Trader);
            try
            {
                Dispose();
            }
            catch (Exception) { }
        }

        // main logic for processing messages received from client utilizing MessageEnums
        protected MessageEnums ProcessLine(string line)
        {
            string[] splitLine = line.Split(' ');
            if (splitLine.Length <= 0 || !Message.IsMessageEnum(splitLine[0]))
            {
                SendMessage(Message.error($"Incorrect message protocol '{line}'"));
                return ERROR;
            }
            try
            {
                MessageEnums responseType = Message.GetMessageEnumFromString(splitLine[0]);
                switch(responseType)
                {
                    case TRADER_NEW:
                        InitNewTrader();
                        return TRADER_NEW;
                    case TRADER_RECONNECTING:
                        if (!Program.IsRestarting)
                            throw new ServerNotRestartingException("Server is not restarting");
                        InitNewTrader(splitLine[1]);
                        return TRADER_RECONNECTING;
                    case TRADER_TRADE:
                        if (_trader == null)
                            throw new Exception("Trader Uninitialized");
                        if (Program.IsRestarting)
                            throw new Exception("Cannot trade whilst server is recovering");
                        return TradeStock(splitLine[1]);
                    default:
                        SendMessage(Message.error($"Invalid request '{line}'"));
                        return ERROR;
                }
            }
            catch (IndexOutOfRangeException e) {
                SendMessage(Message.error($"Inavlid request '{line}' unexpected number of arguments"));
                return ERROR;
            }
            catch (ServerNotRestartingException e)
            {
                SendMessage(Message.error($"{e.Message}"));
                return ERROR;
            }
        }

        // gets new trader and calls method to send messages
        private void InitNewTrader()
        {
            var tradersListAndNewTrader = Market.GetNewTrader();
            AssignTraderSendWelcome(tradersListAndNewTrader);
        }

        // gets reconecting trader, or new if ID not recognised, and calls method to send messages
        private void InitNewTrader(string ID)
        {
            var tradersListAndNewTrader = Market.GetReconnectingTrader(ID);
            AssignTraderSendWelcome(tradersListAndNewTrader);
        }

        // assigns trader to class member and send the welcome string of messages whilst
        // starting the outbox thread
        private void AssignTraderSendWelcome(Pair<List<string>, Trader> tradersListAndNewTrader)
        {
            this._trader = tradersListAndNewTrader.Second;
            if (Program.IsRestarting)
                _trader.Reconnected = true;
            SendMessage(Message.traderIDBroadCast(_trader.ID));
            SendOtherTraderList(tradersListAndNewTrader.First);
            Trader stockHolder = Market.StockHolder;
            SendMessage(Message.traderWithStockBroadCast(stockHolder.ID));
            StartOutboxListener();
        }

        // extracts traders from input list and formats them to server protocol message
        private void SendOtherTraderList(List<string> traderList)
        {
            string allTraders = "";
            foreach (string traderID in traderList)
                allTraders += traderID + " ";
            SendMessage(Message.allTradersBroadCast(allTraders));
        }

        // starts a thread specificly for checking traders messages and sending them if exists
        // outbox could consist of traderleft, traderjoined, traded stock etc
        private void StartOutboxListener()
        {
            new Thread((object arg) =>
            {
                ClientHandler handler = (ClientHandler)arg;
                while (handler.Trader != null && handler.Trader.DeathIndicator == 0)
                {
                    var messages = handler.Trader.GetMessages();
                    foreach (var message in messages)
                    {
                        handler.SendMessage(message);
                    }
                    Thread.Sleep(100);
                }
            }).Start(this);
        }

        // trading stock logic
        private MessageEnums TradeStock(string otherTradersID)
        {
            if (!String.Equals(Market.StockHolder.ID, Trader.ID))
            {
                SendMessage(Message.tradeFailBroadCast("you do not have the stock to give away!"));
                return TRADE_FAIL;
            }
            return Market.MakeTrade(this, otherTradersID);
        }

        public void Dispose()
        {
            try
            {
                if (tcpClient != null)
                {
                    tcpClient.GetStream().Close();
                    tcpClient.Close();
                }
                if (writer != null)
                    writer.Close();
                if (reader != null)
                    reader.Close();
            }
            catch (Exception) { }
        }
    }
}