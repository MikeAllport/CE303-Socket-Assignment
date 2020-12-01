using System;
using System.Collections.Generic;
using System.Text;

namespace Client
{
    /**
     * Markets purpose is to hold the current state of the market with all
     * the traders and the stockholder. This class also stores messages related
     * to tyhe markets state, i.e trader joined, left etc, for the UI to display
     */
    public class Market
    {
        private SortedSet<Trader> _traderList;
        private object traderListLock;
        private List<string> _uiMessageList;
        public List<string> UiMessageList { get { return GetUIMessageListCopy(); } }
        private object uiMessageListLock;
        public SortedSet<Trader> TradersListCopy { get { return GetTradersCopy(); } }
        private Trader _StockHolder;
        public Trader StockHolder { get { return _StockHolder; } set { 
                _StockHolder = value; } }

        public Market()
        {
            _traderList = new SortedSet<Trader>();
            _StockHolder = null;
            traderListLock = new object();
            _uiMessageList = new List<string>();
            uiMessageListLock = new object();
        }
        public void AddTrader(Trader trader)
        {
            lock (traderListLock)
            {
                _traderList.Add(trader);
            }
        }

        public bool RemoveTrader(Trader trader)
        {
            lock (traderListLock)
            {
                return _traderList.Remove(trader);
            }
        }

        public bool HasTrader(Trader trader)
        {
            lock (traderListLock)
            {
                return _traderList.Contains(trader);
            }
        }
        public void PrintTraders()
        {
            lock (traderListLock)
            {
                string traders = "TraderList:\n";
                foreach (var trader in _traderList)
                {
                    traders += trader.ID;
                    if (Equals(trader, StockHolder))
                        traders += " STOCK HOLDER";
                    traders += "\n";
                }
                traders = traders.Substring(0, traders.Length - 1);
                traders += "ListEnd";
                Console.WriteLine(traders);
            }
        }

        private SortedSet<Trader> GetTradersCopy()
        {
            SortedSet<Trader> newset = new SortedSet<Trader>();
            lock (traderListLock)
            {
                foreach (Trader trader in _traderList)
                    newset.Add(trader);
            }
            return newset;
        }

        private List<string> GetUIMessageListCopy()
        {
            var messageListCopy = new List<string>();
            lock(uiMessageListLock)
            {
                foreach (var message in _uiMessageList)
                    messageListCopy.Add(message);
            }
            return messageListCopy;
        }

        public void AddUIMessage(string message)
        {
            lock (uiMessageListLock)
            {
                _uiMessageList.Add(message);
            }
        }

        public void ClearMarket()
        {
            StockHolder = null;
            lock (traderListLock)
            {
                _traderList = new SortedSet<Trader>();
            }
        }
    }
}
