/*
  Code is taken from Computer Networking: A Top-Down Approach Featuring
  the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross,
  All Rights Reserved.
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

class TCPClient {

    public static void main(String[] argv) throws Exception {
        //noinspection InfiniteLoopStatement
        while(true) {
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            Socket clientSocket = new Socket("localhost", 6789);
            BufferedReader receiveStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            DataOutputStream sendStream = new DataOutputStream(clientSocket.getOutputStream());


            sendStream.writeBytes(userInput.readLine() + '\n');
            System.out.println("FROM SERVER: " + receiveStream.readLine());

            clientSocket.close();
        }

    }
} 
