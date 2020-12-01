package Client;

import Server.Market;
import Utils.Message;
import Utils.Pair;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import static Utils.Message.*;

public class ClientHandler {
    private static final String DELIM = " ";
    protected static Trader trader = null;
    protected boolean serverRestarting = false;

    private PrintWriter printWriter;

    public ClientHandler()
    {
    }

    protected void init(Socket socket) throws Exception{
        this.printWriter = new PrintWriter(socket.getOutputStream(), true);
        Scanner scanner = new Scanner(socket.getInputStream());
        if (ClientHandler.trader == null)
            ClientHandler.trader = new Trader();
        new Thread(new SocketListener(scanner, this)).start();
    }

    public void sendMessage(String message)
    {
        if (printWriter == null)
            return;
        if (serverRestarting)
        {
            ClientProgram.ui.addMessage(String.format("Server reconnecting, cannot send message",
                    message));
            return;
        }
        this.printWriter.println(message);
    }

    protected void trade(String traderID)
    {
        sendMessage(TRADER_TRADE.getLabel() + traderID);
    }

    protected Pair<Message, String> processLine(String line)
    {
        String arr[] = line.split(DELIM);
        if (arr.length <= 0)
            return new Pair<>(ERROR, line);
        if (!isEnum(arr[0]))
        {
            arr[0] = "ERROR";
        }
        try
        {
            switch (valueOf(arr[0]))
            {
                case CONNECT_SUCC:
                    sendMessage(Message.newTraderBroadcast());
                    return new Pair<>(CONNECT_SUCC, line);
                case SERVER_REBOOT:
                    serverRestarting = true;
                    ClientProgram.ui.addMessage(Message.serverRebootUI());
                    if (ClientHandler.trader == null)
                        sendMessage(Message.newTraderBroadcast());
                    else
                        sendMessage(TRADER_RECONNECTING.getLabel() + ClientHandler.trader.getTraderID());
                    return new Pair<>(SERVER_REBOOT, line);
                case SERVER_RESTORED:
                    serverRestarting = false;
                    ClientProgram.ui.addMessage(Message.serverRecoveredUI());
                    return new Pair<>(SERVER_RESTORED, line);
                case TRADER_ID:
                    initTrader(arr[1]);
                    return new Pair<>(TRADER_ID, line);
                case TRADER_LIST:
                    if (arr.length > 0)
                        traderList(arr);
                    return new Pair<>(TRADER_LIST, line);
                case TRADER_WITH_STOCK:
                    ClientProgram.ui.setStockHolder(arr[1]);
                    return new Pair<>(TRADER_WITH_STOCK, line);
                case TRADER_JOINED:
                    traderJoined(arr[1]);
                    return new Pair<>(TRADER_JOINED, line);
                case TRADER_LEFT:
                    traderLeft(arr[1]);
                    return new Pair<>(TRADER_LEFT, line);
                case TRADE_SUCC:
                    tradeSucc(arr[1], arr[2]);
                    return new Pair<>(TRADE_SUCC, line);
                case TRADE_FAIL:
                    errorOccured(arr);
                    return new Pair<>(TRADE_FAIL, line);
                case TRADER_ACQ_STOCK:
                    traderAcqStock(arr[1]);
                    return new Pair<>(TRADER_ACQ_STOCK, line);
                default:
                    errorOccured(arr);
                    return new Pair<>(ERROR, line);
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            errorOccured(arr);
            return new Pair<>(ERROR, line);
        }
    }

    protected void initTrader(String traderid)
    {
        ClientHandler.trader.setAccountID(traderid);
        ClientProgram.ui.addTrader(traderid);
        System.out.println("Account " + trader.getTraderID());
        ClientProgram.ui.setTraderID(traderid);
    }

    protected void traderList(String[] arr)
    {
        String stateOfMarket = "Other Traders:\n";
        ClientProgram.ui.addMessage(stateOfMarket);
        for (int i = 1; i < arr.length; i++)
        {
            if (!(arr[i].equals(ClientHandler.trader.getTraderID())))
            {
                ClientProgram.ui.addTrader(arr[i]);
                stateOfMarket += arr[i] + "\n";
                ClientProgram.ui.addMessage(arr[i]);
            }
        }
        stateOfMarket += "End of list";
        ClientProgram.ui.addMessage("End of list");
        System.out.println(stateOfMarket);
    }

    protected void errorOccured(String[] message)
    {
        String messageJoined = "";
        for (String string : message)
            messageJoined += string + " ";
        ClientProgram.ui.addMessageError(messageJoined);
        System.out.println(messageJoined);
    }

    //TODO: add change of stock
    protected void traderAcqStock(String traderID)
    {
        String message = Message.traderAcqUI(traderID);
        ClientProgram.ui.addMessage(message);
        ClientProgram.ui.setStockHolder(traderID);
        System.out.println(message);
    }

    protected void traderLeft(String traderID)
    {
        if (traderID.equals(ClientHandler.trader.getTraderID()))
            return;
        ClientProgram.ui.removeTrader(traderID);
        String message = Message.traderLeftUI(traderID);
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
        ClientProgram.ui.setStockHolder(trader2);
        System.out.println(Message.tradeUI(trader1, trader2));
    }
}
