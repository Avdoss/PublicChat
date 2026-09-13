package core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class TCPChannel implements Channel
{
    private final AtomicBoolean enable;
    private final int port;
    private Thread thread;
    private final ConcurrentLinkedQueue<Connection> newConnections;

    public TCPChannel(int port)
    {
        this.enable = new AtomicBoolean();
        this.port = port;
        newConnections = new ConcurrentLinkedQueue<>();
    }

    public boolean hasConnection()
    {
        return !newConnections.isEmpty();
    }

    public Connection getConnection()
    {
        return newConnections.poll();
    }

    public void start()
    {
        if (!enable.get())
        {
            enable.set(true);
            thread = new Thread(this::run);
            thread.start();
        }
    }

    public void stop()
    {
        if (enable.get())
        {
            enable.set(false);
            try {
                thread.join();
            } catch (InterruptedException e) {
                Debug.Log(DebugType.WARNING, "TCP channel was closed incorrectly");
            }
            while (hasConnection())
            {
                Connection connection = getConnection();
                connection.close();
            }
        }
    }

    private void run()
    {
        try(ServerSocketChannel serverSocket = ServerSocketChannel.open())
        {
            serverSocket.socket().bind(new InetSocketAddress(port));
            serverSocket.configureBlocking(false);
            Debug.Log(DebugType.INFO, "TCP channel was established successfully");
            while(enable.get())
            {
                SocketChannel socket = null;
                try {
                    socket = serverSocket.accept();
                    if (socket != null)
                    {
                        socket.configureBlocking(false);
                        Connection connection = new TCPConnectionAdapter(socket);
                        newConnections.add(connection);
                        Debug.Log(DebugType.INFO, "A new TСP connection has been added");
                    }
                    else
                        Thread.sleep(100);
                } catch (Exception e) {
                    Debug.Log(DebugType.WARNING, "Error establishing new TCP connection: " + e.getMessage());
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException ex) {
                            Debug.Log(DebugType.WARNING, "TCP connection closing error: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            Debug.Log(DebugType.ERROR, "IO error opening TCP socket: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            Debug.Log(DebugType.ERROR, "Error opening TCP socket: port is outside the range");
        } catch (Exception e) {
            Debug.Log(DebugType.ERROR, "Error opening TCP socket: " + e.getMessage());
        }
        Debug.Log(DebugType.INFO, "The TСP channel was closed");
    }
}
