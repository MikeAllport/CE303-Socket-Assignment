package Server;

import Utils.Pair;
import Utils.StoppableThread;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static Server.Message.*;

public class MarketTest {

    @Before
    public void init()
    {
        Market.getTraders().second().clear();
        Market.setStockHolder(null);
    }

    @Test
    public void newTraderStockHolderFirstTrader() {
        Trader trader1 = Market.newTrader().second();
        assertEquals(trader1, Market.getCurrentStockHolder());
        assertNotEquals(Market.newTrader().second(), Market.getCurrentStockHolder());
    }

    @Test
    public void getCurrentStockHolder() {
        assertNull(Market.getCurrentStockHolder());
        assertEquals(Market.newTrader().second(), Market.getCurrentStockHolder());
    }

    @Test
    public void setStockHolder() {
        Market.newTrader();
        Trader t = Market.newTrader().second();
        assertNotEquals(t, Market.getCurrentStockHolder());
        Market.setStockHolder(t);
        assertEquals(t, Market.getCurrentStockHolder());
    }

    @Test
    public void getTradersCorrectSizes() {
        assertEquals(0, Market.getTraders().second().size());
        Market.newTrader();
        Market.newTrader();
        Market.newTrader();
        assertEquals(3, Market.getTraders().second().size());
    }

    @Test
    public void getTradersLock() {
        assertFalse(Thread.holdsLock(Market.getTraders().first()));
        Pair<Object, ArrayList<Trader>> traders = Market.getTraders();
        synchronized (traders.first())
        {
            assertTrue(Thread.holdsLock(traders.first()));
        }
        StoppableThread thread = new StoppableThread(() -> {
            synchronized (traders.first())
            {
                assertTrue(Thread.holdsLock(traders.first()));
                while(StoppableThread.running);
            }
        });
        new Thread(thread).start();
        assertFalse(Thread.holdsLock(traders.first()));
        thread.stop();
    }

    @Test
    public void marketRemoveTrader()
    {
        Trader trader = Market.newTrader().second();
        Market.removeTrader(trader);
        assertFalse(Market.getTraders().second().contains(trader));
    }

    @Test
    public void marketRemoveTraderRemovesStockHolder()
    {
        Trader trader = Market.newTrader().second();
        Market.removeTrader(trader);
        assertEquals(Market.getCurrentStockHolder(), null);
    }

    @Test
    public void marketRemoveTraderGivesStockToOtherTrader()
    {
        Trader t1 = Market.newTrader().second();
        Trader t2 = Market.newTrader().second();
        assertEquals(t1, Market.getCurrentStockHolder());
        Market.removeTrader(t1);
        assertEquals(t2, Market.getCurrentStockHolder());
        assertEquals(t2.getMessages().get(0), TRADER_ACQ_STOCK.getLabel() + t2.getID());
    }
}