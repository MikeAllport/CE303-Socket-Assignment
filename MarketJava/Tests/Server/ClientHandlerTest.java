package Server;

import Utils.Message;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static Utils.Message.*;

public class ClientHandlerTest {
    Market market = new Market();
    ClientHandlerStub handler = new ClientHandlerStub(market);

    @Before
    public void init()
    {
        Market.setStockHolder(null);
        Market.traders.getList().clear();
    }

    @Test
    public void processLineFailNotEnum() throws Exception {
        assertEquals(handler.processLine(" "), ERROR);
        assertEquals(handler.processLine("Muhahaha"), ERROR);
        assertEquals(handler.processLine(""), ERROR);
    }

    @Test
    public void processLineSuccTRADER_TRADE() throws Exception
    {
        Trader t1 = Market.getNewTrader().second();
        assertEquals(t1, Market.getCurrentStockHolder());
        Trader t2 = Market.getNewTrader().second();
        handler.trader = t1;
        String tradeStr = TRADER_TRADE.getLabel() + t1.getID() + " " + t2.getID();
        assertEquals(TRADE_SUCC, handler.processLine(tradeStr));
    }

    @Test
    public void processLineFailTRADER_TRADE() throws Exception
    {
        Trader t1 = Market.getNewTrader().second();
        handler.trader = t1;
        assertEquals(ERROR, handler.processLine("TRADER_TRADE "));
        assertEquals(TRADE_FAIL, handler.processLine("TRADER_TRADE t1"));
        assertEquals(TRADE_FAIL, handler.processLine("TRADER_TRADE t1 t2"));
        assertEquals(TRADE_FAIL, handler.processLine("TRADER_TRADE t1 t2 t3"));
    }

    @Test
    public void processLineReturningTraderSuccess() throws Exception
    {
        ServerProgram.serverRestarting = true;
        assertEquals(TRADER_RECONNECTING, handler.processLine(Message.traderReconnectingBroadcast("SomeID")));
    }

    @Test
    public void processLineReturningTraderFailServerNotReboot()
    {
        ServerProgram.serverRestarting = false;
        try
        {
            handler.processLine(Message.traderReconnectingBroadcast("SomeID"));
            fail();
        } catch (Exception fail)
        {
            assertTrue(true);
        }
    }

    @Test
    public void killTrader() {
    }
}