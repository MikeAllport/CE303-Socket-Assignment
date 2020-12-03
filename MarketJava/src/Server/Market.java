package Server;

import Utils.ListLock;
import Utils.Message;
import Utils.Pair;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Market's purpose is to store an active list of all conected traders and contain logic related to
 * creating a new trader. All ClientHandlers have to call static getNewTrader method to attain a new trader,
 * that way this class is solely responsible for incrementing and handling the trader id.
 *
 * BigInteger is used for a boundless ID number. This class is fully serializable with custom serializing
 * implementations. This class is also thread safe with any methods accessing the traders list being synchronised.
 */
public class Market implements JsonSerializer<Market>, JsonDeserializer {
    private static final String JSON_TRADER_LIST_KEY = "Traders",
            JSON_CURRENT_ID = "CurrentID",
            JSON_STOCK_HOLDER = "StockHolder";
    private static Trader currentStockHolder = null;
    protected static BigInteger currentTraderID = BigInteger.ZERO;
    protected static ListLock<Trader> traders = new ListLock<>();

    // main method for trader creation, returns a pair containing a list of the currently connected clients ids
    // as first in pair and the new trader as second in pair. Also assigns stockholder if stockholder is null
    public static synchronized Pair<ArrayList<String>, Trader> getNewTrader()
    {
        Trader trader = new Trader(getNewTraderID());
        ArrayList<String> currentTraders = new ArrayList<>();
        synchronized (traders.getLock())
        {
            for (Trader otherTrader: traders.getList())
                currentTraders.add(otherTrader.getID());
            traders.getList().add(trader);
            if (currentStockHolder == null) {
                currentStockHolder = trader;
                System.out.println(Message.traderAcqUI(trader.getID()));
            }
        }
        return new Pair<>(currentTraders, trader);
    }

    // method responsible for incrementing the trader id number. As getNewTrader is synchronized this does not have
    // to be
    private static String getNewTraderID()
    {
        String id = "Trader" + currentTraderID;
        currentTraderID = currentTraderID.add(BigInteger.ONE);
        System.out.println(Message.traderJoinedUI(id));
        return id;
    }

    // finds an existing trader to return, or returns a new trader id if the given argument id is not found
    // return type is the same as getNewTrader with a pair containing current trader ids and new/existing trader
    public static Pair<ArrayList<String>, Trader> getReturningTrader(String traderID)
    {
        Trader traderToReturn = null;
        ArrayList<String> existingTraderIDs = new ArrayList<>();
        synchronized (traders.getLock())
        {
            for (Trader trader: traders.getList())
                if (trader.getID().toUpperCase().equals(traderID.toUpperCase()))
                {
                    traderToReturn = trader;
                    traderToReturn.reconnected = true;
                }
                else existingTraderIDs.add(trader.getID());
        }
        if (traderToReturn == null)
        {
            traderToReturn = getNewTrader().second();
        }
        return new Pair<>(existingTraderIDs, traderToReturn);
    }

    // logic for removing a trader from market. This checks if the trader being removed is the stock holder,
    // if so gives the stock to a random trader in the list or sets to null and handles message broadcasting
    public static void removeTrader(Trader trader)
    {
        System.out.println(Message.traderLeftUI(trader.getID()));
        synchronized (Market.traders.getLock())
        {
            Market.traders.getList().remove(trader);
            if (Market.getCurrentStockHolder().equals(trader))
            {
                if (Market.traders.getList().size() == 0)
                    setStockHolder(null);
                else
                {
                    Trader randomTrader = Market.traders.getList().get((int) Math.floor(Math.random() *
                            Market.traders.getList().size()));
                    setStockHolder(randomTrader);
                    ServerProgram.ui.addMessage(Message.traderAcqUI(randomTrader.getID()));
                    ClientHandler.broadcast(Message.traderAcqBroadCast(randomTrader.getID()));
                    System.out.println(Message.traderAcqUI(randomTrader.getID()));
                }
            }
        }
    }

    // method called when server restoring has finished, this clears out any traders who has not reconnected
    // with the reconnected control boolean in trader
    public static void cleanTradersNotReconnected()
    {
        ArrayList<Trader> disconnectedTraderList = new ArrayList<>();
        synchronized (Market.traders.getLock())
        {
            for (Trader trader: Market.traders.getList())
                if (!trader.reconnected)
                    disconnectedTraderList.add(trader);
        }
        for (Trader disconnectedTrader: disconnectedTraderList)
        {
            String disconnectingTrader = String.format("Disconnecting trader %s", disconnectedTrader.getID());
            ServerProgram.ui.addMessage(disconnectingTrader);
            removeTrader(disconnectedTrader);
            ClientHandler.broadcast(Message.traderLeftBroadCast(disconnectedTrader.getID()));
        }
    }

    public static Trader getCurrentStockHolder()
    {
        return Market.currentStockHolder;
    }

    public static void setStockHolder(Trader trader)
    {
        Market.currentStockHolder = trader;
    }


    // serialization/deserialization methods in format interchangeable with csharp and java implementation
    @Override
    public JsonElement serialize(Market o, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject marketJson = new JsonObject();
        //dont serialize is empty market
        if (Market.currentStockHolder == null || Market.traders.getList().size() == 0)
            return marketJson;

        //serialize
        JsonArray jsonListEles = new JsonArray();
        for (Trader trader: Market.traders.getList())
            jsonListEles.add(trader.getID());
        marketJson.add(JSON_TRADER_LIST_KEY, jsonListEles);
        marketJson.addProperty(JSON_STOCK_HOLDER, Market.currentStockHolder.getID());
        marketJson.addProperty(JSON_CURRENT_ID, Market.currentTraderID);
        return marketJson;
    }

    @Override
    public Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject marketJson = jsonElement.getAsJsonObject();
        Market newMarket = new Market();
        //dont deserialize is backup was empty
        if (!(marketJson.has(JSON_TRADER_LIST_KEY)))
            return newMarket;

        //deserialize
        JsonArray tradersJson = marketJson.get(JSON_TRADER_LIST_KEY).getAsJsonArray();
        BigInteger currentTraderIDJson = marketJson.get(JSON_CURRENT_ID).getAsBigInteger();
        String stockHolderIDJson = marketJson.get(JSON_STOCK_HOLDER).getAsString();
        Market.currentTraderID = currentTraderIDJson;
        for (JsonElement traderID: tradersJson)
        {
            Trader trader = new Trader(traderID.getAsString());
            Market.traders.getList().add(trader);
            if (trader.getID().equals(stockHolderIDJson))
                Market.currentStockHolder = trader;
        }
        return newMarket;
    }
}
