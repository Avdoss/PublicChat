package core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Client extends ConnectionManager
{
    private final AtomicBoolean enable;
    private final AtomicInteger serverId;
    private final AtomicBoolean authorizedStatus;
    private Thread updateThread;
    private final ConcurrentLinkedQueue<Message> messagesQueue;

    public Client()
    {
        enable = new AtomicBoolean();
        serverId = new AtomicInteger(-1);
        authorizedStatus = new AtomicBoolean();
        messagesQueue = new ConcurrentLinkedQueue<>();
    }

    public boolean isConnected()
    {
        return serverId.get() != -1;
    }

    public boolean isAuthorized()
    {
        return authorizedStatus.get();
    }

    public boolean connectToServerTCP(String host, int port)
    {
        if (isConnected())
            return false;
        boolean result = false;
        try{
            SocketBuilder builder = new NonBlockingTCPSocketBuilder();
            SocketChannel socket = builder.setRecvBufferSize(8192).setSendBufferSize(8192).build();
            socket.connect(new InetSocketAddress(host, port));
            while (!socket.finishConnect())
            {
                Thread.yield();
            };
            Connection connection = new TCPConnectionAdapter(socket);
            serverId.set(addConnection(connection));
            result = true;
        } catch (UnknownHostException e) {
            System.out.println("Incorrect host: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("TCP connection error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            Debug.Log(DebugType.ERROR, "Incorrect hostname or port");
        } catch (Exception e) {
            Debug.Log(DebugType.ERROR, "The server is unavailable. " + e.getMessage());
        }
        return result;
    }

    public void authorization(String login, String password)
    {
        if (isConnected() && !isAuthorized())
        {
            AuthorizationMessage message = new AuthorizationMessage();
            message.login = login;
            message.password = password;
            messagesQueue.add(message);
        }
    }

    public void sendMessage(String content)
    {
        if (isAuthorized())
        {
            String[] words = content.split(" ");
            String lastWord = words[words.length - 1];
            StringMessage message;

            if (lastWord.startsWith("\\"))
            {
                message = new LetterMessage(content.replace(lastWord, ""));
                if(lastWord.contains("r"))
                    message = new StringColorDecorator(message);
                if(lastWord.contains("b"))
                    message = new StringStyleDecorator(message);
            }
            else
                message = new LetterMessage(content);

            messagesQueue.add(message);
        }
    }

    public void start()
    {
        if (!enable.get())
        {
            Debug.Log(DebugType.INFO, "The client is running");
            enable.set(true);
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
                Debug.Log(DebugType.WARNING, "Client was closed incorrectly");
            }
            if (isAuthorized())
            {
                DisconnectMessage message = new DisconnectMessage();
                sendMessage(serverId.get(), message);
            }
            if (isConnected())
                closeConnection(serverId.get());
            Debug.Log(DebugType.INFO, "The client has stopped");
        }
    }

    private void run()
    {
        while(enable.get())
        {
            if (isConnected())
            {
                // sending messages
                while(!messagesQueue.isEmpty())
                {
                    Message message = messagesQueue.poll();
                    switch (message.getMsgType())
                    {
                        case AUTHORIZATION:
                            if (!isAuthorized())
                                sendMessage(serverId.get(), message);
                            break;
                        case LETTER:
                            if (isAuthorized())
                                sendMessage(serverId.get(), message);
                            break;
                    }
                }
                // receiving messages
                update();
            }
        }
    }

    @Override
    protected void onConnectionClosed(int id)
    {
        serverId.set(-1);
        authorizedStatus.set(false);
        messagesQueue.clear();
    }

    @Override
    protected void onReceiveMessage(int id, Message message)
    {
        switch (message.getMsgType())
        {
            case AUTHORIZATION_RESPONSE:
                AuthorizationResponseMessage response = (AuthorizationResponseMessage)message;
                if (response.status == AuthorizationResponseMessage.Status.SUCCESSFULLY) {
                    authorizedStatus.set(true);
                    System.out.println("Authorization successful!");
                }
                else
                    System.out.println("Authorization error: " + response.status.name());
                break;
            case LETTER:
                LetterMessage letter = (LetterMessage)message;
                System.out.println(letter.content);
                break;
        }
    }
}
