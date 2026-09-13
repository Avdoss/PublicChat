package core;

import java.io.Closeable;
import java.util.List;

public interface Connection extends Closeable
{
    void sendMessage(Message message);
    List<Message> receiveMessages();
    boolean isConnected();
    void close();
}
