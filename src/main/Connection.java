package main;

import java.io.*;
import java.net.Socket;

// Connection Class Reads and Writes from Server, holds socket
public class Connection {
    final Socket socket;
    boolean connected = true;
    DataOutputStream sendStream;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        sendStream = new DataOutputStream(socket.getOutputStream());
    }

    public String getInput() {
        // Read input from Client and return as String
        String input = "";
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return input;
    }

    public void getFile(File file, long filesize, boolean append) throws IOException {
        // Read File from Client and Write to file on Server
        BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
        FileOutputStream fos = new FileOutputStream(file, append);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        for (int i = 0; i < filesize; i++) {
            bos.write(bis.read());
        }
        bos.flush();
        fos.close();
    }

    public void writeOutput(String message) throws IOException {
        // Write Server Response to Client
        sendStream.writeBytes(message + "\n");
        sendStream.close();
    }

    public void writeFile(byte[] buffer) throws IOException {
        // Write File as bytes to Client
        for (byte b : buffer) {
            sendStream.write(b);
        }
        sendStream.close();
    }
}
