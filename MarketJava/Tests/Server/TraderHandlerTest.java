package Server;

import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;
import static Server.Message.*;

public class TraderHandlerTest {
    Market market = new Market();
    TraderHandlerStub handler = new TraderHandlerStub(market);

    @Before
    public void init()
    {
        Market.setStockHolder(null);
        Market.traders.getList().clear();
    }

    @Test
    public void processLineFailNotEnum() {
        assertEquals(handler.processLine(" "), ERROR);
        assertEquals(handler.processLine("Muhahaha"), ERROR);
        assertEquals(handler.processLine(""), ERROR);
    }

    @Test
    public void processLineSuccTRADER_TRADE()
    {
        Trader t1 = Market.newTrader().second();
        assertEquals(t1, Market.getCurrentStockHolder());
        Trader t2 = Market.newTrader().second();
        handler.trader = t1;
        String tradeStr = TRADER_TRADE.getLabel() + t1.getID() + " " + t2.getID();
        assertEquals(TRADE_SUCC, handler.processLine(tradeStr));
    }

    @Test
    public void processLineFailTRADER_TRADE()
    {
        Trader t1 = Market.newTrader().second();
        handler.trader = t1;
        assertEquals(handler.processLine("TRADER_TRADE "), ERROR);
        assertEquals(handler.processLine("TRADER_TRADE t1"), ERROR);
        assertEquals(handler.processLine("TRADER_TRADE t1 t2"), TRADE_FAIL);
        assertEquals(handler.processLine("TRADER_TRADE t1 t2 t3"), ERROR);
    }

    @Test
    public void killTrader() {
    }
}