package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    // Server address
    private static final String SERVER_ADDRESS = "localhost";

    // Server port
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) {

        System.out.println("=================================");
        System.out.println("   NETWORK ESCAPE ROOM CLIENT");
        System.out.println("=================================");

        try {

            // Connect to server
            Socket socket =
                    new Socket(SERVER_ADDRESS, SERVER_PORT);

            System.out.println(
                    "Connected to server!\n");

            // Receive messages from server
            BufferedReader input =
        new BufferedReader(
                new InputStreamReader(
                        socket.getInputStream()));

PrintWriter output =
        new PrintWriter(
                socket.getOutputStream(), true);

Scanner scanner = new Scanner(System.in);


// First wait for the server's name request
String firstMessage = input.readLine();

if (firstMessage.equals("ENTER_NAME")) {

    System.out.print("Enter your name: ");

    String name = scanner.nextLine();

    output.println(name);
}


// NOW start receiving messages
Thread receiveThread = new Thread(() -> {

    try {

        String message;

        while ((message = input.readLine()) != null) {

            System.out.println(
                    "\n" + message);

            System.out.print("> ");
        }

    } catch (IOException e) {

        System.out.println(
                "\nDisconnected from server.");
    }
});

receiveThread.start();

            // Continuously send messages
            while (true) {

                System.out.print("> ");

                String message = scanner.nextLine();

                // Type exit to disconnect
                if (message.equalsIgnoreCase("exit")) {

                    break;
                }

                output.println(message);
            }

            socket.close();
            scanner.close();

            System.out.println(
                    "Disconnected from server.");

        } catch (IOException e) {

            System.out.println(
                    "Could not connect to server.");

            System.out.println(
                    "Make sure the server is running.");
        }
    }
}


    
