package Client;

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
    public void run()
    {
        while(true)
        {
            String line = scanner.nextLine();
            this.handler.processLine(line);
        }
    }
}
