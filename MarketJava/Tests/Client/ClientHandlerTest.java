package Client;

import Server.Market;
import Server.ServerProgram;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.Socket;

import static org.junit.Assert.*;
import static Utils.Message.*;

public class ClientHandlerTest {
    static boolean initialized = false;
    Socket socket;
    ClientHandlerStub handler;
    @BeforeClass
    public static void initServerAndClient() throws Exception
    {
        ClientProgram.restartServer();
    }

    @Before
    public void initsocket() throws Exception
    {
        socket = new Socket(ClientProgram.ADDRESS, ClientProgram.PORT);
        handler = new ClientHandlerStub();
        ClientHandler.trader = null;
        handler.init(socket);
        Thread.sleep(1000);
    }

    @After
    public void printMessage() throws Exception
    {
        handler.printResult();
        socket.close();
    }

    @Test
    public void testClientConnected()
    {
        assertNotNull(ClientHandler.trader);
    }

    @Test
    public void traderRecconectError() throws Exception
    {
        ServerProgram.serverRestarting = false;
        handler.sendMessage("TRADER_RECONNECTING 1");
        Thread.sleep(1000);
        assertEquals(ERROR, handler.serverResponse.first());
    }

    @Test
    public void traderTradeFailNotStockHolder() throws Exception
    {
        Server.Trader fakeTrader = new Server.Trader("someid");
        Server.Market.setStockHolder(fakeTrader);
        handler.sendMessage(String.format("TRADER_TRADE %s", fakeTrader.getID()));
        Thread.sleep(1000);
        assertEquals(TRADE_FAIL, handler.serverResponse.first());
    }

    @Test
    public void traderFailNoTraderWithID() throws Exception
    {
        Market.setStockHolder(new Server.Trader(ClientHandler.trader.getTraderID()));
        handler.sendMessage(String.format("TRADER_TRADE someid"));
        Thread.sleep(1000);
        assertEquals(TRADE_FAIL, handler.serverResponse.first());
    }

    @Test
    public void traderTradeSuccess() throws Exception
    {
        Market.setStockHolder(new Server.Trader(ClientHandler.trader.getTraderID()));
        Server.Trader newTrader = Server.Market.getNewTrader().second();
        handler.sendMessage(String.format("TRADER_TRADE " + newTrader.getID()));
        Thread.sleep(1000);
        assertEquals(TRADE_FAIL, handler.serverResponse.first());
    }
}