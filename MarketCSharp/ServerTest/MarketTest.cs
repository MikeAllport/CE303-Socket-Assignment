using Microsoft.VisualStudio.TestTools.UnitTesting;

using System;
using System.Collections.Generic;
using System.Text;
using Server;
using System.Threading;
using Utils;
using Newtonsoft.Json;
using System.Numerics;

namespace ServerTest
{
    [TestClass]
    public class MarketTest
    {
        Trader trader;

        [TestInitialize]
        public void ResetsMarket()
        {
            Market.ClearMarket();
        }

        [TestCleanup]
        public void clean()
        {
            Market.ClearMarket();
        }

        [TestMethod]
        public void TestTraderIdInitialValue()
        {
            trader = Market.GetNewTrader().Second;
            Assert.AreEqual("Trader0", trader.ID);
        }

        [TestMethod]
        public void TestTraderIdConcurrency()
        {
            var threads = new List<Thread>();
            var threadCount = 100;
            for (var i = 0; i < threadCount; ++i)
            {
                var thread = new Thread(() =>
                {
                    trader = Market.GetNewTrader().Second;
                });
                thread.Start();
                threads.Add(thread);
            }
            foreach (var thread in threads)
                thread.Join();
            Trader currentMostTrader = Market.GetNewTrader().Second;
            Assert.AreEqual("Trader" + threadCount, currentMostTrader.ID);
        }

        [TestMethod]
        public void TestBroadCastMessage()
        {
            string message = "some new message";
            Trader trader1 = Market.GetNewTrader().Second;
            Trader trader2 = Market.GetNewTrader().Second;
            Market.BroadCastMessage(message);
            Assert.AreEqual(message, trader1.GetMessages()[1]);
            Assert.AreEqual(message, trader2.GetMessages()[0]);
        }

        [TestMethod]
        public void TestKillTrader()
        {
            Trader trader = Market.GetNewTrader().Second;
            Assert.AreEqual(trader.ID, Market.StockHolder.ID);
            Market.KillTrader(trader);
            Assert.AreEqual(null, Market.StockHolder);
        }

        [TestMethod]
        public void TestClearMarket()
        {
            Trader trader = Market.GetNewTrader().Second;
            Thread.Sleep(100);
            Trader stockHolder = Market.StockHolder;
            Assert.AreEqual(trader, Market.StockHolder);
            Market.ClearMarket();
            Trader trader2 = Market.StockHolder;
            Assert.AreEqual(null, Market.StockHolder);
        }

        [TestMethod]
        public void TestStockHolder()
        {
            Assert.AreEqual(null, Market.StockHolder);
            Trader trader = Market.GetNewTrader().Second;
            Assert.AreEqual(trader, Market.StockHolder);
        }

        [TestMethod]
        public void TestSerialization()
        {
            Trader t1 = Market.GetNewTrader().Second;
            Trader t2 = Market.GetNewTrader().Second;
            Trader t3 = Market.GetNewTrader().Second;
            BigInteger currentID = Market.IDNumberCopy;
            string marketJson = Market.Serialize();

            // checks market status is correct
            Assert.AreEqual(t1, Market.StockHolder);
            Assert.IsTrue(Market.HasTrader(t1));
            Assert.IsTrue(Market.HasTrader(t2));
            Assert.IsTrue(Market.HasTrader(t3));

            // clears market and checks it is empty
            Market.ClearMarket();
            Assert.IsFalse(Market.HasTrader(t1));
            Assert.IsFalse(Market.HasTrader(t2));
            Assert.IsFalse(Market.HasTrader(t3));

            // restores market
            Market.Deserialize(marketJson);
            Assert.AreEqual(t1, Market.StockHolder);
            Assert.IsTrue(Market.HasTrader(t1));
            Assert.IsTrue(Market.HasTrader(t2));
            Assert.IsTrue(Market.HasTrader(t3));
            Assert.AreEqual(currentID, Market.IDNumberCopy);
        }

        [TestMethod]
        public void TestGetReconnectingTraderSuccess()
        {
            Trader t1 = Market.GetNewTrader().Second;
            Trader t2 = Market.GetNewTrader().Second;
            Assert.IsFalse(t1.Reconected);
            var t1ReconnectedAndList = Market.GetReconnectingTrader(t1.ID);
            Assert.AreEqual(t1, t1ReconnectedAndList.Second);
            Assert.IsTrue(t1.Reconected);
            Assert.IsTrue(t1ReconnectedAndList.First.Contains(t2.ID));
        }

        [TestMethod]
        public void TestGetReconnectedUnknownID()
        {
            Trader t1 = Market.GetNewTrader().Second;
            Trader t2 = Market.GetNewTrader().Second;
            var t1ReconnectedAndList = Market.GetReconnectingTrader("SomeID");
            Assert.AreNotEqual(t1, t1ReconnectedAndList.Second);
            Assert.AreNotEqual(t2, t1ReconnectedAndList.Second);
            Assert.IsTrue(t1ReconnectedAndList.First.Contains(t1.ID));
            Assert.IsTrue(t1ReconnectedAndList.First.Contains(t1.ID));
            Assert.IsTrue(t1ReconnectedAndList.Second.Reconected = true);
        }

        [TestMethod]
        public void TestKickingTraderNotReconnected()
        {
            Trader t1 = Market.GetNewTrader().Second;
            Trader t2 = Market.GetNewTrader().Second;
            Trader t3 = Market.GetNewTrader().Second;
            t1 = Market.GetReconnectingTrader(t1.ID).Second;
            t2 = Market.GetReconnectingTrader(t2.ID).Second;
            Assert.IsTrue(t1.Reconected);
            Assert.IsTrue(t2.Reconected);
            Assert.IsFalse(t3.Reconected);
            Market.KickDisconnectedTraders();
            Assert.IsTrue(Market.HasTrader(t1));
            Assert.IsTrue(Market.HasTrader(t2));
            Assert.IsFalse(Market.HasTrader(t3));
        }
    }
}
