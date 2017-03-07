package client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class client {

    public static void main(String[] args) throws Exception {

        if(args.length < 4) {
            System.err.println("Not enough args!");
            return;
        }

        String serverAddress = args[0];
        String message = args[3];
        int nPort, reqCode;

        try {
            nPort = Integer.parseInt(args[1]);
            reqCode = Integer.parseInt(args[2]);
        } catch(NumberFormatException e) {
            printErr("n_port and req_code need to be valid integers!");
            return;
        }

        InetAddress ipAddress;
        if((ipAddress = getValidAddress(serverAddress)) == null) {
            printErr("Invalid Server Address");
            return;
        }

        Socket clientSocket;
        try {
            clientSocket = new Socket(ipAddress, nPort);
        } catch (ConnectException e) {
            printErr("Server refused connection!");
            return;
        }

        try {
            int rPort = getRPort(clientSocket, reqCode);
            /* ---- end tcp negotiation --- */

            /* ---- udp part ---- */

            String reversedString = getReversedString(ipAddress, rPort, message);
            printOut(reversedString);

            /* --- end udp part --- */
        } catch(IOException e) {
            printErr("Failed to get response from server!");
            clientSocket.close();
            return;
        } catch(NumberFormatException e) {
            printErr("Server gave invalid r_port!");
            clientSocket.close();
            return;
        }

    }

    private static String getReversedString(InetAddress addr, int rPort, String string) throws IOException {
        try {
            DatagramSocket udpSocket = new DatagramSocket();

            byte[] sendData;
            byte[] receiveData = new byte[1024];
            sendData = string.getBytes();

            //send server the string
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, addr, rPort);
            udpSocket.send(sendPacket);

            //wait for server response
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            udpSocket.receive(receivePacket);

            String reversedString = new String(receivePacket.getData());
            udpSocket.close();

            return reversedString;
        } catch (IOException e) {
            throw e;
        }
    }
    private static int getRPort(Socket socket, int reqCode) throws IOException, NumberFormatException {
        try {
            BufferedReader inFromServer = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
            outToServer.writeBytes(reqCode + "\n");

            String serverResponse = inFromServer.readLine();
            return Integer.parseInt(serverResponse);
        } catch (IOException e) {
            throw e;
        } catch (NumberFormatException e) {
            throw e;
        }
    }

    private static InetAddress getValidAddress(String address) {
        try {
            return Inet4Address.getByName(address);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    private static void printOut(String msg) {
        System.out.println(msg);
    }

    private static void printErr(String err) {
        System.err.println(err);
    }
}
