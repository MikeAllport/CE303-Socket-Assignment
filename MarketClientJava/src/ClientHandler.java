public class ClientHandler implements Runnable{
    private Client client;
    private static final String DELIM = " ";
    ClientProgram program;
    enum Messages { ERROR, ACCOUNT, TRADE_SUCC, TRADE_FAIL, TRADE_JOINED,
    TRADER_LEFT, TRADER_ACQ_STOCK }

    private void sendMessage(Client client, String message)
    {
        client.printWriter.println(message);
    }

    private void listen() throws InvalidAccountIDException
    {
        while (true)
        {
            String line = client.reader.nextLine();
            processLine(line);
        }
    }

    private Messages processLine(String line) throws InvalidAccountIDException
    {
        String arr[] = line.split(DELIM);
        Long id;
        switch (arr[0].toUpperCase())
        {
            case "ACC":
                id = checkLongError(arr[1], "Account login");
                client.setAccountID(id);
                System.out.println("Account " + client);
                return Messages.ACCOUNT;
            case "TRADE_SUCC":
                System.out.println("You acquired stock");
                return Messages.TRADE_SUCC;
            case "TRADE_FAIL":
                System.out.println("Could not acquire stock");
                return Messages.TRADE_FAIL;
            case "TRADER_LEFT":
                id = checkLongError(arr[1], "Trader leaving");
                return Messages.TRADER_LEFT;
            case "TRADER_JOINED":
                id = check
                return Messages.TRADE_JOINED;
            case "TRADER_ACQ_STOCK":
                return Messages.TRADER_ACQ_STOCK;
            default:
                return Messages.ERROR;
        }
    }

    @Override
    public void run(){
        try {
            this.client = new Client();
            listen();
        }
        catch (InvalidAccountIDException e)
        {
            System.out.println("Invalid server response of:\n" + e.getMessage());
        }
        catch (Exception e)
        {
            System.out.println("Server crashed\n");
            e.printStackTrace();
        }
    }
}
