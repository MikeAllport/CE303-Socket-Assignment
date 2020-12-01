package Server;

public class TraderHandlerStub extends TraderHandler{

    TraderHandlerStub(Market market)
    {
        super(null);
    }

    @Override
    protected void sendMessage(String message) {
    }
}
