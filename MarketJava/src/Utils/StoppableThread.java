package Utils;

/**
 * Simple stoppable thread including a static running boolean.
 * Intention is that the Runnable implementer will use this variable in a
 * while loop and the instantiating class can call stop() method to exit loop.
 * This is meant as a singleton class, however given using a runnable this would not
 * be possible. Implementors should be aware running is static
 * Only used in testing
 */
public class StoppableThread implements Runnable{
    public static boolean running;
    Runnable runnable;

    /**
     * Constructor instantiates
     * @param runnable
     */
    public StoppableThread(Runnable runnable)
    {
        StoppableThread.running = true;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        runnable.run();
    }

    public void stop()
    {
        StoppableThread.running = false;
    }
}
