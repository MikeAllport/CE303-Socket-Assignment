package Client;

public class Trader {
    private String traderID;

    public Trader()
    {
    }

    public Trader(String id)
    {
        this.traderID = id;
    }

    public String getTraderID()
    {
        return this.traderID;
    }

    public void setAccountID(String no)
    {
        this.traderID = no;
    }
}
