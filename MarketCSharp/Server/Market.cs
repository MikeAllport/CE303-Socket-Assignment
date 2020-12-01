using System;
using System.Collections.Generic;
using System.Numerics;
using Utils;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace Server
{
    /**
     * Market's purpose is to hold data members and logic for all traders currently connected.
     * Including a thread safe list of all traders, creating new traders, controlling the
     * unique unbound trader id, the current stock holder, and logic relating to removal
     * of traders and assigning the new stock holder
     * 
     * For the most part, this class is static. However, adding serialization/deserialization
     * requires an instance of the class made so cannot declare as static class. This class is 
     * fully serializable
     */
    [JsonConverter(typeof(Market))]
    public class Market: JsonConverter
    {
        // json attribute name constants
        private static readonly string JSON_TRADER_LIST_KEY = "Traders",
            JSON_CURRENT_ID = "CurrentID",
            JSON_STOCK_HOLDER = "StockHolder";
        // stock holder
        private static Trader _stockHolder = null;
        private static object stockHolderLock = new object();
        public static Trader StockHolder { 
            get 
            {
                lock (stockHolderLock)
                {
                    return _stockHolder;
                }
            } 
            set
            {
                lock (stockHolderLock)
                {
                    _stockHolder = value;
                }
            } 
        }
        // unique trader id, utilizes big integer - infinitely big number
        private static BigInteger _IDNumber = BigInteger.Zero;
        private static object IDLock = new object();
        public static BigInteger IDNumberCopy { 
            get
            {
                lock (IDLock)
                {
                    return BigInteger.Parse(_IDNumber.ToString());
                }
            } }
        // active traders list
        private static List<Trader> _traderList = new List<Trader>();
        private static object traderListLock = new object();
        public static List<Trader> TraderListCopy { get { return GetTraderListCopy(); } }
        // market messages, used for ui
        private static List<string> _marketMessages = new List<string>();
        public static List<string> MarketMessagesCopy { get { return GetMarketMessagesCopy(); } }
        private static object marketMessageListLock = new object();

        // Logic

        // calls member functions to get existing trader id list, create new trader, and return pair of such
        public static Pair<List<string>, Trader> GetNewTrader()
        {
            Trader theNewTrader = CreateNewTrader();
            var otherTraderIDs = GetAllTraderIDs();
            var pair = new Pair<List<string>, Trader>(otherTraderIDs, theNewTrader);
            return pair;
        }

        // instantiates and returns a new trader, assigning as stockholder if no stock holder current
        private static Trader CreateNewTrader()
        {
            Trader newTrader = new Trader(GetNewTraderID());
            Market.BroadCastMessage(Message.traderJoincedBroadCast(newTrader.ID));
            Console.WriteLine(Message.traderJoinedUI(newTrader.ID));
            AddMarketMessage(Message.traderJoinedUI(newTrader.ID));
            lock (traderListLock)
            {
                _traderList.Add(newTrader);
            }
            lock (stockHolderLock)
            {
                if (_stockHolder == null)
                {
                    //TODO: add server message for automatically giving stock away
                    Console.WriteLine(Message.traderAcqUI(newTrader.ID));
                    AddMarketMessage(Message.traderAcqUI(newTrader.ID));
                    _stockHolder = newTrader;
                }
            }
            return newTrader;
        }

        // gets a new trader ID string incrementing thread safe, with lock, ID number
        private static string GetNewTraderID()
        {
            string traderID = "Trader";
            lock(IDLock)
            {
                traderID += _IDNumber++;
            }
            return traderID;
        }

        // broadcasts a message to all traders by adding message to their outbox
        public static void BroadCastMessage(string message)
        {
            lock(traderListLock)
            {
                foreach (var trader in _traderList)
                    trader.AddMessage(message);
            }
        }

        // trades stock between two traders
        public static MessageEnums MakeTrade(ClientHandler handler, string otherTraderID)
        {
            Trader otherTrader = null;
            lock (traderListLock)
            {
                // gets other trader
                foreach (var trader in _traderList)
                    if (String.Equals(trader.ID.ToUpper(), otherTraderID.ToUpper()))
                        otherTrader = trader;

                // trade fail check
                if (otherTrader == null || otherTrader.DeathIndicator > 0)
                {
                    handler.SendMessage(Message.tradeFailBroadCast($"{otherTraderID} is invalid, disconnected," +
                        $" or no longer holds the stock"));
                    return MessageEnums.TRADE_FAIL;
                }

                //trade success
                StockHolder = otherTrader;
            }
            BroadCastMessage(Message.tradeBroadCast(handler.Trader.ID, otherTraderID));
            Console.WriteLine(Message.tradeUI(handler.Trader.ID, otherTraderID));
            AddMarketMessage(Message.tradeUI(handler.Trader.ID, otherTraderID));
            return MessageEnums.TRADE_SUCC;
        }

        // kicks a trader from the market
        public static void KillTrader(Trader trader)
        {
            if (trader == null)
                return;

            trader.KillTrader();
            Console.WriteLine(Message.traderLeftUI(trader.ID));
            BroadCastMessage(Message.traderLeftBroadCast(trader.ID));
            AddMarketMessage(Message.traderLeftUI(trader.ID));
            lock(traderListLock)
            {
                _traderList.Remove(trader);
            }
            // resets stock holder if trader leaving is stock holder
            lock (stockHolderLock)
            {
                if (Equals(_stockHolder, trader))
                    if (_traderList.Count == 0)
                        _stockHolder = null;
                    else
                    {
                        // gives stock to random trader
                        Random random = new Random();
                        int randomTraderIndex = (int)(random.NextDouble() * _traderList.Count);
                        Trader randomTrader = _traderList[randomTraderIndex];
                        _stockHolder = randomTrader;
                        BroadCastMessage(Message.traderAcqBroadCast(randomTrader.ID));
                        Console.WriteLine(Message.traderAcqUI(randomTrader.ID));
                        AddMarketMessage(Message.traderAcqUI(randomTrader.ID));
                    }
            }
        }

        // Returns reconnecting trader and a list of all the traders in the market
        public static Pair<List<string>, Trader> GetReconnectingTrader(string ID)
        {
            var testTrader = new Trader(ID);
            // gives new trader if the ID is not within the list of traders
            if (!HasTrader(testTrader))
            {
                var tradersAndTrader = GetNewTrader();
                tradersAndTrader.Second.Reconected = true;
                return tradersAndTrader;
            }

            // gets the trader associated with the ID and returns
            lock(traderListLock)
            {
                Trader traderToReturn = null;
                var otherTraders = new List<string>();
                foreach (var trader in _traderList)
                    if (Equals(trader.ID, ID))
                        traderToReturn = trader;
                    else
                        otherTraders.Add(trader.ID);
                traderToReturn.Reconected = true;
                return new Pair<List<string>, Trader>(otherTraders, traderToReturn);
            }
        }

        // when server has recovered from restart, this kicks any disconnected trader
        public static void KickDisconnectedTraders()
        {
            var traders = GetTraderListCopy();
            foreach (var trader in traders)
                if (!trader.Reconected)
                    KillTrader(trader);
        }

        // wipes market, only used in testing
        public static void ClearMarket()
        {
            lock (IDLock)
            {
                _IDNumber = BigInteger.Zero;
                lock (traderListLock)
                {
                    _traderList.Clear();
                }
                _stockHolder = null;
            }
        }

        // gets a list of all currently connected trader IDs
        private static List<string> GetAllTraderIDs()
        {
            var otherTradersIDList = new List<string>();
            lock (traderListLock)
            {
                foreach (var trader in _traderList)
                    otherTradersIDList.Add(trader.ID);
            }
            return otherTradersIDList;
        }

        // returns a copy of current traders
        private static List<Trader> GetTraderListCopy()
        {
            var traderListCopy = new List<Trader>();
            lock(traderListLock)
            {
                foreach (var trader in _traderList)
                    traderListCopy.Add(trader);
            }
            return traderListCopy;
        }

        // Adds a message to the market i.e trader left/joined/stock traded
        // mainly for use with ui
        public static void AddMarketMessage(string message)
        {
            lock(marketMessageListLock)
            {
                _marketMessages.Add(message);
            }
        }

        // returns a copy of all messages
        public static List<string> GetMarketMessagesCopy()
        {
            var copiedMessages = new List<string>();
            lock(marketMessageListLock)
            {
                foreach (var message in _marketMessages)
                    copiedMessages.Add(message);
            }
            return copiedMessages;
        }

        // checks trader exists in market, mainly used in testing
        public static bool HasTrader(Trader trader)
        {
            lock(traderListLock)
            {
                return _traderList.Contains(trader);
            }
        }

        // Json serialization methods
        //serialise
        public override void WriteJson(JsonWriter writer, object value, JsonSerializer serializer)
        {
            var market = value as Market;
            if (value == null || GetAllTraderIDs().Count == 0)
                return;
            writer.WriteStartObject();
            writer.WritePropertyName(JSON_TRADER_LIST_KEY);
            writer.WriteStartArray();
            lock (_traderList)
            {
                foreach (var trader in _traderList)
                    serializer.Serialize(writer, trader.ID);
            }
            writer.WriteEndArray();
            writer.WritePropertyName(JSON_STOCK_HOLDER);
            lock (stockHolderLock)
            {
                serializer.Serialize(writer, _stockHolder.ID);
            }
            writer.WritePropertyName(JSON_CURRENT_ID);
            lock (IDLock)
            {
                serializer.Serialize(writer, _IDNumber.ToString());
            }
            writer.WriteEndObject();
        }

        //deserialize
        public override object ReadJson(JsonReader reader, Type objectType, object existingValue, JsonSerializer serializer)
        {
            var market = new Market();
            ClearMarket();
            lock (_traderList)
            {
                lock (IDLock)
                {
                    JObject jsonObj = JObject.Load(reader);
                    var stockHolderID = jsonObj[JSON_STOCK_HOLDER].Value<string>();
                    var currentID = jsonObj[JSON_CURRENT_ID].Value<string>();
                    var jsonTraderList = jsonObj[JSON_TRADER_LIST_KEY].Value<JArray>();
                    var convertedList = jsonTraderList.ToObject<List<string>>();
                    foreach (var traderID in convertedList)
                    {
                        var trader = new Trader(traderID);
                        _traderList.Add(trader);
                        if (Equals(traderID, stockHolderID))
                            _stockHolder = trader;
                    }
                    _IDNumber = BigInteger.Parse(currentID);
                }
            }
            return market;
        }

        public override bool CanConvert(Type objectType)
        {
            return typeof(Market).IsAssignableFrom(objectType);
        }

        // simple serialization/deserialization methods for ease of access
        public static string Serialize()
        {
            Market market = new Market();
            return JsonConvert.SerializeObject(market, Formatting.Indented);
        }

        public static Market Deserialize(string jsonSerialized)
        {
            try
            {
                return JsonConvert.DeserializeObject<Market>(jsonSerialized);
            } catch (Exception) { return null; }
        }
    }
}
