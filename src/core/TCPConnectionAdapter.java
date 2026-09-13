package core;

import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class TCPConnectionAdapter implements Connection
{
    private static ByteBuffer ioBuffer;

    static {
        ioBuffer = ByteBuffer.allocate(1024);
    }

    private final SocketChannel socket;
    private boolean connected;
    private boolean closed;

    public TCPConnectionAdapter(SocketChannel socket)
    {
        this.socket = socket;
        this.connected = true;
        this.closed = false;
    }

    @Override
    public void sendMessage(Message message)
    {
        if (!closed && isConnected())
        {
            ioBuffer.clear();
            try {
                Message.serializeMessage(ioBuffer, message);
                ioBuffer.flip();
                socket.write(ioBuffer);
            } catch (Exception e) {
                connected = false;
            }
        }
    }

    @Override
    public List<Message> receiveMessages()
    {
        List<Message> messages = null;
        if (!closed && isConnected())
        {
            ioBuffer.clear();
            try {
                int readBytes = socket.read(ioBuffer);
                if (readBytes > 0)
                {
                    ioBuffer.flip();
                    messages = new LinkedList<>();
                    while(ioBuffer.position() < ioBuffer.limit())
                    {
                        Message message = Message.deserializeMessage(ioBuffer);
                        if (message != null)
                            messages.add(message);
                    }
                } else if (readBytes == -1)
                    connected = false;
            } catch (Exception e) {
                connected = false;
            }
        }
        return messages;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void close()
    {
        if (!closed) {
            closed = true;
            try {
                socket.close();
            } catch (IOException e) {
                Debug.Log(DebugType.ERROR, "TCP socket closing error: " + e.getMessage());
            }
        }
    }
}
