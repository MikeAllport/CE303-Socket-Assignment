package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerProgram {
    final static String ADDRESS = "localhost";
    final static int PORT = 8888;
    public final static GUI ui = new GUI();


    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
        ServerProgram.runServer(new Market());
        return;
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
        }
    }
}
