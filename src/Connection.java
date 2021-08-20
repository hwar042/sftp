import java.io.*;
import java.net.Socket;

public class Connection {
    Socket socket;
    boolean connected = true;
    DataOutputStream sendStream;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        sendStream = new DataOutputStream(socket.getOutputStream());
    }

    public String getInput() {
        String input = "";
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return input;
    }

    public void getFile(File file, long filesize, boolean append) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
        FileOutputStream fos = new FileOutputStream(file, append);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        // Set timeout
        long time = System.currentTimeMillis();
        for (int i = 0; i < filesize; i++) {
            bos.write(bis.read());
        }
        bos.flush();
        fos.close();
    }

    public void writeOutput(String message) throws IOException {
        DataOutputStream send = new DataOutputStream(socket.getOutputStream());
        send.writeBytes(message + "\n");
        send.close();
    }

    public void writeFile(byte[] buffer) throws IOException {
        DataOutputStream send = new DataOutputStream(socket.getOutputStream());
        for (byte b : buffer) {
            send.write(b);
        }
        send.close();
    }
}
