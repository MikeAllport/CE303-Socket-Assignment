using System;
using System.IO;
using System.Net.Sockets;
using Utils;
using System.Threading;
using static Utils.MessageEnums;

namespace Client
{
    /**
     * Handler's purpose is to contain the main logic for the server protocol,
     * instantiate a listener thread for check for messages, instantiate the socket itself
     * and to instantate the trader;
     */
    public class Handler: IDisposable
    {
        private StreamWriter writer;
        private StreamReader _reader;
        private readonly Market market;
        public StreamReader Reader { get { return _reader; } }
        // the clients trader
        protected static Trader _trader = null;
        public Trader Trader { get { return _trader; } }
        private static TcpClient tcpClient = null;
        // used to determine whether ui should wait to close connection due to a delay
        // in server restarting - issue when closing connection during this delay
        private static bool _serverReconnecting = false;
        public static bool ServerReconnecting { get { return _serverReconnecting; } set { _serverReconnecting = value; } }

        public Handler(Market market)
        {
            this.market = market;
            tcpClient = new TcpClient(Program.SERVER_ADDRESS, Program.PORT);
            NetworkStream stream = tcpClient.GetStream();
            this.writer = new StreamWriter(stream);
            this._reader = new StreamReader(stream);
            writer.AutoFlush = true;
            new Thread(new SocketListener(this).Run).Start();
        }

        // sends a string to the socket streamwriter if connected
        public void SendMessage(string message)
        {
            try
            {
                if (_serverReconnecting && Equals(message.Split(" ")[0], TRADER_TRADE.ToString()))
                    // disallows trade messages being send during restart
                    return;
                writer.WriteLine(message);
            } catch (Exception e) { Console.WriteLine("Not connected to server"); }
        }

        // main logic for processing a message from the server utilizes MessageEnums
        public MessageEnums ProcessLine(string line)
        {
            string[] splitLine = line.Split(" ");
            if (!(splitLine.Length > 0) || !(Message.IsMessageEnum(splitLine[0])))
            {
                return MessageEnums.ERROR;
            }
            try
            {
                MessageEnums messageType = Message.GetMessageEnumFromString(splitLine[0]);
                switch(messageType)
                {
                    case CONNECT_SUCC:
                        {
                            SendMessage(Message.newTraderBroadcast());
                            return CONNECT_SUCC;
                        }
                    case SERVER_REBOOT:
                        {
                            if (_trader == null)
                                SendMessage(Message.newTraderBroadcast());
                            else
                                SendMessage(Message.traderReconnectingBroadcast(_trader.ID));
                            market.AddUIMessage(Message.serverRebookUI());
                            _serverReconnecting = true;
                            return SERVER_REBOOT;
                        }
                    case SERVER_RESTORED:
                        {
                            market.AddUIMessage(Message.serverRecoveredUI());
                            _serverReconnecting = false;
                            return SERVER_RESTORED;
                        }
                    case TRADER_ID:
                        {
                            InitTrader(splitLine[1]);
                            Console.WriteLine($"Your ID is {_trader.ID}");
                            market.AddUIMessage($"Your ID is {_trader.ID}");
                            return TRADER_ID;
                        }
                    case TRADER_LIST:
                        {
                            InitTraderList(splitLine);
                            return TRADER_LIST;
                        }
                    case TRADER_WITH_STOCK:
                        {
                            Program.Market.StockHolder = new Trader(splitLine[1]);
                            Program.Market.PrintTraders();
                            return TRADER_WITH_STOCK;
                        }
                    case TRADER_JOINED:
                        {
                            Program.Market.AddTrader(new Trader(splitLine[1]));
                            if (Trader != null && !Equals(Trader.ID, splitLine[1]))
                            {
                                Console.WriteLine(Message.traderJoinedUI(splitLine[1]));
                                Program.Market.AddUIMessage(Message.traderJoinedUI(splitLine[1]));
                                Program.Market.PrintTraders();
                            }
                            return TRADER_JOINED;
                        }
                    case TRADER_LEFT:
                        {
                            Program.Market.RemoveTrader(new Trader(splitLine[1]));
                            Console.WriteLine(Message.traderLeftUI(splitLine[1]));
                            Program.Market.AddUIMessage(Message.traderLeftUI(splitLine[1]));
                            Program.Market.PrintTraders();
                            return TRADER_LEFT;
                        }
                    case TRADE_SUCC:
                        {
                            Program.Market.StockHolder = new Trader(splitLine[2]);
                            Console.WriteLine(Message.tradeUI(splitLine[1], splitLine[2]));
                            Program.Market.AddUIMessage(Message.tradeUI(splitLine[1], splitLine[2]));
                            return TRADER_TRADE;
                        }
                    case TRADER_ACQ_STOCK:
                        {
                            Program.Market.StockHolder = new Trader(splitLine[1]);
                            Console.WriteLine(Message.traderAcqUI(splitLine[1]));
                            Program.Market.AddUIMessage(Message.traderAcqUI(splitLine[1]));
                            return TRADER_ACQ_STOCK;
                        }
                    default:
                        {
                            throw new IndexOutOfRangeException();
                        }
                }
            } catch (IndexOutOfRangeException e)
            {
                Program.Market.AddUIMessage(Message.error($"Incorrect command '{line}'"));
                return MessageEnums.ERROR;
            }
        }

        private void InitTrader(string traderID)
        {
            _trader = new Trader(traderID);
            Program.Market.AddTrader(_trader);
        }

        private void InitTraderList(string[] traderAndPrefix)
        {
            for (int i = 1; i < traderAndPrefix.Length; ++i)
            {
                if (traderAndPrefix[i] != "")
                    Program.Market.AddTrader(new Trader(traderAndPrefix[i]));
            }
        }

        public void Dispose()
        {
            if (writer != null)
                writer.Close();
            if (_reader != null)
                Reader.Close();
            if (tcpClient != null)
                tcpClient.Close();
        }
    }
}
