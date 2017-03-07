package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class server {

	//based on TCPServer.java and UDPServer.java from text book
    public static void main(String[] args) throws Exception {
	/* ---- tcp negotiation begin --- */

        int reqCode;
        try {
            reqCode = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            printErr("Request Code must be an integer!");
            return;
        } catch (ArrayIndexOutOfBoundsException e) {
            printErr("Request Code must be provided!");
            return;
        }

        String clientReqCode, clientSentence, reversedSentence;

        //set up welcome socket
        ServerSocket welcomeSocket = new ServerSocket(0);
        printOut("SERVER_PORT=" + welcomeSocket.getLocalPort());

        //begin waiting for connections
        while(true) {
            Socket connectionSocket = welcomeSocket.accept();
            BufferedReader inFromClient =
                    new BufferedReader(
                            new InputStreamReader(connectionSocket.getInputStream()
                            )
                    );

            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            clientReqCode = inFromClient.readLine();

            if(clientReqCode != null) {
                //try to parse the sent request code
                try {
                    DatagramSocket udpSocket;
                    if((udpSocket = createUDPSocket(connectionSocket, clientReqCode, reqCode)) == null) {
                        //failed to create the socket for some reason
                        continue;
                    }

                    outToClient.writeBytes(udpSocket.getLocalPort() + "\n");
                    connectionSocket.close();

                    /* ------ udp part begin ------ */

                    byte[] receiveData = new byte[1024];

                    //get the sentence to be reversed
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    udpSocket.receive(receivePacket);
                    clientSentence = new String(receivePacket.getData());

                    //get client info
                    InetAddress clientAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();

                    reversedSentence = reverseSentence(clientSentence);

                    try {
                        sendBackAndClose(udpSocket, reversedSentence, clientAddress, clientPort);
                    } catch (IOException e) {
                        printErr("Failed to send reversed string!");
                        return;
                    }

					/* -------- udp part ends ----- */
                } catch (Exception e) {
                    connectionSocket.close();
                }
            }
        }
    }

    private static DatagramSocket createUDPSocket(
            Socket connection,
            String clientReqCode,
            int serverReqCode
    ) throws IOException {
        int clientReqCodeInt = Integer.parseInt(clientReqCode);
        if(clientReqCodeInt != serverReqCode) {
            try {
                connection.close();
            } catch (IOException e) {
                throw e;
            }
        }

        return new DatagramSocket(0);
    }
    private static void sendBackAndClose(
            DatagramSocket socket,
            String msg,
            InetAddress addr,
            int port
    ) throws IOException {
        byte[] sendData;
        sendData = msg.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(
                sendData, sendData.length, addr, port
        );

        try {
            socket.send(sendPacket);
        } catch(IOException e) {
            socket.close();
            throw e;
        }
    }

    private static String reverseSentence(String sentence) {
        return new StringBuffer(sentence).reverse().toString();
    }

    private static void printOut(String msg) {
        System.out.println(msg);
    }

    private static void printErr(String err) {
        System.err.println(err);
    }
}
