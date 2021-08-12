/*
  Code is taken from Computer Networking: A Top-Down Approach Featuring
  the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross,
  All Rights Reserved.
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

class TCPServer {

    public static void main(String[] argv) throws Exception {
        String clientSentence;
        String capitalizedSentence;

        ServerSocket welcomeSocket = new ServerSocket(6789);

		//noinspection InfiniteLoopStatement
		while (true) {
		    try {
                Connection connection = new Connection(welcomeSocket.accept());
                connection.writeOutput(connection.input.toUpperCase());
            } catch (SocketException | NullPointerException e) {
		        System.out.println("Lost Connection to Client, restarting");
            }
        }
    }
} 

