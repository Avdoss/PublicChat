package core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class Server extends ConnectionManager
{
    private final long AUTHORIZATION_TIMEOUT = 10000; //ms

    private final AtomicBoolean enable;
    private final List<Channel> channels;
    private final Map<Integer, Long> unauthorizedUsers;
    private final Map<Integer, String> users;
    private final Map<String, String> usersDB;
    private Thread updateThread;

    public Server()
    {
        enable = new AtomicBoolean();
        channels = new ArrayList<>();
        unauthorizedUsers = new HashMap<>();
        users = new HashMap<>();
        usersDB = new HashMap<>();
    }

    public void addChannel(Channel channel)
    {
        if (!enable.get())
            channels.add(channel);
    }

    public boolean loadUsers(String filePath)
    {
        boolean isSuccessfully = false;
        if (!enable.get())
        {
            try (Stream<String> stream = Files.lines(Paths.get(filePath)))
            {
                stream.map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .forEach(
                                line -> {
                                    String[] words = line.split(" ");
                                    if (words.length == 2)   // new entry
                                        usersDB.put(words[0], words[1]);
                                    else
                                        throw new IncorrectDataFormatException("Correct user entry: Login Password");
                                }
                        );
                isSuccessfully = true;
            } catch (InvalidPathException e) {
                System.out.println("The path is incorrect");
            } catch (IOException e) {
                System.out.println("File read error: " + e.getMessage());
            } catch (IncorrectDataFormatException e) {
                System.out.println("Incorrect file format: " + e.getMessage());
            }
        }
        return isSuccessfully;
    }

    public void start()
    {
        if (!enable.get())
        {
            Debug.Log(DebugType.INFO, "The server is running");
            enable.set(true);
            for(Channel channel: channels)
                channel.start();
            updateThread = new Thread(this::run);
            updateThread.start();
        }
    }

    public void stop()
    {
        if (enable.get())
        {
            enable.set(false);
            try{
                updateThread.join();
            } catch (InterruptedException e) {
                Debug.Log(DebugType.WARNING, "Server was closed incorrectly");
            }
            for(Channel channel: channels)
                channel.stop();

            List<Integer> allConnections = new LinkedList<>();
            allConnections.addAll(unauthorizedUsers.keySet());
            allConnections.addAll(users.keySet());
            for(int id: allConnections)
                closeConnection(id);
            Debug.Log(DebugType.INFO, "The server has stopped");
        }
    }

    private void run()
    {
        while(enable.get())
        {
            // authorization timeout
            List<Integer> lossConnections = new LinkedList<>();
            long currentTime = System.currentTimeMillis();
            for (Map.Entry<Integer, Long> entry: unauthorizedUsers.entrySet())
                if(currentTime - entry.getValue() > AUTHORIZATION_TIMEOUT)
                    lossConnections.add(entry.getKey());
            for(int id : lossConnections)
                closeConnection(id);
            // receiving messages
            update();
            // adding new connections
            for(Channel channel: channels)
            {
                while (channel.hasConnection())
                {
                    Connection connection = channel.getConnection();
                    int id = addConnection(connection);
                    unauthorizedUsers.put(id, System.currentTimeMillis());
                }
            }
        }
    }

    @Override
    protected void onConnectionClosed(int id)
    {
        if (users.containsKey(id))
        {
            Debug.Log(DebugType.INFO, String.format("User %s disconnect from server", users.get(id)));
            users.remove(id);
        }
        else
        {
            Debug.Log(DebugType.INFO, String.format("Unauthorized user id:%d disconnect from server", id));
            unauthorizedUsers.remove(id);
        }
    }

    @Override
    protected void onReceiveMessage(int id, Message message)
    {
        switch (message.getMsgType())
        {
            case AUTHORIZATION:
                if (unauthorizedUsers.containsKey(id))
                {
                    AuthorizationMessage request = (AuthorizationMessage)message;
                    AuthorizationResponseMessage answer = new AuthorizationResponseMessage();
                    if (!usersDB.containsKey(request.login))
                        answer.status = AuthorizationResponseMessage.Status.BAD_LOGIN;
                    else if (users.containsValue(request.login))
                        answer.status = AuthorizationResponseMessage.Status.ALREADY_LOGGED;
                    else if (!usersDB.get(request.login).equals(request.password))
                        answer.status = AuthorizationResponseMessage.Status.BAD_PASSWORD;
                    else
                    {
                        answer.status = AuthorizationResponseMessage.Status.SUCCESSFULLY;
                        unauthorizedUsers.remove(id);
                        users.put(id, request.login);
                        Debug.Log(DebugType.INFO, String.format("User %s connect to server", request.login));
                    }
                    sendMessage(id, answer);
                    if (answer.status != AuthorizationResponseMessage.Status.SUCCESSFULLY)
                        closeConnection(id);
                }
                break;
            case DISCONNECT:
                if (users.containsKey(id))
                    closeConnection(id);
                break;
            case LETTER:
                if (users.containsKey(id)) {
                    String login = users.get(id);
                    LetterMessage latter = (LetterMessage)message;
                    latter.content = String.format("%s: %s", login, latter.content);
                    for (int k : users.keySet())
                        sendMessage(k, latter);
                }
                break;
        }
    }
}
