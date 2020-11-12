public class InvalidAccountIDException extends Exception{
    public InvalidAccountIDException(String ID, String cause)
    {
        super("Un-parsable account id given of '" + ID +"' sent during " + cause);
    }
}
