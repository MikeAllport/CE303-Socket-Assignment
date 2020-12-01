using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Utils;

class MessageTest
{
    [TestClass]
    public class UtilsMessageTest
    {
        [TestMethod]
        public void testAllTradersBroadcast()
        {
            String result = Message.allTradersBroadCast("test");
            Assert.IsTrue(result.Equals("TRADER_LIST test"));
        }

        [TestMethod]
        public void testTraderJoinedBroadcast()
        {
            String result = Message.traderJoincedBroadCast("test");
            Assert.IsTrue(result.Equals("TRADER_JOINED test"));
        }

        [TestMethod]
        public void TestIsEnum()
        {
            Assert.IsFalse(Message.IsMessageEnum(""));
            Assert.IsFalse(Message.IsMessageEnum("nope"));
            Assert.IsTrue(Message.IsMessageEnum(Message.connectSuccessful()));
        }

        [TestMethod]
        public void TestGetEnum()
        {
            Assert.AreEqual(MessageEnums.CONNECT_SUCC, Message.GetMessageEnumFromString(Message.connectSuccessful()));
        }
    }
}