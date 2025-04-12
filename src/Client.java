import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Client <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);

        try {
            // Спочатку знаходимо сервер через UDP broadcast
            DatagramSocket udpSocket = new DatagramSocket();
            udpSocket.setBroadcast(true);

            byte[] sendData = "CCS DISCOVER".getBytes();
            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcastAddress, port);
            udpSocket.send(sendPacket);

            // Очікуємо відповідь від сервера
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            udpSocket.receive(receivePacket);

            // Підключаємося до сервера через TCP
            Socket tcpSocket = new Socket(receivePacket.getAddress(), port);
            PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
            BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Connected to server. Enter commands in format: <OPER> <ARG1> <ARG2>");
            System.out.println("Operations: ADD, SUB, MUL, DIV");
            System.out.println("Example: ADD 5 3");
            System.out.println("Enter 'exit' to quit");

            String userInput;
            while ((userInput = consoleIn.readLine()) != null) {
                if ("exit".equalsIgnoreCase(userInput)) {
                    break;
                }

                out.println(userInput);
                String response = in.readLine();
                System.out.println("Server response: " + response);
            }

            tcpSocket.close();
            udpSocket.close();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}