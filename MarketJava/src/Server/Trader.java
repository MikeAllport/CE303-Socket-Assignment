package Server;

import Utils.ListLock;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Trader {
    protected AtomicInteger deathIndicator = new AtomicInteger(0);
    private String traderID;
    private ListLock<String> outbox;


    public Trader(String id)
    {
        this.traderID = id;
        this.outbox = new ListLock<>();
    }

    public void addMessage(String message)
    {
        synchronized (outbox.getLock())
        {
            this.outbox.getList().add(message);
        }
    }

    public ArrayList<String> getMessages()
    {
        ArrayList<String> messages = new ArrayList<>();
        synchronized (outbox.getLock())
        {
            messages.addAll(outbox.getList());
            this.outbox.getList().clear();
        }
        return messages;
    }

    public String getID()
    {
        return traderID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof Trader)
        {
            Trader other = (Trader) obj;
            return other.traderID.toUpperCase().equals(traderID.toUpperCase());
        }
        return false;
    }
}
