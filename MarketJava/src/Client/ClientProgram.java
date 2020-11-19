package Client;

import java.net.Socket;

public class ClientProgram {
    private final static String ADDRESS = "localhost";
    private final static int PORT = 8888;
    private final static String EXECUTABLE = "java.exe";
    private final static String DIRECTORY = System.getProperty("user.dir") + "\\out\\";
    private final static String SERVER_CLASS_NAME = "Server.ServerProgram";
    private final static String UI_TITLE = "Client";
    protected static Boolean socketClosed = false;
    protected final static GUI ui = new GUI(UI_TITLE);

    protected static ClientHandler handler;

    public ClientProgram()
    {
        ClientProgram.handler = new ClientHandler();
        initSocket();
    }

    private void initSocket()
    {
        try
        {
            Socket socket = new Socket(ADDRESS, PORT);
            initClientHandler(socket);
            while(!ClientProgram.socketClosed)
            {
                Thread.sleep(60);
            }
            throw new Exception("Socket closed");
        } catch (Exception e)
        {
            try
            {
                Market.marketCrash();
                restartServer();
            } catch (ServerIrreparableException serverError)
            {
                serverError.printStackTrace();
            }
        }
    }

    private void initClientHandler(Socket socket) throws Exception
    {
        ClientProgram.handler.init(socket);
    }

    private void restartServer() throws ServerIrreparableException
    {
        try
        {
            String attempt = "Server restart attempt 1";
            System.out.println(attempt);
            runServerSeparateProcess(ClientProgram.EXECUTABLE, ClientProgram.DIRECTORY,
                    ClientProgram.SERVER_CLASS_NAME);
            Thread.sleep(60);
            ClientProgram.socketClosed = false;
            initSocket();
        } catch (Exception e)
        {
            throw new ServerIrreparableException("Failed to restart server during thread sleep");
        }
    }

    public static void runServerSeparateProcess(String executable, String dir, String className)
            throws Exception
    {
        ProcessBuilder p = new ProcessBuilder(executable, "-cp", dir, className);
        p.start();
    }

    public static void main(String[] args) {
        new ClientProgram();
    }
}
