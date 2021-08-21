/*
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

class TCPClient {
    static final String outputDir = "D:/temp/s/output/";
    static Socket clientSocket;
    static File file;
    static int fileSize;
    static String output;
    static boolean sendFile;

    public static void main(String[] argv) throws Exception {
        //noinspection InfiniteLoopStatement
        while (true) {
            clientSocket = new Socket("localhost", 6789);
            DataOutputStream sendStream = new DataOutputStream(clientSocket.getOutputStream());
            if (sendFile) {
                sendFile(sendStream);
                sendFile = false;
            } else {
                sendText(sendStream);
            }
            clientSocket.close();
        }
    }

    private static void sendFile(DataOutputStream sendStream) throws IOException {
        System.out.println("Enter Absolute Filepath of File to Send");
        String userInputText = new BufferedReader(new InputStreamReader(System.in)).readLine();
        if (fileSize == new File(userInputText).length()) {
            try {
                byte[] buffer = Files.readAllBytes(Paths.get(userInputText));
                BufferedOutputStream bos = new BufferedOutputStream(sendStream);
                for (byte b : buffer) {
                    bos.write(b);
                }
                bos.flush();
                standard();
            } catch (NoSuchFileException e) {
                System.out.println("No Such File, restarting client");
            }
        } else {
            System.out.println("-File size mismatch, restarting client");
        }
    }

    private static void sendText(DataOutputStream sendStream) throws IOException {
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        String userInputText = userInput.readLine();
        sendStream.writeBytes(userInputText + '\n');
        sendStream.flush();
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
        } catch (StringIndexOutOfBoundsException e) {
            standard();
        }
    }

    private static void retr(String args) {
        file = new File(outputDir + args.substring(1));
        standard();
        if (output.charAt(0) != '-') fileSize = Integer.parseInt(output);
    }

    private static void send() throws IOException {
        try {
            FileOutputStream fos = new FileOutputStream(file, false);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            BufferedInputStream bis = new BufferedInputStream(clientSocket.getInputStream());
            for (int i = 0; i < fileSize; i++) {
                bos.write(bis.read());
            }
            bos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void size(String args) {
        try {
            fileSize = Integer.parseInt(args.substring(1));
        } catch (NumberFormatException ignored) {
        }
        standard();
        if (output.charAt(0) != '-') sendFile = true;
    }

    private static void standard() {
        Scanner reader;
        try {
            reader = new Scanner(new InputStreamReader(clientSocket.getInputStream()));
            while (reader.hasNext()) {
                output = reader.nextLine();
                System.out.println(output);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 
