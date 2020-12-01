package Server;

import org.junit.Test;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class ServerProgramTest {

    @Test
    public void testCreateEmptyBackupFile() throws IOException
    {
        ServerProgram.createEmptyBackupFile();
        Path outputFilePath = Paths.get(ServerProgram.BACKUP_FILE);
        String fileContents = Files.readString(outputFilePath);
        assertEquals(0, fileContents.length());
        // fills file with string
        OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(outputFilePath));
        String testOutput = "Test output";
        writer.write(testOutput);
        writer.flush();
        assertTrue(Files.readString(outputFilePath).equals(testOutput));
        // empties files contents
        ServerProgram.createEmptyBackupFile();
        fileContents = Files.readString(outputFilePath);
        assertEquals(0, fileContents.length());
    }

    @Test
    public void backupRestoreMarket()
    {
        new ServerProgram();
        Market market = new Market();
        Trader trader = Market.getNewTrader().second();
        trader = Market.getNewTrader().second();
        trader = Market.getNewTrader().second();
        ArrayList<Trader> currentMarket = new ArrayList<>();
        currentMarket.addAll(Market.traders.getList());
        Trader stockHolder = Market.getCurrentStockHolder();
        BigInteger currentTraderID = new BigInteger(Market.currentTraderID.toString());
        ServerProgram.backupMarket();
        MarketTest.resetMarket();
        assertFalse(stockHolder.equals(Market.getCurrentStockHolder()));
        ServerProgram.restoreMarket();
        assertEquals(stockHolder, Market.getCurrentStockHolder());
        assertEquals(currentMarket.size(), Market.traders.getList().size());
        for (Trader existingTrader: Market.traders.getList())
            assertTrue(currentMarket.contains(existingTrader));
        assertEquals(currentTraderID, Market.currentTraderID);
    }
}