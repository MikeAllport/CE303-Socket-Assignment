package Server;

import Utils.ListLock;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Trader's purpose is to contain a traders id, logic for adding messages to its outbox which is populated through
 * the handlers static broadcast method, and contain a death indicator which is only checked when trading stock in the
 * market
 */
public class Trader{
    protected AtomicInteger deathIndicator = new AtomicInteger(0);
    protected boolean reconnected = false;
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
        if (!(obj instanceof Trader))
            return false;
        Trader other = (Trader) obj;
        if (traderID == null || other.traderID == null)
            return false;
        return other.traderID.toUpperCase().equals(traderID.toUpperCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(traderID.toUpperCase());
    }

    @Override
    public String toString() {
        return traderID;
    }
}
