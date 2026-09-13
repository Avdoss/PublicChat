package core;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public class Program {

    public static void main(String[] args)
    {
        boolean isDebugMode = Arrays.stream(args).anyMatch(it -> it.equals("-d") | it.equals("--debug"));

        Debug.addDebugHandler(new ErrorDebugHandler());
        if (isDebugMode) {
            Debug.addDebugHandler(new WarningDebugHandler());
            Debug.addDebugHandler(new InfoDebugHandler());
        }

        Scanner scanner = new Scanner(System.in);
        String command;
        do {
            System.out.println("--- COMMANDS ---");
            System.out.println("1. \\server");
            System.out.println("2. \\client");
            System.out.println("3. \\exit");
            System.out.println("----------------");
            System.out.println("Please, enter command...");
            command = scanner.nextLine();
        } while (!command.equals("\\server") && !command.equals("\\client") && !command.equals("\\exit"));

        if (command.equals("\\server"))
        {
            Server server = new Server();
            System.out.println("Please, enter the path to the file with users...");
            String filepath = scanner.nextLine();
            if (server.loadUsers(filepath)) {
                System.out.println("Users successfully loaded from file");
                System.out.println("Please, enter port for TCP channel...");
                try {
                    int port = Integer.parseInt(scanner.nextLine());
                    TCPChannel tcpChannel = new TCPChannel(port);
                    if (isDebugMode)
                        server.addChannel(new TCPChannelDebugProxy(tcpChannel));
                    else
                        server.addChannel(tcpChannel);
                } catch (NumberFormatException e) {
                    System.out.println("Warning: Incorrect port. TCP channel is not open");
                }
                server.start();
                do {
                    System.out.println("To close the server, enter \\exit command");
                } while (!scanner.nextLine().equals("\\exit"));
                server.stop();
            }
            else
            {
                System.out.println("Error loading users");
                scanner.nextLine();
            }
        } else if (command.equals("\\client"))
        {
            Client client = new Client();
            client.start();
            do {
                System.out.println("--- COMMANDS ---");
                System.out.println("1. \\connect_tcp");
                System.out.println("2. \\exit");
                System.out.println("----------------");
                System.out.println("Please, enter command...");
                command = scanner.nextLine();
                if (command.equals("\\connect_tcp"))
                {
                    System.out.println("Please, enter the server's IP address or hostname...");
                    System.out.println("Example: 127.0.0.1");
                    String ip = scanner.nextLine();
                    System.out.println("Please, enter the server's port...");
                    System.out.println("Example: 3858");
                    int port = -1;
                    try{
                        port = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Incorrect port");
                    }

                    if(port != -1)
                    {
                        System.out.println("Please, enter login...");
                        String login = scanner.nextLine();
                        System.out.println("Please, enter password...");
                        String password = scanner.nextLine();
                        System.out.println("Connection...");
                        if(client.connectToServerTCP(ip, port))
                        {
                            client.authorization(login, password);
                            while(client.isConnected() && !client.isAuthorized())
                            {
                                Thread.yield();
                            }
                            if(client.isAuthorized())
                                System.out.println("You can send messages in the format: \"message [\\rb]\", where option r - red color, option b - bold style");
                            while(client.isAuthorized())
                            {
                                command = scanner.nextLine();
                                if(command.equals("\\exit"))
                                    break;
                                else
                                    client.sendMessage(command);
                            }
                        }
                    }
                }
            } while (!client.isConnected() && !command.equals("\\exit"));

            client.stop();
        }
        scanner.close();
    }
}
