//package Server;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import java.net.Socket;
//
//import static org.junit.Assert.*;
//
//public class TraderHandlerTest {
//    Market market;
//    TraderHandlerStub th;
//    Socket socket;
//
//    @Before
//    public void init() throws Exception
//    {
//        this.market = new Market();
//        this.th = new TraderHandlerStub(new Socket(), market);
//        this.th.removeAll();
//        ServerProgram.runServerSeperateProcess();
//        socket = new Socket(ServerProgram.ADDRESS, ServerProgram.PORT);
//    }
//
//    @Test
//    public void testGetNextTraderID() throws Exception
//    {
//        int expectedID = 999;
//
//        for (int i = 0; i < expectedID; i++)
//        {
//            Trader trader = new Trader(this.socket, Market.getNewTraderID());
//            this.th.addTrader(trader);
//        }
//
//        String traderid = Market.getNewTraderID();
//        Trader trader = new Trader(this.socket, traderid);
//        assertEquals("Trader999", trader.getID());
//    }
//
//    @Test
//    public void testKillTrader() throws Exception
//    {
//        Trader t1 = new Trader(this.socket, "");
//        th.addTrader(t1);
//        assertTrue(th.testKillTrader(t1));
//        assertFalse(th.testKillTrader(t1));
//    }
//
//    @Test
//    public void processLine() {
//    }
//
//    @Test
//    public void testRun() {
//
//    }
//}