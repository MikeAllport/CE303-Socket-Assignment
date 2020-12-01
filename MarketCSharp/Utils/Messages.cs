using System;
using static Utils.MessageEnums;

namespace Utils
{
    /**
     * MessageEnums purpose is to stor constants relating to the server/client
     * protocol
     */
    public enum MessageEnums
    {
        TRADER_LIST, ERROR, TRADE_FAIL, TRADER_JOINED, TRADER_LEFT,
        TRADER_ACQ_STOCK, TRADER_ID, TRADER_TRADE,
        TRADER_WITH_STOCK, TRADER_RECONNECTING, TRADER_NEW, 
        SERVER_RESTORED, SERVER_REBOOT, CONNECT_SUCC, TRADE_SUCC
    }

    /**
     * Message's purpose is to control all logic assertaining to messages sent between
     * Client and Server so that messages are uniform accross both applications
     */
    public static class Message
    {
        public static bool IsMessageEnum(string input)
        {
            try
            {
                GetMessageEnumFromString(input);
                return true;
            }
            catch (Exception e)
            {
                return false;
            }
        }

        public static MessageEnums GetMessageEnumFromString(string input)
        {
            return (MessageEnums)Enum.Parse(typeof(MessageEnums), input.ToUpper(), true);
        }

        public static String allTradersBroadCast(String traders)
        {
            return $"{TRADER_LIST.ToString()} {traders}";
        }

        public static String traderJoincedBroadCast(String trader)
        {
            return $"{TRADER_JOINED.ToString()} {trader}";
        }

        public static String traderJoinedUI(String trader)
        {
            return $"{trader} joined the server";
        }

        public static String traderWithStockBroadCast(String trader)
        {
            return $"{TRADER_WITH_STOCK.ToString()} {trader}";
        }

        public static String tradeBroadCast(String t1, String t2)
        {
            return $"{TRADE_SUCC.ToString()} {t1} {t2}";
        }

        public static String tradeUI(String t1, String t2)
        {
            return $"{t1} traded stock to {t2}";
        }

        public static String tradeFailBroadCast(String message)
        {
            return $"{TRADE_FAIL.ToString()} {message}";
        }

        public static String traderIDBroadCast(String id)
        {
            return $"{TRADER_ID.ToString()} {id}";
        }

        public static String error(String message)
        {
            return $"{ERROR.ToString()} {message}";
        }

        public static String traderLeftUI(String trader)
        {
            return $"{trader} left.";
        }

        public static String traderLeftBroadCast(String trader)
        {
            return $"{TRADER_LEFT.ToString()} {trader}";
        }

        public static String traderAcqUI(String trader)
        {
            return $"{trader} acquired the stock automatically";
        }

        public static String traderAcqBroadCast(String trader)
        {
            return $"{TRADER_ACQ_STOCK.ToString()} {trader}";
        }
    
        public static String serverRebootBroadcast() 
        { 
            return $"{SERVER_REBOOT.ToString()} "; 
        }

        public static String serverRebookUI() 
        { 
            return "Server is rebooting..."; 
        }

        public static String traderReconnectingBroadcast(String traderID) 
        { 
            return $"{TRADER_RECONNECTING.ToString()} {traderID}"; 
        }

        public static String connectSuccessful() 
        { 
            return $"{CONNECT_SUCC.ToString()} "; 
        }

        public static String newTraderBroadcast() 
        { 
            return $"{TRADER_NEW.ToString()} "; 
        }

        public static String serverRecoveredBroadCast() 
        { 
            return $"{SERVER_RESTORED.ToString()} "; 
        }

        public static String makeTrade(string traderID)
        {
            return $"{TRADER_TRADE.ToString()} {traderID}";
        }

        public static String serverRecoveredUI() 
        { 
            return "Server recovered from a reboot"; 
        }
    }
}
