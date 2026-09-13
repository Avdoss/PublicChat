package core;

public abstract class DebugHandler
{
    protected DebugHandler nextDebugHandler;

    public void setNextDebugHandler(DebugHandler debugLayer)
    {
        nextDebugHandler = debugLayer;
    }

    public abstract void handleRequest(DebugType type, String message);
}
