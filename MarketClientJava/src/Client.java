import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private long traderID;
    private boolean hasStock = false;
    Scanner reader;
    PrintWriter printWriter;

    public Client() throws Exception
    {
        Socket socket = new Socket(ClientProgram.ADDRESS, ClientProgram.PORT);
        this.reader = new Scanner(socket.getInputStream());
        this.printWriter = new PrintWriter(socket.getOutputStream());
    }

    public void setAccountID(Long no)
    {
        this.traderID = no;
    }
}
