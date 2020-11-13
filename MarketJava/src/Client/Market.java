package Client;

import java.io.Serializable;
import java.util.ArrayList;

public class Market implements Serializable {
    protected static final ArrayList<Trader> traders = new ArrayList<>();
    protected static Trader hasStock = null;
    protected static Trader clientTrader = null;

    public static void attainedStock(Trader trader)
    {
        Market.hasStock = trader;
    }

    public static void setClientTrader(Trader trader)
    {
        Market.clientTrader = trader;
    }

    public static Trader getClientTrader()
    {
        return Market.clientTrader;
    }

    public static void traderJoined(Trader trader)
    {
        Market.traders.add(trader);
    }

    public static void traderLeft(Trader trader)
    {
        Market.traders.remove(trader);
    }

    public static void marketCrash()
    {
        Market.hasStock = null;
        Market.traders.clear();
    }
}
