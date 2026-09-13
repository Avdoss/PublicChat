package core;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface SocketBuilder
{
    SocketBuilder setRecvBufferSize(int size);
    SocketBuilder setSendBufferSize(int size);
    SocketBuilder setNodelayMode(boolean value);
    SocketBuilder setKeepAliveMode(boolean value);
    SocketChannel build() throws IOException;
}
