package Server;

import Utils.Pair;

import java.math.BigInteger;
import java.util.ArrayList;

import static Server.Message.*;

public class Market {
    protected static BigInteger currentTraderID = BigInteger.ZERO;
    private static Trader currentStockHolder;
    private static ArrayList<Trader> traders = new ArrayList<>();
    protected static Object tradersLock = new Object();

    public Market()
    {
        currentStockHolder = null;
    }

    private static String getNewTraderID()
    {
        String id = "Trader" + currentTraderID;
        currentTraderID = currentTraderID.add(BigInteger.ONE);
        return id;
    }

    public static synchronized Pair<ArrayList<String>, Trader> newTrader()
    {
        Trader trader = new Trader(getNewTraderID());
        ArrayList<String> currentTraders = new ArrayList<>();
        synchronized (tradersLock)
        {
            for (Trader otherTrader: traders)
                currentTraders.add(otherTrader.getID());
            traders.add(trader);
            if (currentStockHolder == null)
                currentStockHolder = trader;
        }
        return new Pair<>(currentTraders, trader);
    }

    public static void removeTrader(Trader trader)
    {
        Pair<Object, ArrayList<Trader>> traders = Market.getTraders();
        synchronized (traders.first()) {
            ArrayList<Trader> tradersList = traders.second();
            tradersList.remove(trader);
            if (Market.getCurrentStockHolder().equals(trader))
            {
                if (tradersList.size() == 0)
                    setStockHolder(null);
                else
                {
                    Trader randomTrader = tradersList.get((int) Math.floor(Math.random() * tradersList.size()));
                    setStockHolder(randomTrader);
                    ServerProgram.ui.addMessage(Message.traderAcqUI(currentStockHolder.getID()));
                    TraderHandler.broadcast(Message.traderAcqBC(randomTrader.getID()));
                }
            }
            ServerProgram.ui.addMessage(Message.traderLeftUI(trader.getID()));
            ServerProgram.ui.removeTrader(trader.getID());
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

    public static Pair<Object, ArrayList<Trader>> getTraders()
    {
        return new Pair<>(Market.tradersLock, Market.traders);
    }
}
