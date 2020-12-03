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

/**
 * ServerProgram holds the main logic for opening the servers socket and instantiating new threads for
 * connected sockets using the ClientHandler class for that purpose.
 *
 * This class also contains methods related to the serialization of the market, creating an empty backup file,
 * and restoring the market. Backing up of the market happens by attaching a shutdown hook, which starts an
 * independent thread whenever the process is closed. When the process is started, if it is called with the
 * 'Restore' argument, the server restores the market from external json backup (compatible with both csharp and
 * java) and sets a serverRestarting boolean so that ClientHandler can accept reconnecting clients. If the server
 * is not started with the Restore argument, the backup file is wiped clean and server runs as normal.
 */
public class ServerProgram {

    private final static String UI_TITLE = "Server",
            BACKUP_PATH = "../",
            SERVER_RESTORE_ARGUMENT = "RESTORE";
    protected final static String BACKUP_FILE = BACKUP_PATH + "MarketBackup.json",
            ADDRESS = "localhost";
    protected final static int PORT = 8888,
            SERVER_RESTART_DURATION = 5000;

    // thread list used to close socket connections cleanly on exit
    private final static ArrayList<ClientHandler> traderThreads = new ArrayList<>();
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

    // attempts to open backup file and empty, or creates new file if not exists
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

    // opens the backup file deserializing the market and creates thread that clears serverRestarting
    // after a given duration
    protected static void restoreMarket()
    {
        try
        {
            Gson gson = new GsonBuilder().registerTypeAdapter(Market.class, new Market()).create();
            Path backupPath = Paths.get(BACKUP_FILE);
            String marketJson = Files.readString(backupPath);
            gson.fromJson(marketJson, Market.class);
            serverRestarting = true;
            createEmptyBackupFile();
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

    // calls market to kick clients not reconnected, sends recovered message to clients and ui, and clears
    // restarting control boolean
    private static void recoverFromReboot()
    {
        Market.cleanTradersNotReconnected();
        ClientHandler.broadcast(Message.serverRecoveredBroadCast());
        ui.addMessage(Message.serverRecoveredUI());
        serverRestarting = false;
    }

    // main method/loop listening for new socket connections and instantiating ClientHandler threads
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
                ClientHandler clientHandler = new ClientHandler(socket);
                ServerProgram.traderThreads.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to start server");
            ServerProgram.ui.close();
        }
    }

    // closes all active socket connections before closing and backs up market
    protected static void cleanupOnExit()
    {
        for (ClientHandler clientHandler : ServerProgram.traderThreads)
        {
            clientHandler.closeSocket();
        }
        backupMarket();
    }

    // gson serialization of market to backup file
    protected static void backupMarket()
    {
        try {
            Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().registerTypeAdapter(Market.class,
                    new Market()).create();
            Path backupPath = Paths.get(BACKUP_FILE);
            BufferedWriter writer = Files.newBufferedWriter(backupPath);
            gson.toJson(new Market(), writer);
            writer.close();
        } catch (IOException error)
        {
            System.out.println("Failed to backup market to JSON");
            error.printStackTrace();
        }
    }
}
