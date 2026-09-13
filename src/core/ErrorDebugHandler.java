package core;

public class ErrorDebugHandler extends DebugHandler
{
    @Override
    public void handleRequest(DebugType type, String message)
    {
        if (type == DebugType.ERROR)
            System.out.println("[ERROR] " + message);
        else if (nextDebugHandler != null)
            nextDebugHandler.handleRequest(type, message);
    }
}

