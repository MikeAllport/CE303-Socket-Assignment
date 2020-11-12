public class ClientProgram {
    final static String ADDRESS = "localhost";
    final static int PORT = 8888;

    public static void main(String[] args) {
        new Thread(new ClientHandler()).start();
    }
}
