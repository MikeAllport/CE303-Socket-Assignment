package Client;

import Utils.Message;
import Utils.Pair;

public class ClientHandlerStub extends ClientHandler{
    Pair<Message, String> serverResponse;


    @Override
    protected Pair<Message, String> processLine(String line) {
        Pair<Message, String> result =  super.processLine(line);
        serverResponse = result;
        System.out.println(result.second());
        return result;
    }

    void printResult() {
        System.out.println(serverResponse.second());
    }
}
