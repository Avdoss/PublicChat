package core;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.SocketChannel;

public class NonBlockingTCPSocketBuilder implements SocketBuilder
{
    private int recvBufferSize;
    private int sendBufferSize;
    private boolean noDelayMode;
    private boolean keepAliveMode;

    public NonBlockingTCPSocketBuilder()
    {
        // defaults
        recvBufferSize = 8192; // input buffer size
        sendBufferSize = 8192; // output buffer size
        noDelayMode = false; // Nagle's algorithm for this connection
        keepAliveMode = false; // periodic connection availability check
    }

    @Override
    public SocketBuilder setRecvBufferSize(int size) {
        recvBufferSize = size;
        return this;
    }

    @Override
    public SocketBuilder setSendBufferSize(int size) {
        sendBufferSize = size;
        return this;
    }

    @Override
    public SocketBuilder setNodelayMode(boolean value) {
        noDelayMode = value;
        return this;
    }

    @Override
    public SocketBuilder setKeepAliveMode(boolean value) {
        keepAliveMode = value;
        return this;
    }

    public SocketChannel build() throws IOException {
        SocketChannel socket = SocketChannel.open();
        socket.setOption(StandardSocketOptions.SO_RCVBUF, recvBufferSize);
        socket.setOption(StandardSocketOptions.SO_SNDBUF, sendBufferSize);
        socket.setOption(StandardSocketOptions.TCP_NODELAY, noDelayMode);
        socket.setOption(StandardSocketOptions.SO_KEEPALIVE, keepAliveMode);
        socket.configureBlocking(false);
        return socket;
    }
}
