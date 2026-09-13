package core;

public class WarningDebugHandler extends DebugHandler
{
    @Override
    public void handleRequest(DebugType type, String message)
    {
        if (type == DebugType.WARNING)
            System.out.println("[WARNING] " + message);
        else if (nextDebugHandler != null)
            nextDebugHandler.handleRequest(type, message);
    }
}

