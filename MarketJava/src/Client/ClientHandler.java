package Client;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import static Server.Message.*;

public class ClientHandler {
    private static final String DELIM = " ";

    private static Trader trader;
    private PrintWriter printWriter;
    private Scanner scanner;

    public ClientHandler(Socket socket) throws Exception
    {
        this.init(socket);
    }

    public void sendMessage(String message)
    {
        this.printWriter.println(message);
    }

    protected Server.Message processLine(String line)
    {
        String arr[] = line.split(DELIM);
        Long id;
        switch (arr[0].toUpperCase())
        {
            case "ACC":
//                id = checkLongError(arr[1], "Account login");
//                client.setAccountID(id);
                System.out.println("Account " + trader);
                return TRADER_ID;
            case "TRADE_FAIL":
                System.out.println("Could not acquire stock");
                return TRADE_FAIL;
            case "TRADER_LEFT":
//                id = checkLongError(arr[1], "Trader leaving");
                return TRADER_LEFT;
            case "TRADER_JOINED":
//                id = check
                return TRADER_JOINED;
            case "TRADER_ACQ_STOCK":
                return TRADER_ACQ_STOCK;
            default:
                return ERROR;
        }
    }

    public void init(Socket socket) throws Exception{
        this.trader = new Trader();
        this.printWriter = new PrintWriter(socket.getOutputStream());
        this.scanner = new Scanner(socket.getInputStream());
        new Thread(new SocketListener(this.scanner, this));
    }
}
