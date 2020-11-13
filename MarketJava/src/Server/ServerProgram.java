package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerProgram {
    final static String ADDRESS = "localhost";
    final static int PORT = 8888;
    private final static String UI_TITLE = "Server";
    public static GUI ui = new GUI(UI_TITLE);


    public static void main(String[] args) {
        ServerProgram.runServer(new Market());
    }

    public static void runServer(Market m)
    {
        ServerSocket serverSocket = null;
        try
        {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Awaiting connections");
            while(true)
            {
                Socket socket = serverSocket.accept();
                new Thread(new TraderHandler(socket, m)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to start server");
            ServerProgram.ui.close();
        }
    }
}
