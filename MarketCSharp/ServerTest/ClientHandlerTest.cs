using System;
using System.Collections.Generic;
using System.Text;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.IO;
using System.Net.Sockets;
using System.Threading;
using Utils;
using Server;
using System.Diagnostics;
using System.ComponentModel;
using System.Windows;

namespace ServerTest
{
    [TestClass]
    public class ClientHandlerTest: ClientHandler
    {
        // stub class for instantiating client connection and providing methods to 
        // send/recieve message
        private class ClientStub
        {
            private readonly int PORT = 8888;
            private StreamReader reader;
            private StreamWriter writer;
            public ClientStub()
            {
                TcpClient tcpClient = new TcpClient("localhost", PORT);
                NetworkStream stream = tcpClient.GetStream();
                reader = new StreamReader(stream);
                writer = new StreamWriter(stream);
            }

            public void SendMessage(string message)
            {
                writer.WriteLine(message);
                writer.Flush();
            }

            public string GetMessage()
            {
                return reader.ReadLine();
            }
        }

        ClientStub clientStub;
        Thread thread;

        [TestInitialize]
        public void init()
        {
            thread = new Thread(Program.RunServer);
            thread.Start();
            clientStub = new ClientStub();
        }

        [TestCleanup]
        public void clean()
        {
            if (!(thread == null))
                thread.Interrupt();
            Market.ClearMarket();
        }

        [TestMethod]
        public void TestASuccesfullConnection()
        {
            Assert.AreEqual(Message.connectSuccessful(), clientStub.GetMessage());
        }

        // INVALID MESSAGES
        [TestMethod]
        public void TestAnUnSuccessfullMessageEnum()
        {
            clientStub.GetMessage();
            string invalidMessageString = "";
            clientStub.SendMessage(invalidMessageString);
            string[] response = clientStub.GetMessage().Split();
            Assert.AreEqual("ERROR", response[0].ToUpper());
            Assert.AreEqual("INCORRECT", response[1].ToUpper());
            invalidMessageString = "I am invalid";
            clientStub.SendMessage(invalidMessageString);
            response = clientStub.GetMessage().Split();
            Assert.AreEqual("ERROR", response[0].ToUpper());
            Assert.AreEqual("INCORRECT", response[1].ToUpper());
        }

        //TRADER_NEW
        [TestMethod]
        public void TestConnectedNewTraderInitialized()
        {
            Trader traderWithMessages = Market.GetNewTrader().Second;
            clientStub.GetMessage();
            clientStub.SendMessage(MessageEnums.TRADER_NEW.ToString());
            Thread.Sleep(500);
            Assert.IsTrue(traderWithMessages.GetMessages().Contains(Message.traderJoincedBroadCast("Trader1")));
        }

        //TRADER_RECONNECTING
        [TestMethod]
        public void TestReconnectingTraderFail()
        {
            clientStub.GetMessage();
            clientStub.SendMessage(Message.traderReconnectingBroadcast("Trader1"));
            Assert.IsTrue(clientStub.GetMessage().Contains(MessageEnums.ERROR.ToString()));
        }

        [TestMethod]
        public void TestReconnectingTraderSuccess()
        {
            clientStub.GetMessage();
            Server.Program.IsRestarting = true;
            clientStub.SendMessage(Message.traderReconnectingBroadcast("Trader1"));
            Assert.IsTrue(clientStub.GetMessage().Contains(MessageEnums.TRADER_ID.ToString()));
        }

        //TRADER_TRADE
        [TestMethod]
        public void TradeWithOtherSuccess()
        {
            clientStub.SendMessage(Message.newTraderBroadcast());
            Thread.Sleep(100);
            Trader traderWithMessages = Market.GetNewTrader().Second;
            clientStub.SendMessage(Message.makeTrade(traderWithMessages.ID));
            Thread.Sleep(100);
            Assert.IsTrue(traderWithMessages.GetMessages().Contains(Message.tradeBroadCast("Trader0", traderWithMessages.ID)));
        }

        // this test takes a while
        [TestMethod]
        public void Test30Connections()
        {
            for (var i = 0; i < 30; ++i)
                using (Process myProcess = new Process())
            {
                string path = Path.GetFullPath(Path.Combine(AppContext.BaseDirectory, "..\\..\\..\\..\\Client\\bin\\Debug\\netcoreapp3.0\\"));
                myProcess.StartInfo.UseShellExecute = true;
                myProcess.StartInfo.FileName = path + "Client.exe";
                myProcess.StartInfo.CreateNoWindow = true;
                myProcess.Start();
            }
            Thread.Sleep(20000);
            Assert.AreEqual(30, Market.TraderListCopy.Count);
        }
    }
}
