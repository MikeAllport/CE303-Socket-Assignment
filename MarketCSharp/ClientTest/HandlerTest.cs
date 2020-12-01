using System;
using System.Collections.Generic;
using System.Text;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.Net;
using System.Net.Sockets;
using System.IO;
using Client;
using Utils;
using System.Threading;

namespace ClientTest
{
    [TestClass]
    public class HandlerTest
    {
        // stub class used to allow testing through connection from client and sending messages
        private class ServerStub : IDisposable
        {
            private StreamWriter writer;
            private StreamReader reader;
            public bool running = true;

            public ServerStub()
            {
            }

            public void initSocket(TcpClient tcpClient)
            {
                Stream stream = tcpClient.GetStream();
                writer = new StreamWriter(stream);
                reader = new StreamReader(stream);
                SendMessage(Message.connectSuccessful());
            }

            public static void Run(object param)
            {
                var server = (ServerStub)param;
                TcpListener listener = new TcpListener(IPAddress.Loopback, Program.PORT);
                listener.Start();
                TcpClient tcpClient = listener.AcceptTcpClient();
                listener.Stop();
                server.initSocket(tcpClient);
            }

            public string GetLine()
            {
                return reader.ReadLine();
            }
            public void SendMessage(string message)
            {
                writer.WriteLine(message);
                writer.Flush();
            }

            public void Dispose()
            {
                if (reader != null)
                    reader.Close();
                if (writer != null)
                    writer.Close();
            }
        }

        Handler handler;
        ServerStub stub;
        Thread serverThread;

        [TestInitialize]
        public void init()
        {
            if (serverThread != null)
            {
                serverThread.Interrupt();
                stub.Dispose();
                handler.Dispose();
            }
            stub = new ServerStub();
            serverThread = new Thread(ServerStub.Run);
            serverThread.Start(stub);
            new Thread(Program.Run).Start();
            Thread.Sleep(300);
            handler = Program.Handler;
        }

        [TestCleanup]
        public void clean()
        {
            stub.running = false;
        }
        [TestMethod]
        public void TestProcessLineNonEnumError()
        {
            Console.WriteLine("HEY");
            Assert.AreEqual(MessageEnums.ERROR, Program.Handler.ProcessLine("sup"));
        }

        //CONNECT_SUCC
        [TestMethod]
        public void TestProcessLineCONN_SUCC()
        {
            Assert.AreEqual(MessageEnums.CONNECT_SUCC, Program.Handler.ProcessLine(MessageEnums.CONNECT_SUCC.ToString()));
        }

        // TRADER_NEW
        [TestMethod]
        public void TestNewTraderFromHander()
        {
            Assert.AreEqual(MessageEnums.TRADER_NEW.ToString(), stub.GetLine().Trim(' '));
        }

        [TestMethod]
        public void TestHandlerCreatesTraderFromResponse()
        {
            Trader trader = new Trader("Trader0");
            stub.SendMessage(Message.traderIDBroadCast(trader.ID));
            Thread.Sleep(300);
            Trader current = handler.Trader;
            Assert.AreEqual(trader, handler.Trader);
            Assert.IsTrue(Program.Market.HasTrader(trader));
        }

        //TRADER_LIST
        [TestMethod]
        public void TestHandlerAddsAllTraders()
        {
            string traders = "Trader0 Trader1 Trader2 Trader3";
            stub.SendMessage(Message.traderIDBroadCast("Trader0"));
            stub.SendMessage(Message.allTradersBroadCast(traders));
            Thread.Sleep(300);
            Assert.IsTrue(Program.Market.HasTrader(new Trader("Trader0")));
            Assert.IsTrue(Program.Market.HasTrader(new Trader("Trader1")));
            Assert.IsTrue(Program.Market.HasTrader(new Trader("Trader2")));
            Assert.IsTrue(Program.Market.HasTrader(new Trader("Trader3")));
        }

        //TRADER_WITH_STOCK
        [TestMethod]
        public void TestHandlerSetsStockHolder()
        {
            string traderWithStock = "Trader1";
            stub.SendMessage(Message.traderWithStockBroadCast(traderWithStock));
            Thread.Sleep(300);
            Assert.AreEqual(new Trader(traderWithStock), Program.Market.StockHolder);
        }

        //TRADER_JOINED
        [TestMethod]
        public void TestHandlerAllocatedNewTrader()
        {
            string newTrader = "Trader50";
            stub.SendMessage(Message.traderJoincedBroadCast(newTrader));
            Thread.Sleep(1000);
            Assert.IsTrue(Program.Market.HasTrader(new Trader(newTrader)));
        }

        //TRADER_LEFT
        [TestMethod]
        public void TestHandlerRemovedTrader()
        {
            Trader trader1 = new Trader("Trader1");
            stub.SendMessage(Message.traderJoincedBroadCast(trader1.ID));
            Thread.Sleep(500);
            Assert.IsTrue(Program.Market.HasTrader(trader1));
            stub.SendMessage(Message.traderLeftBroadCast(trader1.ID));
            Thread.Sleep(300);
            Assert.IsFalse(Program.Market.HasTrader(trader1));
        }

        //TRADE_SUCC
        [TestMethod]
        public void TestTraderTradedWithOther()
        {
            Trader trader1 = new Trader("t1");
            Trader trader2 = new Trader("t2");
            stub.SendMessage(Message.tradeBroadCast(trader1.ID, trader2.ID));
            Thread.Sleep(300);
            Assert.AreEqual(trader2, Program.Market.StockHolder);
        }

        //TRADER_ACQ_STOCK
        [TestMethod]
        public void TestTraderGivenStockAutomatically()
        {
            Trader trader1 = new Trader("t");
            stub.SendMessage(Message.traderAcqBroadCast(trader1.ID));
            Thread.Sleep(300);
            Assert.AreEqual(trader1, Program.Market.StockHolder);
        }
    }
}
