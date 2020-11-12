package Server;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Trader {
    protected AtomicInteger deathIndicator = new AtomicInteger(0);
    private String traderID;
    private ArrayList<String> outbox;
    private Object outboxLock;


    public Trader(String id)
    {
        this.traderID = id;
        this.outbox = new ArrayList<>();
        this.outboxLock = new Object();
    }

    public void addMessage(String message)
    {
        synchronized (outboxLock)
        {
            this.outbox.add(message);
        }
    }

    public ArrayList<String> getMessages()
    {
        ArrayList<String> messages = new ArrayList<>();
        synchronized (outbox)
        {
            messages.addAll(outbox);
            this.outbox.clear();
        }
        return messages;
    }

    public String getID()
    {
        return traderID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Trader)
        {
            Trader other = (Trader) obj;
            return other.traderID == traderID;
        }
        return false;
    }
}
