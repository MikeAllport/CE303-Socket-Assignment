package Server;

import javafx.util.Pair;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import static Server.Message.*;

public class TraderHandler implements Runnable{
    protected Scanner scanner;
    protected PrintWriter printWriter;

    private Trader trader = null;
    private final Socket socket;
    private final Market market;

    public TraderHandler(Socket socket, Market market)
    {

        this.market = market;
        this.socket = socket;
    }

    synchronized void processLine(String line)
    {
        String arr[] = line.split(" ");
        if (!isEnum(arr[0]))
        {
            sendMessage(ERROR.getLabel() + line + " is not a valid server request");
            return;
        }
        switch(Message.valueOf(arr[0]))
        {
            case TRADER_TRADE:
                tradeStock(arr[1]);
        }
    }

    private void tradeStock(String otherTraderID)
    {
        if (this.trader != market.getCurrentStockHolder())
        {
            sendMessage(ERROR.getLabel() + "you do not have the stock to give away!");
            return;
        }
        Pair<Object, ArrayList<Trader>> traders = Market.getTraders();
        synchronized (traders.getKey()) {
            Trader otherTrader = null;
            for (Trader trader1: traders.getValue())
            {
                if (trader1.getID().equals(otherTraderID))
                    otherTrader = trader1;
            }
            if (otherTrader != null && otherTrader.deathIndicator.get() == 0)
            {
                this.market.setStockHolder(otherTrader);
                broadcast(TRADER_ACQ_STOCK.getLabel() + otherTrader.getID());
                ServerProgram.ui.addMessage(this.trader.getID() + " traded stock to " + otherTraderID);
            }
            else
            {
                sendMessage(TRADE_FAIL.getLabel() + otherTraderID + " is invalid, or other client disconnected. " +
                        "Please try again");
            }
        }
    }

    protected void killTrader()
    {
        this.trader.deathIndicator.incrementAndGet();
        Pair<Object, ArrayList<Trader>> traders = Market.getTraders();
        synchronized (traders.getKey()) {
            traders.getValue().remove(this.trader);

        }
        broadcast(TRADER_LEFT.getLabel() + trader.getID());
        ServerProgram.ui.addMessage(this.trader.getID() + " left.");
        ServerProgram.ui.removeTrader(this.trader.getID());
    }

    private void initTrader()
    {
        Pair<ArrayList<String>, Trader> tradersAndMe = Market.newTrader();
        this.trader = tradersAndMe.getValue();
        String allTraders = TRADER_LIST.getLabel();
        for (String traderID: tradersAndMe.getKey())
            allTraders += traderID + " ";
        sendMessage(TRADER_ID.getLabel() + this.trader.getID());
        sendMessage(allTraders);
        broadcast(TRADER_JOINED.getLabel() + trader.getID());
        ServerProgram.ui.addMessage(String.format("Trader %s joined the server", trader.getID()));
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

    private static void broadcast(String message)
    {
        Pair<Object, ArrayList<Trader>> theTradersWLock = Market.getTraders();
        synchronized (theTradersWLock.getKey())
        {
            for (Trader trader: theTradersWLock.getValue())
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
