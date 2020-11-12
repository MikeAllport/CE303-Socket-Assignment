package Server;

public enum Message
{
    TRADER_LIST("TRADERS "), ERROR("ERROR "),
    TRADE_FAIL("TRADE_FAIL "), TRADER_JOINED("TRADER_JOIN "), TRADER_LEFT("TRADER_LEFT "),
    TRADER_ACQ_STOCK("TRADER_ACQ_STOCK "), TRADER_ID("YOUR_ID "), TRADER_TRADE("TRADER_TRADE");

    private final String label;

    private Message(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }

    public static boolean isEnum(String toCheck)
    {
        try
        {
            Message.valueOf(toCheck);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}