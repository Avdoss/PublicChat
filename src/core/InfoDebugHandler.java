package core;

public class InfoDebugHandler extends DebugHandler
{
    @Override
    public void handleRequest(DebugType type, String message)
    {
        if (type == DebugType.INFO)
            System.out.println("[INFO] " + message);
        else if (nextDebugHandler != null)
            nextDebugHandler.handleRequest(type, message);
    }
}
