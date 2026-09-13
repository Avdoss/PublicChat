package core;

public class Debug
{
    static private DebugHandler firstDebugHandler;

    public static void addDebugHandler(DebugHandler handler)
    {
        handler.setNextDebugHandler(firstDebugHandler);
        firstDebugHandler = handler;
    }

    public static void Log(DebugType type, String message)
    {
        // chain of responsibility
        if (firstDebugHandler != null)
            firstDebugHandler.handleRequest(type, message);
    }
}
