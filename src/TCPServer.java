/*
  Code is taken from Computer Networking: A Top-Down Approach Featuring
  the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross,
  All Rights Reserved.
 */

import java.net.ServerSocket;
import java.net.SocketException;

class TCPServer {
    static Connection connection;

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] argv) throws Exception {
        String clientSentence;
        String capitalizedSentence;

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
                    connection.writeOutput(connection.input.toUpperCase());
                }
            } catch (SocketException | NullPointerException e) {
                System.out.println("Lost Connection to Client, restarting");
            }
        }
    }


} 

