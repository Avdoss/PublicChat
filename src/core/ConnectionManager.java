package lesson3;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public abstract class ConnectionManager
{
    private final HashMap<Integer, Connection> connections;

    public ConnectionManager()
    {
        connections = new HashMap<>();
    }

    protected int addConnection(Connection newConnection)
    {
        int id = generateID();
        if (id != -1)
            connections.put(id, newConnection);
        return id;
    }

    protected void closeConnection(int id)
    {
        if (connections.containsKey(id))
        {
            Connection connection = connections.get(id);
            connection.close();
            connections.remove(id);
            onConnectionClosed(id);
        }
    }

    protected void sendMessage(int id, Message message)
    {
        if (connections.containsKey(id))
        {
            Connection connection = connections.get(id);
            if(connection.isConnected())
                connection.sendMessage(message);
        }
    }

    protected void update()
    {
        List<Integer> lossConnections = null;
        for(Map.Entry<Integer, Connection> entry: connections.entrySet())
        {
            int id = entry.getKey();
            Connection connection = entry.getValue();
            if (connection.isConnected())
            {
                // receiving messages
                List<Message> messages = connection.receiveMessages();
                if (messages != null)
                    for(Message message: messages)
                        onReceiveMessage(id, message);
            }
            else
            {
                // removing broken connections
                if (lossConnections == null)
                    lossConnections = new LinkedList<>();
                lossConnections.add(id);
            }
        }
        if(lossConnections != null)
            for(int id: lossConnections)
                closeConnection(id);
    }

    protected abstract void onConnectionClosed(int id);

    protected abstract void onReceiveMessage(int id, Message message);

    private int generateID()
    {
        for (int i = 0; i < Integer.MAX_VALUE; i++)
            if (!connections.containsKey(i))
                return i;
        return -1;
    }
}
