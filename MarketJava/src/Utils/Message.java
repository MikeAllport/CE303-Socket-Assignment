package Utils;

/**
 * Messages purpose is to enumerate all server/client communication messages so that messages are uniform
 * across both applications. Class contains static methods to retrieve the desired string messages for
 * a given enum.
 *
 * Methods named xBroadcast(...) are used for sending messages to client/server
 * Methods named xUI(...) are used for displaying messages in the ui
 */
public enum Message
{
    CONNECT_SUCC("CONNECT_SUCC "),SERVER_REBOOT("SERVER_REBOOT "),

    TRADER_NEW("TRADER_NEW "), TRADER_RECONNECTING("TRADER_RECONNECTING "),

    TRADER_LIST("TRADER_LIST "), TRADER_ID("TRADER_ID "), TRADER_WITH_STOCK("TRADER_WITH_STOCK "),
    TRADER_JOINED("TRADER_JOINED "),

    TRADER_TRADE("TRADER_TRADE "), TRADE_SUCC("TRADE_SUCC "), TRADE_FAIL("TRADE_FAIL "),

    TRADER_LEFT("TRADER_LEFT "), TRADER_ACQ_STOCK("TRADER_ACQ_STOCK "),
    SERVER_RESTORED("SERVER_RESTORED "), ERROR("ERROR ");

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

    public static String allTradersBroadCast(String traders)
    {
        return TRADER_LIST.getLabel() + traders;
    }

    public static String traderJoincedBroadCast(String trader)
    {
        return TRADER_JOINED.getLabel() + trader;
    }

    public static String traderJoinedUI(String trader)
    {
        return String.format("%s joined the server", trader);
    }

    public static String traderWithStockBroadCast(String trader)
    {
        return TRADER_WITH_STOCK.getLabel() + trader;
    }

    public static String tradeBroadCast(String t1, String t2)
    {
        return TRADE_SUCC.getLabel() + t1 + " " + t2;
    }

    public static String tradeUI(String t1, String t2)
    {
        return String.format("%s traded stock to %s", t1, t2);
    }

    public static String tradeFailBroadCast(String message)
    {
        return TRADE_FAIL.getLabel() + message;
    }

    public static String traderIDBroadCast(String id)
    {
        return TRADER_ID.getLabel() + id;
    }

    public static String error(String message)
    {
        return ERROR.getLabel() + message;
    }

    public static String traderLeftUI(String trader)
    {
        return trader + " left.";
    }

    public static String traderLeftBroadCast(String trader)
    {
        return TRADER_LEFT.getLabel() + trader;
    }

    public static String traderAcqUI(String trader)
    {
        return trader + " acquired the stock automatically";
    }

    public static String traderAcqBroadCast(String trader)
    {
        return TRADER_ACQ_STOCK.getLabel() + trader;
    }

    public static String serverRebootBroadcast() { return SERVER_REBOOT.getLabel(); }

    public static String serverRebootUI() { return "Server is rebooting..."; }

    public static String traderReconnectingBroadcast(String traderID) { return TRADER_RECONNECTING.getLabel() + traderID; }

    public static String connectSuccessful() { return CONNECT_SUCC.getLabel(); }

    public static String newTraderBroadcast() { return TRADER_NEW.getLabel(); }

    public static String serverRecoveredBroadCast() { return SERVER_RESTORED.getLabel(); }

    public static String serverRecoveredUI() { return "Server recovered from a reboot"; }
}