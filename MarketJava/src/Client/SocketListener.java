package Client;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class SocketListener implements Runnable{
    private final Scanner scanner;
    private final ClientHandler handler;


    public SocketListener(Scanner scanner, ClientHandler handler)
    {
        this.scanner = scanner;
        this.handler = handler;
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run()
    {
        try {
            while (true) {
                String line = scanner.nextLine();
                this.handler.processLine(line);
            }
        } catch (NoSuchElementException e)
        {
            ClientProgram.socketClosed = true;
        }
    }
}
