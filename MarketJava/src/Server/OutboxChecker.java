package Server;

import java.util.ArrayList;

/**
 * OutboxChecker's sole responsibility is for checking if a trader has any messages in their outbox,
 * which is populated through server broadcasts, and if there are any messages it sends them through to the
 * associated handler's sendMessage function
 */
public class OutboxChecker implements Runnable {
    ClientHandler handler;
    public OutboxChecker(ClientHandler handler)
    {
        this.handler = handler;
    }

    @Override
    public void run(){
        while(handler.getTrader() != null && handler.getTrader().deathIndicator.get() == 0)
        {
            Trader trader = handler.getTrader();
            ArrayList<String> messages = trader.getMessages();
            for (String message: messages)
            {
                handler.sendMessage(message);
            }
            messages.clear();
            try {
                Thread.sleep(100);
            } catch (Exception e)
            {
                handler.startOutboxListener();
            }
        }
    }
}
