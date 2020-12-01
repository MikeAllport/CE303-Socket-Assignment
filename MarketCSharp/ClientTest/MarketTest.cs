using System;
using System.Collections.Generic;
using System.Text;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Client;
using System.Linq;

namespace ClientTest
{
    [TestClass]
    public class MarketTest
    {
        Market market;

        [TestInitialize]
        public void init()
        {
            market = new Market();
        }
        [TestMethod]
        public void TestInitialization()
        {
            Assert.AreEqual(null, market.StockHolder);
        }


        [TestMethod]
        public void TestSetStockHolder()
        {
            Trader t1 = new Trader();
            market.StockHolder = t1;
            Assert.AreEqual(t1, market.StockHolder);
        }

        [TestMethod]
        public void TestAddTrader()
        {
            Trader t1 = new Trader();
            market.AddTrader(t1);
            Assert.IsTrue(market.HasTrader(t1));
        }

        [TestMethod]
        public void TestRemoveTraderSucc()
        {
            Trader t1 = new Trader();
            market.AddTrader(t1);
            Assert.IsTrue(market.RemoveTrader(t1));
        }

        [TestMethod]
        public void TestRemoveTraderFail()
        {
            Assert.IsFalse(market.RemoveTrader(new Trader()));
        }

        [TestMethod]
        public void TestSortedSet()
        {
            Trader t1 = new Trader("Trader0");
            Trader t2 = new Trader("Trader1");
            Trader t3 = new Trader("Trader2");
            Market market = new Market();
            market.AddTrader(t3);
            market.AddTrader(t1);
            market.AddTrader(t2);
            var traderSet = market.TradersListCopy;
            Assert.AreEqual(t1, Enumerable.ElementAt(traderSet, 0));
            Assert.AreEqual(t2, Enumerable.ElementAt(traderSet, 1));
            Assert.AreEqual(t3, Enumerable.ElementAt(traderSet, 2));
        }

    }
}
