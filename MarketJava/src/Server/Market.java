package Server;

import Utils.ListLock;
import Utils.Pair;

import java.math.BigInteger;
import java.util.ArrayList;


public class Market {
    protected static BigInteger currentTraderID = BigInteger.ZERO;
    private static Trader currentStockHolder;
    protected static ListLock<Trader> traders = new ListLock<>();

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
        synchronized (traders.getLock())
        {
            for (Trader otherTrader: traders.getList())
                currentTraders.add(otherTrader.getID());
            traders.getList().add(trader);
            if (currentStockHolder == null)
                currentStockHolder = trader;
        }
        return new Pair<>(currentTraders, trader);
    }

    public static void removeTrader(Trader trader)
    {
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
                    ServerProgram.ui.addMessage(Message.traderAcqUI(currentStockHolder.getID()));
                    TraderHandler.broadcast(Message.traderAcqBroadCast(randomTrader.getID()));
                }
            }
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
}
