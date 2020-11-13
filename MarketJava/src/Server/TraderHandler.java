package Server;

import Utils.*;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import static Server.Message.*;

public class TraderHandler implements Runnable{
    protected Scanner scanner;
    protected PrintWriter printWriter;

    // had to change trader to protected for testing purposes
    protected Trader trader = null;
    private final Socket socket;
    private final Market market;

    public TraderHandler(Socket socket, Market market)
    {

        this.market = market;
        this.socket = socket;
    }

    protected synchronized Message processLine(String line)
    {
        String arr[] = line.split(" ");
        if (arr.length <= 0 || !isEnum(arr[0]))
        {
            sendMessage(Message.error(line + " is not a valid server request"));
            return ERROR;
        }
        if (valueOf(arr[0]) == TRADER_TRADE) {
            if (arr.length != 2)
            {
                sendMessage(Message.error("'" + line + "' has incorrect argument number, 1 id required"));
                return ERROR;
            }
            return tradeStock(arr[1]);
        }
        sendMessage(Message.error("'" + line + "'" + " unrecognised command"));
        return ERROR;
    }

    private Message tradeStock(String otherTraderID)
    {
        if (this.trader == null)
            return null;
        if (this.trader != market.getCurrentStockHolder())
        {
            sendMessage(Message.error("you do not have the stock to give away!"));
            return TRADE_FAIL;
        }
        Pair<Object, ArrayList<Trader>> traders = Market.getTraders();
        synchronized (traders.first()) {
            Trader otherTrader = null;
            for (Trader trader1: traders.second())
            {
                if (trader1.getID().equals(otherTraderID))
                    otherTrader = trader1;
            }
            if (otherTrader != null && otherTrader.deathIndicator.get() == 0)
            {
                this.market.setStockHolder(otherTrader);
                broadcast(Message.tradeBC(trader.getID(), otherTraderID));
                ServerProgram.ui.addMessage(Message.tradeUI(this.trader.getID(), otherTraderID));
                return TRADE_SUCC;
            }
            else
            {
                sendMessage(Message.tradeFailBC(otherTraderID + " is invalid, or other client disconnected. " +
                        "Please try again"));
                return TRADE_FAIL;
            }
        }
    }

    protected void killTrader()
    {
        if (trader == null)
            return;
        this.trader.deathIndicator.incrementAndGet();
        broadcast(Message.traderLeftBC(this.trader.getID()));
        Market.removeTrader(this.trader);
    }

    private void initTrader()
    {
        Pair<ArrayList<String>, Trader> tradersAndMe = Market.newTrader();
        this.trader = tradersAndMe.second();
        String allTraders = "";
        for (String traderID: tradersAndMe.first())
            allTraders += traderID + " ";
        sendMessage(Message.traderIDBC(this.trader.getID()));
        sendMessage(Message.allTradersBC(allTraders));
        Trader stockHolder = Market.getCurrentStockHolder();
        sendMessage(Message.traderWithStockBC((stockHolder != null)? stockHolder.getID(): "null"));
        broadcast(Message.traderJoincedBC(trader.getID()));
        ServerProgram.ui.addMessage(Message.traderJoinedUI(trader.getID()));
        ServerProgram.ui.addTrader(trader.getID());
    }

    private void mainLoop()
    {
        while (true)
        {
            String line = this.scanner.nextLine();
            processLine(line);
        }
    }

    protected void sendMessage(String message)
    {
        printWriter.println(message);
    }

    protected static void broadcast(String message)
    {
        Pair<Object, ArrayList<Trader>> theTradersWLock = Market.getTraders();
        synchronized (theTradersWLock.first())
        {
            for (Trader trader: theTradersWLock.second())
                trader.addMessage(message);
        }
    }

    public Trader getTrader()
    {
        return trader;
    }

    public void startOutboxListener()
    {
        new Thread(new OutboxChecker(this)).start();
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
            initTrader();
            startOutboxListener();
            mainLoop();
        } catch (Exception ignore) {
        }
        finally
        {
            if (this.trader != null)
            {
                killTrader();
            }
        }
    }

}
