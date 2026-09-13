package core;

public class TCPChannelDebugProxy implements Channel
{
    private final TCPChannel tcpChannel;

    public TCPChannelDebugProxy(TCPChannel tcpChannel)
    {
        this.tcpChannel = tcpChannel;
    }

    @Override
    public boolean hasConnection() {
        return tcpChannel.hasConnection();
    }

    @Override
    public Connection getConnection() {
        return tcpChannel.getConnection();
    }

    @Override
    public void start() {
        Debug.Log(DebugType.INFO, "Opening of a TCP channel");
        tcpChannel.start();
    }

    @Override
    public void stop() {
        Debug.Log(DebugType.INFO, "Closing of a TCP channel");
        tcpChannel.stop();
    }
}
