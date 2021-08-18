/*
  Code is taken from Computer Networking: A Top-Down Approach Featuring
  the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross,
  All Rights Reserved.
 */

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

class TCPClient {
    static Socket clientSocket;
    static String outputDir = "D:/temp/s/output/";
    static File file;
    static int fileSize;
    static String output;

    public static void main(String[] argv) throws Exception {
        //noinspection InfiniteLoopStatement
        while (true) {
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            clientSocket = new Socket("localhost", 6789);
            DataOutputStream sendStream = new DataOutputStream(clientSocket.getOutputStream());
            String userInputText = userInput.readLine();
            sendStream.writeBytes(userInputText + '\n');
            try {
                String cmd = userInputText.substring(0, 4);
                String args = userInputText.substring(4);
                switch (cmd.toUpperCase()) {
                    case "RETR" -> retr(args);
                    case "SEND" -> send();
                    default -> standard();
                }
            } catch (StringIndexOutOfBoundsException e) {
                standard();
            }
            clientSocket.close();
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
