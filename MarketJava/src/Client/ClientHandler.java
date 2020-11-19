package Client;

import Server.Message;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import static Server.Message.*;

public class ClientHandler {
    private static final String DELIM = " ";
    private static Trader trader;
    private static final Market market = new Market();

    private boolean serverReconnecting = false;
    private PrintWriter printWriter;

    public ClientHandler()
    {
    }

    public void sendMessage(String message)
    {
        if (this.serverReconnecting || printWriter == null)
            return;
        this.printWriter.println(message);
    }

    protected void trade(String traderID)
    {
        sendMessage(TRADER_TRADE.getLabel() + traderID);
    }

    protected Server.Message processLine(String line)
    {
        String arr[] = line.split(DELIM);
        if (arr.length <= 0)
            return ERROR;
        if (!isEnum(arr[0]))
        {
            arr[0] = "ERROR";
        }
        try
        {
            switch (valueOf(arr[0]))
            {
                case TRADER_ID:
                    initTrader(arr[1]);
                    return TRADER_ID;
                case TRADER_LIST:
                    if (arr.length > 0)
                        traderList(arr);
                    return TRADER_LIST;
                case TRADER_WITH_STOCK:
                    ClientProgram.ui.setStockHolder(arr[1]);
                    return TRADER_WITH_STOCK;
                case TRADER_JOINED:
                    traderJoined(arr[1]);
                    return TRADER_JOINED;
                case TRADER_LEFT:
                    traderLeft(arr);
                    return TRADER_LEFT;
                case TRADE_SUCC:
                    tradeSucc(arr[1], arr[2]);
                    return TRADE_SUCC;
                case TRADER_ACQ_STOCK:
                    traderAcqStock(arr);
                    return TRADER_ACQ_STOCK;
                case SERVER_RESTORED:
                    serverReconnecting = false;
                    return SERVER_RESTORED;
                default:
                    String message = "";
                    for (String string : arr)
                        message += string + " ";
                    errorOccured(message);
                    return ERROR;
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            errorOccured(String.format("Failed to process: %s\nInvalid argument length", line));
            return ERROR;
        }
    }

    //TODO: implement setting me in ui
    protected void initTrader(String traderid)
    {
        ClientHandler.trader.setAccountID(traderid);
        ClientProgram.ui.addTrader(traderid);
        System.out.println("Account " + trader.getTraderID());
        ClientProgram.ui.setTraderID(traderid);
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
        ClientProgram.ui.setStockHolder(arr[1]);
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

    protected void tradeSucc(String trader1, String trader2)
    {
        ClientProgram.ui.addMessage(Message.tradeUI(trader1, trader2));
        Market.attainedStock(new Trader(trader2));
        ClientProgram.ui.setStockHolder(trader2);
    }

    protected void init(Socket socket) throws Exception{
        this.printWriter = new PrintWriter(socket.getOutputStream(), true);
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
