using Microsoft.VisualStudio.TestTools.UnitTesting;
using Client;

namespace ClientTest
{
    [TestClass]
    public class TraderTest
    {
        [TestMethod]
        public void TestMakeTrader()
        {
            Trader trader = new Trader("Trader0");
            Assert.AreEqual("Trader0", trader.ID);
        }

        [TestMethod]
        public void TestEquality()
        {
            Trader t1 = new Trader("Trader0");
            Trader t2 = new Trader("Trader0");
            Assert.AreEqual(t1, t2);
        }

        [TestMethod]
        public void TestAssignTraderID()
        {
            Trader trader = new Trader();
            Assert.AreEqual("", trader.ID);
            trader.ID = "Trader1";
            Assert.AreEqual("Trader1", trader.ID);
        }

        [TestMethod]
        public void TestCompareTo()
        {
            Trader t1 = new Trader("Trader0");
            Trader t2 = new Trader("Trader1");
            Assert.IsTrue(t1.CompareTo(t2) < 0);
        }
    }
}
