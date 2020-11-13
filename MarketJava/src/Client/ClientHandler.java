package Client;

import Server.Message;
import com.sun.istack.internal.NotNull;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import static Server.Message.*;

public class ClientHandler {
    private static final String DELIM = " ";
    private static Trader trader;

    private boolean serverReconnecting = false;
    private PrintWriter printWriter;
    private Socket socket;

    public ClientHandler()
    {
    }

    public void sendMessage(String message)
    {
        if (serverReconnecting)
            return;
        this.printWriter.println(message);
    }


    protected Server.Message processLine(@NotNull String line)
    {
        String arr[] = line.split(DELIM);
        if (arr.length <= 0)
            return ERROR;
        if (!isEnum(arr[0]))
        {
            arr[0] = "ERROR";
        }
        switch (valueOf(arr[0]))
        {
            case TRADER_ID:
                if (errorStringArrLength(1, 3, arr))
                    return ERROR;
                initTrader(arr[1]);
                return TRADER_ID;
            case TRADER_LIST:
                if (arr.length > 0)
                    traderList(arr);
                return TRADER_LIST;
            case TRADER_WITH_STOCK:
                if (errorStringArrLength(1, 3, arr))
                    return ERROR;
                //TODO:: implement
                return TRADER_WITH_STOCK;
            case TRADER_JOINED:
                if (errorStringArrLength(1, 3, arr))
                    return ERROR;
                traderJoined(arr[1]);
                return TRADER_JOINED;
            case TRADER_LEFT:
                if (errorStringArrLength(1, 3, arr))
                    return ERROR;
                traderLeft(arr);
                return TRADER_LEFT;
            case TRADE_FAIL:
                tradeFailed(arr);
                return TRADE_FAIL;
            case TRADER_ACQ_STOCK:
                if (errorStringArrLength(1, 3, arr))
                    return ERROR;
                traderAcqStock(arr);
                return TRADER_ACQ_STOCK;
            case SERVER_RESTORED:
                serverReconnecting = false;
                return SERVER_RESTORED;
            default:
                return ERROR;
        }
    }

    protected boolean errorStringArrLength(int lowerbound, int upperbound, String[] arr)
    {
        boolean response = arr.length <= lowerbound && arr.length >= upperbound;
        if (response)
            errorOccured(String.format("%s invalid response, %s%d arguments expected", arr[0],
                    upperbound - lowerbound));
        return response;
    }

    //TODO: implement setting me in ui
    protected void initTrader(String traderid)
    {
        ClientHandler.trader.setAccountID(traderid);
        ClientProgram.ui.addTrader(traderid);
        System.out.println("Account " + trader.getTraderID());
    }

    protected void traderList(String[] arr)
    {
        for (int i = 1; i < arr.length; i++)
        {
            if (!(arr[i].equals(ClientHandler.trader.getTraderID())))
                traderJoined(arr[i]);
        }
    }

    protected void errorOccured(String message)
    {
        ClientProgram.ui.addMessageError(message);
        System.out.println(message);
    }

    //TODO: add change of stock
    protected void traderAcqStock(String[] arr)
    {
        Trader trader = new Trader(arr[1]);
        String message = Message.traderAcqUI(trader.getTraderID());
        ClientProgram.ui.addMessage(message);
        System.out.println(message);
    }

    protected void traderLeft(String[] arr)
    {
        if (arr[1].equals(ClientHandler.trader.getTraderID()))
            return;
        ClientProgram.ui.removeTrader(arr[1]);
        String message = Message.traderLeftUI(arr[1]);
        ClientProgram.ui.addMessage(message);
        System.out.println(message);
    }

    protected void traderJoined(String id)
    {
        if (id.equals(ClientHandler.trader.getTraderID()))
            return;
        ClientProgram.ui.addTrader(id);
        ClientProgram.ui.addMessage(Message.traderJoinedUI(id));
        System.out.println(Message.traderJoinedUI(id));
    }

    protected void tradeFailed(String[] arr)
    {
        String message = arr[0];
        if (arr.length > 1)
        {
            message += " ";
            for (int i = 1; i < arr.length; ++i)
                message += " ";
        }
        ClientProgram.ui.addMessageError(message);
    }

    protected void giveStock(Trader trader)
    {

    }

    protected void init(Socket socket) throws Exception{
        this.socket = socket;
        this.printWriter = new PrintWriter(socket.getOutputStream());
        Scanner scanner = new Scanner(socket.getInputStream());
        if (ClientHandler.trader == null)
        {
            ClientHandler.trader = new Trader();
        }
        else
        {
            this.serverReconnecting = true;
            sendMessage(TRADER_RECONNECT.getLabel() + ClientHandler.trader.getTraderID());
        }
        new Thread(new SocketListener(scanner, this)).start();
    }
}
