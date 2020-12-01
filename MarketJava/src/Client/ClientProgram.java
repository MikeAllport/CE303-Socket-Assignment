package Client;

import java.net.Socket;

public class ClientProgram {
    protected final static String ADDRESS = "localhost";
    protected final static int PORT = 8888;
    private final static String EXECUTABLE = "java.exe",
            CLASS_PATH = "./libs/gson-2.8.2.jar;./out",
            SERVER_CLASS_NAME = "Server.ServerProgram",
            UI_TITLE = "Client";
    protected static Boolean socketClosed = false;

    protected final static GUI ui = new GUI(UI_TITLE);

    protected static ClientHandler handler;

    public ClientProgram()
    {
        ClientProgram.handler = new ClientHandler();
        initSocket();
    }

    protected void initSocket()
    {
        try
        {
            Socket socket = new Socket(ADDRESS, PORT);
            ClientProgram.handler.init(socket);
            while(!ClientProgram.socketClosed)
            {
                Thread.sleep(60);
            }
            throw new Exception("Socket closed");
        } catch (Exception e)
        {
            try
            {
                ui.resetTraders();
                restartServer();
                initSocket();
            } catch (ServerIrreparableException serverError)
            {
                serverError.printStackTrace();
            }
        }
    }

    protected static void restartServer() throws ServerIrreparableException
    {
        try
        {
            String attempt = "Server restart attempt 1";
            System.out.println(attempt);
            runServerSeparateProcess(ClientProgram.EXECUTABLE, ClientProgram.CLASS_PATH,
                    ClientProgram.SERVER_CLASS_NAME);
            Thread.sleep(1000);
            ClientProgram.socketClosed = false;
        } catch (Exception e)
        {
            throw new ServerIrreparableException("Failed to restart server during thread sleep");
        }
    }

    public static void runServerSeparateProcess(String executable, String classpath, String className)
            throws Exception
    {
        ProcessBuilder p = new ProcessBuilder(executable, "-cp", classpath, className, "restore");
        p.start();
    }

    public static void main(String[] args) {
        new ClientProgram();
    }
}
