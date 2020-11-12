package Server;

import java.util.ArrayList;

public class OutboxChecker implements Runnable {
    TraderHandler handler;
    public OutboxChecker(TraderHandler handler)
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
