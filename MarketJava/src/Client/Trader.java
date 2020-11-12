package Client;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Trader {
    private String traderID;
    private boolean hasStock;

    public Trader()
    {
        this.hasStock = false;
    }

    public void setAccountID(String no)
    {
        this.traderID = no;
    }
}
