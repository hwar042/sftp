/*
  Code is taken from Computer Networking: A Top-Down Approach Featuring
  the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross,
  All Rights Reserved.
 */

import java.io.File;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;

class TCPServer {
    static Connection connection;
    static Auth auth;
    static ArrayList<String[]> users;
    static ArrayList<String[]> accts;
    static String input;
    static String output;
    static File currentDir = new File(System.getProperty("user.dir"));
    static File currentFile;

    private static void initDatabase() {
        // Reads Users and Accounts into memory
        users = new Reader().readDatabase(new File("./src/database/users.txt"));
        accts = new Reader().readDatabase(new File("./src/database/accts.txt"));
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] argv) throws Exception {
        initDatabase();
        ServerSocket welcomeSocket = new ServerSocket(6789);
        while (true) {
            try {
                // Generate new Authentication
                auth = new Auth();
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
                        output = ("-Error! use format: <cmd> [<SPACE> <args>]");
                    }
                    // Disconnect Client if '-' received
                    if (output.charAt(0) == '-') connection.connected = false;
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
        String cmd = message.substring(0, 4);
        String args = message.substring(4);
        // Get Number of Arguments
        int argCount = args.replaceAll("[^ ]", "").length();
        // Call Method Based on Command
        if (!auth.auth) {
            switch (cmd.toUpperCase()) {
                case "USER" -> user(args, argCount);
                case "ACCT" -> acct(args, argCount);
                case "PASS" -> pass(args, argCount);
                default -> unknown();
            }
        } else {
            switch (cmd.toUpperCase()) {
                case "USER" -> user(args, argCount);
                case "ACCT" -> acct(args, argCount);
                case "PASS" -> pass(args, argCount);
                case "TYPE" -> type(args, argCount);
                case "LIST" -> list(args);
                case "CDIR" -> cdir(args);
                case "KILL" -> kill(args);
                case "NAME" -> name(args);
                case "TOBE" -> tobe(args);
                case "DONE" -> done();
                default -> unknown();
            }
        }
    }

    private static void unknown() {
        output = "-Unknown command, try again";
    }

    private static void user(String args, int argCount) {
        // Manages User Command
        String user = args.substring(1);
        // Already Authorized?
        if (auth.auth) output = "!" + user + " logged in";
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
                output = "!" + user + " logged in";
            } else {
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
            } else {
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
                output = ("! Logged in");
            } else {
                // Pass pass, request account
                auth.setT_pass();
                // Set required Account
                setAcct(pass);
                output = "+Send account";
            }
        }
    }

    private static void type(String args, int argCount) {
        switch (args.substring(1).toUpperCase()) {
            case "A" -> output = "+Using ASCII mode";
            case "B" -> output = "+Using Binary mode";
            case "C" -> output = "+Using Continuous mode";
        }
        if (argCount != 1) {
            output = "-Type not valid";
        }
    }

    private static void list(String args) {
        // Get Listing
        String listing = args.substring(1, 2);
        // Check correct Listing
        if (listing.equalsIgnoreCase("f") || listing.equalsIgnoreCase("v")) {
            // Get Path
            File path;
            try {
                path = new File(args.substring(3));
            }
            // Set null path to Current Directory
            catch (StringIndexOutOfBoundsException e) {
                path = currentDir;
            }
            File[] files = path.listFiles();
            StringBuilder fileList = new StringBuilder();
            // Check directory not empty
            if (files != null) {
                fileList.append("+").append(path).append("\n");
                for (File f : files) {
                    String file = f.getName();
                    // Append / to directory
                    if (f.isDirectory()) file = file + "/";
                    // Formatted listing
                    if (listing.equalsIgnoreCase("f")) fileList.append(file).append(" \r\n");
                        // Verbose listing
                    else {
                        fileList.append(file).append(" ").
                                append(new Date(f.lastModified())).append(" ").
                                append(f.length()).append(" bytes").append(" \r\n");
                    }
                }
                output = fileList.toString();
            } else output = "-non-existent directory";
        } else output = "-invalid file listing format";
    }

    private static void cdir(String args) {
        File dir = new File(args.substring(1));
        if (dir.exists()) {
            currentDir = dir;
            output = "!Changed working dir to " + dir.getPath();
        } else {
            output = "-Can't connect to directory because: directory does not exist";
        }
    }

    private static void kill(String args) {
        File file = new File(currentDir.getPath() + "/" + args.substring(1));
        if (file.delete()) {
            output = "+" + file + " deleted";
        } else if (!file.exists()) {
            output = "-Not deleted because file does not exist";
        }
    }

    private static void name(String args) {
        File file = new File(currentDir.getPath() + "/" + args.substring(1));
        if (file.exists()) {
            output = "+File exists";
            currentFile = file;
        } else {
            output = "-Can't find " + file;
            currentFile = null;
        }
    }

    private static void tobe(String args) {
        String oldFile = currentFile.getPath();
        File file = new File(currentDir.getPath() + "/" + args.substring(1));
        if (currentFile != null) {
            if (currentFile.renameTo(file)) {
                output = "+" + oldFile + " was renamed to " + file.getPath();
            } else {
                output = "-File wasn't renamed because file with dest path already exists";
            }
        } else {
            output = "-File wasn't renamed because no file selected via NAME";
        }
        currentFile = null;
    }

    private static void done() {
        output = "+closing connection";
        connection.connected = false;
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


} 

