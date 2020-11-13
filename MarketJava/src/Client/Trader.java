package Client;

import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Trader implements Serializable {
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
