package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    // Port number on which the server will listen
    private static final int PORT = 5000;

    // List of all connected clients
    private static final List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {

        System.out.println("=================================");
        System.out.println("   NETWORK ESCAPE ROOM SERVER");
        System.out.println("=================================");

        try(ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("Server started on port " + PORT);
            System.out.println("Waiting for players...\n");

            // Keep accepting clients
            while (true) {

                Socket socket = serverSocket.accept();

                System.out.println(
                        "New client connected: "
                        + socket.getInetAddress()
                );

                // Create a handler for this client
                ClientHandler clientHandler =
                        new ClientHandler(socket, clients);

                // Add client to list
                clients.add(clientHandler);

                // Start a separate thread for this client
                clientHandler.start();
            }

        } catch (IOException e) {

            System.out.println("Server error: " + e.getMessage());

        }
    }

    // Send a message to every connected client
    public static void broadcast(
            String message,
            List<ClientHandler> clients) {

        for (ClientHandler client : clients) {

            client.sendMessage(message);
        }
    }
}


