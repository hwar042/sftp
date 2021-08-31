package main;/*
  Code is taken from Computer Networking: A Top-Down Approach Featuring
  the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross,
  All Rights Reserved.
 */

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Scanner;

public class TCPClient {
    static final String outputDir = "tests/downloads/";
    static Socket clientSocket;
    static File file;
    static int fileSize;
    static String output;
    static boolean sendFile;

    public static void main(String[] argv) throws IOException {
        //noinspection LoopConditionNotUpdatedInsideLoop
        do {
            // Create Socket
            clientSocket = new Socket("localhost", 6789);
            // Create stream to Server
            DataOutputStream sendStream = new DataOutputStream(clientSocket.getOutputStream());
            if (sendFile) {
                sendFile(sendStream);
                sendFile = false;
            } else {
                sendText(sendStream);
            }
            // Close socket after transmission
            clientSocket.close();
        } while (argv.length < 1); // Will Loop unless args passed to program
    }

    private static void sendFile(DataOutputStream sendStream) throws IOException {
        // Get Path of file to send
        System.out.println("Enter Absolute Filepath of File to Send");
        String userInputText = new BufferedReader(new InputStreamReader(System.in)).readLine();
        File fileToSend = new File(userInputText);
        // Check Correct filesize specified
        if (fileSize == fileToSend.length()) {
            try {
                // Read File as bytes and send to Server
                byte[] buffer = Files.readAllBytes(Paths.get(userInputText));
                BufferedOutputStream bos = new BufferedOutputStream(sendStream);
                for (byte b : buffer) {
                    bos.write(b);
                }
                bos.flush();
                // Get Server Response
                standard();
            } catch (NoSuchFileException e) {
                System.out.println("No Such File, restarting client");
            }
        } else {
            // Incorrect Size
            System.out.println("-File size mismatch, restarting client");
        }
    }

    private static void sendText(DataOutputStream sendStream) throws IOException {
        // Read User input
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        String userInputText = userInput.readLine();
        sendStream.writeBytes(userInputText + '\n');
        sendStream.flush();
        // Check what user is sending for client-side changes
        try {
            String cmd = userInputText.substring(0, 4);
            String args = userInputText.substring(4);
            switch (cmd.toUpperCase()) {
                case "RETR":
                    retr(args);
                    break;
                case "SEND":
                    send();
                    break;
                case "SIZE":
                    size(args);
                    break;
                default:
                    standard();
            }
        }
        // Catch substring command out of bounds or null text, and just do standard
        catch (StringIndexOutOfBoundsException | NullPointerException e) {
            standard();
        }
    }

    private static void retr(String args) {
        // Prepare to receive file
        file = new File(outputDir + args.substring(1));
        // And get server output
        standard();
        // If no error, store size returned by server
        if (output.charAt(0) != '-') fileSize = Integer.parseInt(output);
    }

    private static void send() throws IOException {
        try {
            // Receive file from server
            FileOutputStream fos = new FileOutputStream(file, false);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            BufferedInputStream bis = new BufferedInputStream(clientSocket.getInputStream());
            for (int i = 0; i < fileSize; i++) {
                bos.write(bis.read());
            }
            bos.close();
            fos.close();
            // Print confirmation
            System.out.println("+File received");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void size(String args) {
        // Get filesize we are sending to server
        try {
            fileSize = Integer.parseInt(args.substring(1));
        }
        // Don't want to crash if no number entered
        catch (NumberFormatException ignored) {
        }
        // Get server output
        standard();
        // Tell Client we are sending a file next, not text
        if (output.charAt(0) != '-') sendFile = true;
    }

    private static void standard() {
        // Get Server Response
        Scanner reader;
        try {
            reader = new Scanner(new InputStreamReader(clientSocket.getInputStream()));
            while (reader.hasNext()) {
                output = reader.nextLine();
                System.out.println(output);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 
