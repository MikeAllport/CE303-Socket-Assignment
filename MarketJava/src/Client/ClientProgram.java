package Client;

import java.io.IOException;
import java.net.Socket;

public class ClientProgram {
    private final static String ADDRESS = "localhost";
    private final static int PORT = 8888;

    ClientHandler handler;

    public ClientProgram()
    {
//        initClientHandlerSocket();
        try {
            runServerSeperateProcess();
        } catch (IOException e)
        {}
        while(true)
        {}
    }

    private void initClientHandlerSocket() throws Exception
    {
//        try
//        {
//            Socket socket = new Socket(ADDRESS, PORT);
//
//        }
    }

    public static void runServerSeperateProcess() throws IOException
    {
        System.out.println(System.getProperty("user.dir"));
        ProcessBuilder p = new ProcessBuilder("java.exe", "-cp", System.getProperty("user.dir") + "\\out\\", "Server.ServerProgram");
        p.start();
    }

    public static void main(String[] args) {
        new ClientProgram();
    }
}
