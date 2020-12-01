package Server;

import Utils.ListLock;
import Utils.Message;
import Utils.Pair;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;


public class Market implements JsonSerializer<Market>, JsonDeserializer {
    private static final String JSON_TRADER_LIST_KEY = "Traders",
            JSON_CURRENT_ID = "CurrentID",
            JSON_STOCK_HOLDER = "StockHolder";
    private static Trader currentStockHolder = null;
    protected static BigInteger currentTraderID = BigInteger.ZERO;
    protected static ListLock<Trader> traders = new ListLock<>();

    private static String getNewTraderID()
    {
        String id = "Trader" + currentTraderID;
        currentTraderID = currentTraderID.add(BigInteger.ONE);
        System.out.println(Message.traderJoinedUI(id));
        return id;
    }

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
                    TraderHandler.broadcast(Message.traderAcqBroadCast(randomTrader.getID()));
                    System.out.println(Message.traderAcqUI(randomTrader.getID()));
                }
            }
        }
    }

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
            TraderHandler.broadcast(Message.traderLeftBroadCast(disconnectedTrader.getID()));
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
