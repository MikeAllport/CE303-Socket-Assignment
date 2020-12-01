package Server;

import Utils.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import static Utils.Message.*;

public class TraderHandler implements Runnable{
    protected Scanner scanner;
    protected PrintWriter printWriter;

    // had to change trader to protected for testing purposes
    protected Trader trader = null;
    private final Socket socket;

    public TraderHandler(Socket socket)
    {
        this.socket = socket;
    }

    @Override
    public void run() {
        try
        {
            try {
                scanner = new Scanner(socket.getInputStream());
                printWriter = new PrintWriter(socket.getOutputStream(), true);
            }
            catch (Exception e)
            {
                throw new Exception("Failed to create socket streams");
            }
            sendWelcomeMessage();
            startListening();
        } catch (Exception ignore) {
        }
        finally {
            killTrader();
            closeSocket();
        }
    }

    // sends initial message to client, CONN_SUCC or SERVER_REBOOT
    private void sendWelcomeMessage() {
        if (ServerProgram.serverRestarting) {
            ServerProgram.ui.addMessage(Message.serverRebootUI());
            sendMessage(Message.serverRebootBroadcast());
        } else {
            sendMessage(Message.connectSuccessful());
        }
    }

    // main socket reader loop
    private void startListening() throws Exception
    {
        while (true)
        {
            String line = this.scanner.nextLine();
            processLine(line);
        }
    }

    // attempts to close the active socket
    protected void closeSocket()
    {
        try {
            socket.close();
        } catch (IOException ignore) {}
    }

    // kicks server from market, sending according trader left messages
    protected void killTrader()
    {
        if (this.trader == null)
            return;
        this.trader.deathIndicator.incrementAndGet();
        broadcast(Message.traderLeftBroadCast(this.trader.getID()));
        ServerProgram.ui.addMessage(Message.traderLeftUI(trader.getID()));
        ServerProgram.ui.removeTrader(trader.getID());
        Market.removeTrader(this.trader);
    }

    // main line filtering loop to determine what messages have been received
    protected synchronized Message processLine(String line) throws Exception
    {
        String arr[] = line.split(" ");
        // error case
        if (arr.length <= 0 || !isEnum(arr[0]))
        {
            sendMessage(Message.error(line + " is not a valid server request"));
            return ERROR;
        }
        try
        {
            // main acceptable messages
            switch (valueOf(arr[0]))
            {
                case TRADER_RECONNECTING:
                    if (!(ServerProgram.serverRestarting))
                    {
                        sendMessage(Message.error("Server not restarting, you are disconnected"));
                        return ERROR;
                    }
                    initNewTrader(Market.getReturningTrader(arr[1]));
                    return TRADER_RECONNECTING;
                case TRADER_NEW:
                    initNewTrader(Market.getNewTrader());
                    return TRADER_NEW;
                case TRADER_TRADE:
                    if (trader == null)
                    {
                        sendMessage(Message.error("Have not initialized yourself as a trader"));
                        return ERROR;
                    }
                    return tradeStock(arr[1]);
            }
        } catch (ArrayIndexOutOfBoundsException ignore){}
        sendMessage(Message.error("'" + line + "'" + " unrecognised command"));
        return ERROR;
    }

    // initiates the new trader, sending messages accordingly to client with their id,
    // the stock holder, the list of traders, and broadcasting trader joined
    private void initNewTrader(Pair<ArrayList<String>, Trader> tradersListAndNewTrader)
    {
        this.trader = tradersListAndNewTrader.second();
        // required so new trader doesn't get kicked out if server restarting
        if (ServerProgram.serverRestarting)
            trader.reconnected = true;
        sendMessage(Message.traderIDBroadCast(this.trader.getID()));
        sendOtherTraders(tradersListAndNewTrader.first());
        Trader stockHolder = Market.getCurrentStockHolder();
        sendMessage(Message.traderWithStockBroadCast((stockHolder != null)? stockHolder.getID(): "null"));
        broadcast(Message.traderJoincedBroadCast(trader.getID()));
        ServerProgram.ui.addMessage(Message.traderJoinedUI(trader.getID()));
        ServerProgram.ui.addTrader(trader.getID());
        startOutboxListener();
    }

    // formats the message required for TRADER_LIST
    private void sendOtherTraders(ArrayList<String> traderIdList)
    {
        String allTraders = "";
        for (String traderID: traderIdList)
            allTraders += traderID + " ";
        sendMessage(Message.allTradersBroadCast(allTraders));
    }

    // thread responsible for checking a traders outbox and sending messages
    public void startOutboxListener()
    {
        new Thread(new OutboxChecker(this)).start();
    }

    // the trade stock with other trader function
    private Message tradeStock(String otherTraderID)
    {
        // this trader doesnt have the stock
        if (this.trader != Market.getCurrentStockHolder())
        {
            sendMessage(Message.tradeFailBroadCast("you do not have the stock to give away!"));
            return TRADE_FAIL;
        }

        // make trade
        synchronized (Market.traders.getLock()) {
            // fetch other trader
            Trader otherTrader = null;
            for (Trader trader1: Market.traders.getList())
            {
                if (trader1.getID().toUpperCase().equals(otherTraderID.toUpperCase()))
                    otherTrader = trader1;
            }
            // make trade if trader found
            if (otherTrader != null && otherTrader.deathIndicator.get() == 0)
            {
                Market.setStockHolder(otherTrader);
                broadcast(Message.tradeBroadCast(trader.getID(), otherTraderID));
                ServerProgram.ui.addMessage(Message.tradeUI(this.trader.getID(), otherTraderID));
                System.out.println(Message.tradeUI(this.trader.getID(), otherTraderID));
                return TRADE_SUCC;
            }
            // trader not found, or disconnected, send error
            else
            {
                sendMessage(Message.tradeFailBroadCast(otherTraderID + " is invalid, or other client disconnected. " +
                        "Please try again"));
                return TRADE_FAIL;
            }
        }
    }

    // sends a message to the client
    protected void sendMessage(String message)
    {
        printWriter.println(message);
    }

    // sends a message to all other traders
    protected static void broadcast(String message)
    {
        synchronized (Market.traders.getLock())
        {
            for (Trader trader: Market.traders.getList())
                trader.addMessage(message);
        }
    }

    public Trader getTrader()
    {
        return trader;
    }

}
