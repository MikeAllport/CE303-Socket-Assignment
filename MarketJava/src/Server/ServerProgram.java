package Server;

import Utils.Message;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ServerProgram {

    private final static String UI_TITLE = "Server";
    private final static String BACKUP_PATH = "../";
    private final static int SERVER_RESTART_DURATION = 3000;
    private final static String SERVER_RESTORE_ARGUMENT = "RESTORE";
    private final static ArrayList<TraderHandler> traderThreads = new ArrayList<>();
    protected final static String BACKUP_FILE = BACKUP_PATH + "MarketBackup.json";
    protected final static String ADDRESS = "localhost";
    protected final static int PORT = 8888;
    protected static Market market = new Market();
    protected static GUI ui = new GUI(UI_TITLE);
    public static boolean serverRestarting = false;

    public static void main(String[] args) {
        // adds cleanup function callback when process closes to backup market and close connections
        Runtime.getRuntime().addShutdownHook(new Thread(ServerProgram::cleanupOnExit, "Shutdown-thread"));
        if (args.length != 0 && args[0].toUpperCase().equals(SERVER_RESTORE_ARGUMENT))
            restoreMarket();
        else
            createEmptyBackupFile();
        ServerProgram.runServer();
    }

    protected static void restoreMarket()
    {
        try
        {
            Gson gson = new GsonBuilder().registerTypeAdapter(Market.class, new Market()).create();
            Path backupPath = Paths.get(BACKUP_FILE);
            String marketJson = Files.readString(backupPath);
            Market market = gson.fromJson(marketJson, Market.class);
            serverRestarting = true;
            new Thread(() ->
            {
                // thread to boot out traders not reconnected
                try
                {
                    Thread.sleep(SERVER_RESTART_DURATION);
                    recoverFromReboot();
                } catch (InterruptedException ignore){}
            }).start();
        } catch (IOException e) {}
    }

    private static void recoverFromReboot()
    {
        Market.cleanTradersNotReconnected();
        TraderHandler.broadcast(Message.serverRecoveredBroadCast());
        ui.addMessage(Message.serverRecoveredUI());
        serverRestarting = false;
    }

    protected static void createEmptyBackupFile() {
        Path backupPath = Paths.get(BACKUP_FILE);
        try
        {
            try {
                BufferedWriter write = Files.newBufferedWriter(backupPath);
                write.close();
            } catch (NoSuchFileException noFile) {
                // creates new file
                System.out.println("Attempting to create backup file");
                Files.createFile(backupPath);
                BufferedWriter write = Files.newBufferedWriter(backupPath);
                write.close();
            }
        }catch (IOException error)
        {
            System.out.printf("Failed to initiate empty backup file %s%n", BACKUP_FILE);
            error.printStackTrace();
        }
    }

    public static void runServer()
    {
        ServerSocket serverSocket;
        try
        {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Awaiting connections");
            while(true)
            {
                Socket socket = serverSocket.accept();
                TraderHandler traderHandler = new TraderHandler(socket);
                ServerProgram.traderThreads.add(traderHandler);
                new Thread(traderHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to start server");
            ServerProgram.ui.close();
        }
    }


    protected static void cleanupOnExit()
    {
        for (TraderHandler traderHandler: ServerProgram.traderThreads)
        {
            traderHandler.closeSocket();
        }
        backupMarket();
    }

    protected static void backupMarket()
    {
        try {
            Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().registerTypeAdapter(Market.class,
                    market).create();
            Path backupPath = Paths.get(BACKUP_FILE);
            BufferedWriter writer = Files.newBufferedWriter(backupPath);
            gson.toJson(market, writer);
            writer.close();
        } catch (IOException error)
        {
            System.out.println("Failed to backup market to JSON");
            error.printStackTrace();
        }
    }
}
