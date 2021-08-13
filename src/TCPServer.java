/*
  Code is taken from Computer Networking: A Top-Down Approach Featuring
  the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross,
  All Rights Reserved.
 */

import java.io.File;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;

class TCPServer {
    static Connection connection;
    static Auth auth;
    static ArrayList<String[]> users;
    static ArrayList<String[]> accts;
    static String input;
    static String output;

    private static void initDatabase() {
        // Reads Users and Accounts into memory
        users = new Reader().readDatabase( new File("./src/database/users.txt"));
        accts = new Reader().readDatabase( new File("./src/database/accts.txt"));
    }

    private static void disconnect() {
        auth = new Auth();
        connection.connected = false;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] argv) throws Exception {
        initDatabase();
        ServerSocket welcomeSocket = new ServerSocket(6789);
        auth = new Auth();
        while (true) {
            try {
                // Receive Client Connection
                connection = new Connection(welcomeSocket.accept());
                // First output should be connection info:
                connection.writeOutput("+hwar042 SFTP Service");
                // Start another loop while connected
                while (connection.connected) {
                    // New Connection from client
                    connection = new Connection(welcomeSocket.accept());
                    // Get Input
                    input = connection.input;
                    try {
                        cmdSelect(input);
                    }
                    // Catch incorrectly formed argument
                    catch (StringIndexOutOfBoundsException e) {
                        connection.writeOutput("-Error! use format: <cmd> [<SPACE> <args>]");
                    }
                    // Disconnect Client if '-' received
                    if (output.charAt(0) == '-') disconnect();
                    // Send Message to Client
                    connection.writeOutput(output);
                }
            }
            // Restart Server if Client disconnects
            catch (SocketException | NullPointerException e) {
                System.out.println("Lost Connection to Client, restarting");
            }
        }
    }

    private static void cmdSelect(String message) {
        // Get Command and Argument Strings
        String cmd = message.substring(0,4);
        String args = message.substring(4);
        // Get Number of Arguments
        int argCount = args.replaceAll("[^ ]", "").length();
        // Call Method Based on Command
        switch (cmd.toUpperCase()) {
            case "USER" -> user(args, argCount);
            case "ACCT" -> acct(args, argCount);
            case "PASS" -> pass(args, argCount);
            default -> unknown();
        }
    }

    private static void user(String args, int argCount) {
        // Manages User Command
        String user = args.substring(1);
        // Already Authorized?
        if (auth.auth) output = "!"+ user +" logged in";
            // Command in Correct Form, user exists
            // user takes 1 arg
        else if (argCount != 1 || checkInvalid(user, users)) output = "-Invalid user-id, try again";
            // Log in user
        else {
            auth.setUser(user);
            // Check if Account Required
            if (checkSuperUser(user)) {
                // Log in if not
                auth.setAuth();
                output = "!"+ user +" logged in";
            }
            else {
                // Pass User, request account
                auth.setT_user();
                output = "+User-id valid, send account and password";
            }
        }
    }

    private static void acct(String args, int argCount) {
        // Manages Acct command
        String acct = args.substring(1);
        // Already Authorized?
        if (auth.auth) output = "! Account valid, logged-in";
            // User registered, Command in Correct Form, Acct exists, Acct matches (if) existing password,
            // acct takes 1 arg
        else if (!auth.t_user || argCount != 1 || checkInvalid(acct, accts) ||
                auth.t_pass && !acct.equals(auth.acct)) {
            output = "-Invalid account, try again";
        }
        // Log in account
        else {
            auth.setAcct(acct);
            // Log in if matching password
            if (auth.t_pass) {
                auth.setAuth();
                output = "! Account valid, logged-in";
            }
            else {
                // Pass account, request password
                auth.setT_acct();
                // Set Required Password
                setPass(acct);
                output = "+Account valid, send password";
            }
        }
    }

    private static void pass(String args, int argCount) {
        // Manages Pass Command
        String pass = args.substring(1);
        // Already Authorized?
        if (auth.auth) output = "! Logged in";
            // User registered, Command in Correct Form, Pass exists, Acct matches (if) existing password,
            // pass takes 1 arg
        else if (!auth.t_user || argCount != 1 || !checkPass(pass) ||
                auth.t_acct && !auth.pass.equals(pass)) {
            output = "-Wrong password, try again";
        }
        // Log in password
        else {
            auth.setPass(pass);
            // Log in if matching account
            if (auth.t_acct) {
                auth.setAuth();
                connection.writeOutput("! Logged in");
            }
            else {
                // Pass pass, request account
                auth.setT_pass();
                // Set required Account
                setAcct(pass);
                output = "+Send account";
            }
        }
    }

    private static boolean checkInvalid(String string, ArrayList<String[]> data) {
        // Checks Data Structures for Strings, confirms if found
        for (String[] strings : data) {
            if (strings[0].contains(string)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkSuperUser(String user) {
        // Checks if User can bypass password (* in String array)
        for (String[] strings : users) {
            if (strings[0].contains(user) && strings.length > 1) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkPass(String pass) {
        // Checks if accounts matches password
        for (String[] strings : accts) {
            if (strings[1].contains(pass)) {
                return true;
            }
        }
        return false;
    }

    private static void setPass(String acct) {
        // Sets required password for account
        for (String[] strings : accts) {
            if (strings[0].contains(acct)) {
                auth.setPass(strings[1]);
            }
        }
    }

    private static void setAcct(String pass) {
        // Sets Required account from given password
        for (String[] strings : accts) {
            if (strings[1].contains(pass)) {
                auth.setAcct(strings[0]);
            }
        }
    }

    private static void unknown() {
        output = "-Unknown command, try again";
    }


} 

