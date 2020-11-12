//package Server;
//
//import Server.Market;
//import Server.ServerProgram;
//import Server.Trader;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.net.Socket;
//
//import static org.junit.Assert.*;
//
//public class MarketTest {
//
//    Market market;
//    Socket socket;
//    final static String marketID = Market.getNewTraderID();
//    @Before
//    public void beforeTests() throws Exception
//    {
//        market = new Market();
//        ServerProgram.runServerSeperateProcess();
//        socket = new Socket(ServerProgram.ADDRESS, ServerProgram.PORT);
//        Market.currentTraderID = marketID;
//    }
//
//    @Test
//    public void getNewTraderID() {
//        for (int i = 0; i < 100; ++i)
//        {
//            assertEquals("Server.Trader" + i, Market.currentTraderID);
//            Market.getNewTraderID();
//        }
//    }
//
//    @Test
//    public void assignAndGetStockHolder() throws Exception
//    {
//        assertEquals(market.getCurrentStockHolder(), null);
//        Trader t1 = new Trader(this.socket, market.getNewTraderID());
//        market.setStockHolder(t1);
//        assertEquals(t1, market.getCurrentStockHolder());
//    }
//}