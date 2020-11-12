package Client;

public class ServerIrreparableException extends Exception{
    public ServerIrreparableException(String ID, String cause)
    {
        super("Server failed during operation. Attempted to restore 3 times unsuccessfully");
    }
}
