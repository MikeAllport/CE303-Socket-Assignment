using System;
using System.Collections.Generic;
using System.Text;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Utils;

namespace UtilsTest
{
    [TestClass]
    public class PairTest
    {
        [TestMethod]
        public void TestPairConstruction()
        {
            string first = "testfirst";
            var second = new List<string>(){ "1", "2"};
            var pair = new Pair<string, List<string>>(first, second);
            Assert.AreEqual(first, pair.First);
            Assert.AreEqual(second, pair.Second);
        }
    }
}
