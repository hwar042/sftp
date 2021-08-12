/*
  Code is taken from Computer Networking: A Top-Down Approach Featuring
  the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross,
  All Rights Reserved.
 */

import java.io.File;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

class TCPServer {
    static Connection connection;
    static ArrayList<String[]> users;
    static ArrayList<String[]> accts;

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] argv) throws Exception {
        initDatabase();
        ServerSocket welcomeSocket = new ServerSocket(6789);

        while (true) {
            try {
                // Receive Client Connection
                connection = new Connection(welcomeSocket.accept());
                // First output should be connection info:
                connection.writeOutput("+hwar042 SFTP Service");
                // Start another loop while connected
                while (true) {
                    connection = new Connection(welcomeSocket.accept());
                    cmdSelect(connection.input);
                    //connection.writeOutput(connection.input.toUpperCase());
                }
            } catch (SocketException | NullPointerException e) {
                System.out.println("Lost Connection to Client, restarting");
            }
        }
    }

    private static void cmdSelect(String message) {
        String cmd = message.substring(0,4);
        String args = message.substring(4);
        int argCount = args.replaceAll("[^ ]", "").length();
        switch (cmd.toUpperCase()) {
            case "USER" -> user(args, argCount);
            default -> unknown();
        }
    }

    private static void unknown() {
        connection.writeOutput("-Unknown command, try again");
    }

    private static void user(String args, int argCount) {
        // user takes 1 arg
        if (argCount != 1 || !checkUser(args.substring(1))) {
            connection.writeOutput("-Invalid user-id, try again");
        } else {
            connection.writeOutput("Got a User!");
        }
    }

    private static void initDatabase() {
        users = new Reader().readDatabase( new File("./src/database/users.txt"));
        accts = new Reader().readDatabase( new File("./src/database/accts.txt"));
    }

    private static boolean checkUser(String user) {
        for (String[] strings : users) {
            if (strings[0].contains(user)) {
                return true;
            }
        }
        return false;
    }


} 

