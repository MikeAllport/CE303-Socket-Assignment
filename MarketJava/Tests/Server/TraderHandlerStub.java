package Server;

public class TraderHandlerStub extends TraderHandler{

    TraderHandlerStub(Market market)
    {
        super(null, market);
    }

    @Override
    protected void sendMessage(String message) {
    }
}
