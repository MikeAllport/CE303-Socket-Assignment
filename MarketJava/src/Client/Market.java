package Client;

import java.io.Serializable;
import java.util.ArrayList;

public class Market {
    protected static Trader hasStock = null;

    public static void attainedStock(Trader trader)
    {
        Market.hasStock = trader;
    }

    public static void marketCrash()
    {
        Market.hasStock = null;
    }
}
