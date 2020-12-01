package Server;

import Utils.StoppableThread;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;
import static Utils.Message.*;

public class MarketTest {

    @Before
    public void init()
    {
        resetMarket();
    }

    public static void resetMarket()
    {
        Market.traders.getList().clear();
        Market.setStockHolder(null);
        Market.currentTraderID = BigInteger.ZERO;
    }

    @Test
    public void newTraderStockHolderFirstTrader() {
        Trader trader1 = Market.getNewTrader().second();
        assertEquals(trader1, Market.getCurrentStockHolder());
        assertNotEquals(Market.getNewTrader().second(), Market.getCurrentStockHolder());
    }

    @Test
    public void getCurrentStockHolder() {
        assertNull(Market.getCurrentStockHolder());
        assertEquals(Market.getNewTrader().second(), Market.getCurrentStockHolder());
    }

    @Test
    public void setStockHolder() {
        Market.getNewTrader();
        Trader t = Market.getNewTrader().second();
        assertNotEquals(t, Market.getCurrentStockHolder());
        Market.setStockHolder(t);
        assertEquals(t, Market.getCurrentStockHolder());
    }

    @Test
    public void getTradersCorrectSizes() {
        assertEquals(0, Market.traders.getList().size());
        Market.getNewTrader();
        Market.getNewTrader();
        Market.getNewTrader();
        assertEquals(3, Market.traders.getList().size());
    }

    @Test
    public void getTradersLock() {
        assertFalse(Thread.holdsLock(Market.traders.getList()));
        synchronized (Market.traders.getLock())
        {
            assertTrue(Thread.holdsLock(Market.traders.getLock()));
        }
        StoppableThread thread = new StoppableThread(() -> {
            synchronized (Market.traders.getLock())
            {
                assertTrue(Thread.holdsLock(Market.traders.getLock()));
                while(StoppableThread.running);
            }
        });
        new Thread(thread).start();
        assertFalse(Thread.holdsLock(Market.traders.getLock()));
        thread.stop();
    }

    @Test
    public void marketRemoveTrader()
    {
        Trader trader = Market.getNewTrader().second();
        Market.removeTrader(trader);
        assertFalse(Market.traders.getList().contains(trader));
    }

    @Test
    public void marketRemoveTraderRemovesStockHolder()
    {
        Trader trader = Market.getNewTrader().second();
        Market.removeTrader(trader);
        assertEquals(Market.getCurrentStockHolder(), null);
    }

    @Test
    public void marketRemoveTraderGivesStockToOtherTrader()
    {
        Trader t1 = Market.getNewTrader().second();
        Trader t2 = Market.getNewTrader().second();
        assertEquals(t1, Market.getCurrentStockHolder());
        Market.removeTrader(t1);
        assertEquals(t2, Market.getCurrentStockHolder());
        assertEquals(t2.getMessages().get(0), TRADER_ACQ_STOCK.getLabel() + t2.getID());
    }
}