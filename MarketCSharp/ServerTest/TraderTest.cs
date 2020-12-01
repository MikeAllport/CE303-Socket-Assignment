using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.Collections.Generic;
using System;
using Server;
using System.Threading;

namespace ServerTest
{
    [TestClass]
    public class TraderTest
    {
        static Trader trader;
        List<string> testMessages = new List<string>() { "msg1", "msg2", "msg3" };

        [TestInitialize]
        public void init()
        {
            trader = new Trader("1");
            foreach (var message in testMessages)
                trader.AddMessage(message);
        }

        [TestMethod]
        public void TestGetMessages()
        {
            List<string> traderOutbox = trader.GetMessages();
            foreach (var message in testMessages)
                Assert.IsTrue(traderOutbox.Contains(message));
        }

        [TestMethod]
        // tests for race conditions to outbox
        public void TestInboxConcurrency()
        {
            int threadCount = 100;
            var threadList = new List<Thread>();

            // init and start threads to add messages
            for (int i = 0; i < testMessages.Count * threadCount; i += testMessages.Count)
            {
                var thread = new Thread(() =>
                {
                   trader.AddMessage(testMessages.ToArray());
                });
                threadList.Add(thread);
                thread.Start();
            }

            // join threads
            foreach (var thread in threadList)
                thread.Join();

            // check messages in order
            var messages = trader.GetMessages();
            for (int i = 0; i < messages.Count; ++i)
            {
                Assert.AreEqual(testMessages[i % 3], messages[i % 3]);
            }
        }

        [TestMethod]
        public void TestKillingTrader()
        {
            Assert.IsFalse(trader.IsDead());
            trader.KillTrader();
            Assert.IsTrue(trader.IsDead());
        }

        [TestMethod]
        public void TestDeathIndicatorConcurrency()
        {
            var threads = new List<Thread>();
            var threadcount = 100;
            for(var i = 0; i < threadcount; ++i)
            {
                var thread = new Thread(() =>
                {
                   Thread.Sleep(100);
                   trader.KillTrader();
                });
                threads.Add(thread);
                thread.Start();
            }
            foreach (var thread in threads)
                thread.Join();
            Assert.IsTrue(trader.DeathIndicator == threadcount);
        }

        [TestMethod]
        public void TestEquality()
        {
            var trader = new Trader("Trader0");
            var trader2 = new Trader("Trader0");
            Assert.AreEqual(trader, trader2);
            var tradersSet = new HashSet<Trader>();
            tradersSet.Add(trader);
            tradersSet.Add(trader2);
            Assert.IsTrue(tradersSet.Count == 1);
        }
    }
}
