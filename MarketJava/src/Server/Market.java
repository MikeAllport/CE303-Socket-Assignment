package Server;

import javafx.util.Pair;

import java.math.BigInteger;
import java.util.ArrayList;

public class Market {
    protected static BigInteger currentTraderID = BigInteger.ZERO;
    private static Trader currentStockHolder;
    protected static ArrayList<Trader> traders = new ArrayList<>();
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

    public Trader getCurrentStockHolder()
    {
        return currentStockHolder;
    }

    public void setStockHolder(Trader trader)
    {
        currentStockHolder = trader;
    }

    public static Pair<Object, ArrayList<Trader>> getTraders()
    {
        return new Pair<Object, ArrayList<Trader>>(tradersLock, traders);
    }
}
