import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Connection {
    Socket socket;
    String input;
    boolean connected = true;

    public Connection(Socket socket) {
        this.socket = socket;
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeOutput(String message) throws IOException {
            System.out.println("Writing to Client");
            DataOutputStream send = new DataOutputStream(socket.getOutputStream());
            send.writeBytes(message + "\n");
            send.close();
    }
}
