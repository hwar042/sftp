package main;/*
  Code is taken from Computer Networking: A Top-Down Approach Featuring
  the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross,
  All Rights Reserved.
 */

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

public class TCPServer {
    static Connection connection; // Stores Connection info (socket + read/write to client)
    static Auth auth; // Stores Authentication info for session
    static ArrayList<String[]> users; // Stores available users
    static ArrayList<String[]> accts; // Stores available accts and Passwords
    static String input; // Input from client
    static String output; // Output to client
    static File currentDir; // Selected directory
    static File nameFile; // File to be renamed
    static byte[] sendFile; // File to send to Client (as bytes)
    static File retrFile; // File to send to Client
    static File storFile; // File to receive from Client
    static boolean storAppend; // Whether to Append to existing file
    static long storSize; // Size of file to receive

    private static void initDatabase() {
        // Reads Users and Accounts into memory
        users = new Reader().readDatabase(new File("database/users.txt"));
        accts = new Reader().readDatabase(new File("database/accts.txt"));
    }

    private static void initServer() {
        // Set default directory
        currentDir = new File(System.getProperty("user.dir"));
        // Generate new Authentication (ie logged out)
        auth = new Auth();
        // Initialize File Info
        nameFile = null;
        sendFile = null;
        retrFile = null;
        storFile = null;
        storAppend = false;
        storSize = 0;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] argv) throws IOException {
        initDatabase(); // Init database
        ServerSocket welcomeSocket = new ServerSocket(6789);
        // Outer Loop Keeps Server alive, Inner loop keeps client connected
        while (true) {
            initServer(); // Init server
            try {
                // Receive Client Connection
                connection = new Connection(welcomeSocket.accept());
                connection.getInput(); // Read client input
                // First output should be connection info:
                connection.writeOutput("+hwar042 SFTP Service");
                // Start another loop while connected
                while (connection.connected) {
                    // New Connection from client
                    connection = new Connection(welcomeSocket.accept());
                    // Check if File expected
                    if (storSize > 0) {
                        getFile(); // Read file from client
                    } else {
                        getText(); // Read text from client
                    }
                    writeOutput(); // Write response to client
                }
            }
            // Restart Server if Client disconnects
            catch (SocketException | NullPointerException e) {
                System.out.println("Lost Connection to Client, restarting");
                e.printStackTrace();
            }
        }
    }

    private static void getFile() { // Receive File from client
        try {
            connection.getFile(storFile, storSize, storAppend); // Store File
            output = "+Saved " + storFile.getName(); // Write Response
        } catch (IOException e) {
            output = "-Couldn't save because of an I/O error";
        }
        // Reset File parameters
        storFile = null;
        storSize = 0;
        storAppend = false;
    }

    private static void getText() {
        // Get Input
        input = connection.getInput();
        try {
            cmdSelect(input);
        }
        // Catch incorrectly formed argument
        catch (StringIndexOutOfBoundsException e) {
            unknown();
        }
    }

    private static void writeOutput() throws IOException {
        // Check if file is being sent
        if (sendFile != null) {
            connection.writeFile(sendFile); // Send File to Client
            sendFile = null; // Clear file being sent
        } else { // Send Text to Client
            // Disconnect Client if '-' received
            if (output.charAt(0) == '-') connection.connected = false;
            // Send Message to Client
            connection.writeOutput(output);
        }
    }

    private static void cmdSelect(String message) {
        // Get Command and Argument Strings
        String cmd = message.substring(0, 4);
        String args = message.substring(4);
        // Get Number of Arguments
        int argCount = args.replaceAll("[^ ]", "").length();
        // Call Method Based on Command
        // Limited Selection when not logged in
        if (!auth.auth) {
            switch (cmd.toUpperCase()) {
                case "USER":
                    user(args, argCount);
                    break;
                case "ACCT":
                    acct(args, argCount);
                    break;
                case "PASS":
                    pass(args, argCount);
                    break;
                default:
                    unknown();
            }
        }
        // Full Command List
        else {
            switch (cmd.toUpperCase()) {
                case "USER":
                    user(args, argCount);
                    break;
                case "ACCT":
                    acct(args, argCount);
                    break;
                case "PASS":
                    pass(args, argCount);
                    break;
                case "TYPE":
                    type(args, argCount);
                    break;
                case "LIST":
                    list(args);
                    break;
                case "CDIR":
                    cdir(args);
                    break;
                case "KILL":
                    kill(args);
                    break;
                case "NAME":
                    name(args);
                    break;
                case "TOBE":
                    tobe(args);
                    break;
                case "DONE":
                    done();
                    break;
                case "RETR":
                    retr(args);
                    break;
                case "SEND":
                    send();
                    break;
                case "STOP":
                    stop();
                    break;
                case "STOR":
                    stor(args);
                    break;
                case "SIZE":
                    size(args);
                    break;
                default:
                    unknown();
            }
        }
    }

    private static void unknown() {
        output = "-Unknown command, try again"; // Output when unknown message received
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
            auth.setAcct(acct.toLowerCase());
            // Log in if matching password
            if (auth.t_pass) {
                auth.setAuth();
                output = "! Account valid, logged-in";
            } else {
                // Pass account, request password
                auth.setT_acct();
                // Set Required Password
                setPass();
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
        // Output mode specified
        switch (args.substring(1).toUpperCase()) {
            case "A":
                output = "+Using ASCII mode";
                break;
            case "B":
                output = "+Using Binary mode";
                break;
            case "C":
                output = "+Using Continuous mode";
                break;
            default:
                output = "-Type not valid";
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
            // Get files in directory
            File[] files = path.listFiles();
            StringBuilder fileList = new StringBuilder();
            // Check directory not empty
            if (files != null) {
                // First line of output is +path
                fileList.append("+").append(path).append("\n");
                // Append Directory Listing
                for (File f : files) {
                    String file = f.getName();
                    // Append / to directory
                    if (f.isDirectory()) file = file + "/";
                    // Formatted listing
                    if (listing.equalsIgnoreCase("f")) fileList.append(file).append(" \r\n");
                    // Verbose listing - last modified date and size in bytes
                    else {
                        fileList.append(file).append(" ").
                                append(new Date(f.lastModified())).append(" ").
                                append(f.length()).append(" bytes").append(" \r\n");
                    }
                }
                // Convert String builder to String for output
                output = fileList.toString();
            } else output = "-non-existent directory";
        } else output = "-invalid file listing format";
    }

    private static void cdir(String args) {
        // Get directory from input
        File dir = new File(args.substring(1));
        // Change to directory if it exists
        if (dir.isDirectory()) {
            currentDir = dir;
            output = "!Changed working dir to " + dir.getPath();
        } else {
            output = "-Can't connect to directory because: directory does not exist";
        }
    }

    private static void kill(String args) {
        // Get file from input
        File file = new File(currentDir.getPath() + "/" + args.substring(1));
        // Attempt to delete file
        if (file.delete()) {
            output = "+" + file.getName() + " deleted";
        } else if (!file.exists()) {
            output = "-Not deleted because file does not exist";
        }
    }

    private static void name(String args) {
        // Get file from input
        File file = new File(currentDir.getPath() + "/" + args.substring(1));
        // Attempt to store file for renaming
        if (file.exists()) {
            output = "+File exists";
            nameFile = file;
        } else {
            output = "-Can't find " + file.getName();
            nameFile = null;
        }
    }

    private static void tobe(String args) {
        // Store oldFile name for output
        String oldFile = nameFile.getName();
        // Get new name from input
        File file = new File(currentDir.getPath() + "/" + args.substring(1));
        // Attempt to rename file
        if (nameFile != null) {
            if (!file.exists() && nameFile.renameTo(file)) {
                output = "+" + oldFile + " was renamed to " + file.getName();
            } else {
                output = "-File wasn't renamed because file with dest path already exists";
            }
        } else {
            unknown();
        }
        // Clear file being renamed
        nameFile = null;
    }

    private static void done() {
        // Close connection
        output = "+closing connection";
        connection.connected = false;
    }

    private static void retr(String args) {
        // Get file to be retrieved from input
        File file = new File(currentDir.getPath() + "/" + args.substring(1));
        // Attempt to set file for retrieval
        if (file.exists() && !file.isDirectory()) {
            retrFile = file;
            output = String.valueOf(file.length());
        } else {
            retrFile = null;
            output = "-File doesn't exist";
        }
    }

    private static void send() {
        // Attempt to retrieve file
        try {
            sendFile = Files.readAllBytes(Paths.get(retrFile.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
            unknown();
        }
        retrFile = null;
    }

    private static void stop() {
        retrFile = null;
        output = "+ok, RETR aborted";
    }

    private static void stor(String args) {
        String cmd = args.substring(1, 4);
        storFile = new File(currentDir.getPath() + "/" + args.substring(5));
        switch (cmd.toUpperCase()) {
            case "NEW": {
                if (storFile.isFile()) {
                    output = "-File exists, but system doesn't support generations";
                } else {
                    output = "+File does not exist, will create new file";
                }
                break;
            }
            case "OLD": {
                if (storFile.isFile()) {
                    output = "+Will write over old file";
                } else {
                    output = "+Will create new file";
                }
                break;
            }
            case "APP": {
                if (storFile.isFile()) {
                    output = "+Will append to file";
                    storAppend = true;
                } else {
                    output = "+Will create file";
                }
                break;
            }
            default: {
                output = "-unknown STOR flag, use NEW, OLD or APP";
                storFile = null;
            }
        }
    }

    private static void size(String args) {
        if (storFile != null) {
            try {
                storSize = Long.parseLong(args.substring(1));
                if (currentDir.getFreeSpace() > storSize) {
                    output = "+ok, waiting for file";
                } else {
                    output = "-Not enough room, don't send it";
                    storFile = null;
                    storSize = 0;
                }
            } catch (NumberFormatException e) {
                output = "-Incorrect number format";
            }
        }
    }

    private static boolean checkInvalid(String string, ArrayList<String[]> data) {
        // Checks Data Structures for Strings, confirms if found
        for (String[] strings : data) {
            if (strings[0].equalsIgnoreCase(string)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkSuperUser(String user) {
        // Checks if User can bypass password (* in String array)
        for (String[] strings : users) {
            if (strings[0].equalsIgnoreCase(user) && strings.length > 1) {
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

    private static void setPass() {
        // Sets required password for account
        for (String[] strings : accts) {
            if (strings[0].contains(auth.acct)) {
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

