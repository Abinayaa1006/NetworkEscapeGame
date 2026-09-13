package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    // Server port
    private static final int PORT = 5000;

    // List of connected clients
    private static final List<ClientHandler> clients =
            new ArrayList<>();

    // One GameManager for the whole server
    private static final GameManager gameManager =
            new GameManager();


    public static void main(String[] args) {

        System.out.println("=================================");
        System.out.println("   NETWORK ESCAPE ROOM SERVER");
        System.out.println("=================================");

        try (ServerSocket serverSocket =
                     new ServerSocket(PORT)) {

            System.out.println(
                    "Server started on port " + PORT);

            System.out.println(
                    "Waiting for players...\n");


            // Continuously accept clients
            while (true) {

                Socket socket =
                        serverSocket.accept();


                System.out.println(
                        "New client connected: "
                        + socket.getInetAddress());


                // Create ClientHandler
                ClientHandler clientHandler =
                        new ClientHandler(
                                socket,
                                clients,
                                gameManager);


                // Add handler to client list
                clients.add(clientHandler);


                // Start thread
                clientHandler.start();
            }


        } catch (IOException e) {

            System.out.println(
                    "Server error: "
                    + e.getMessage());
        }
    }


    // =========================
    // BROADCAST
    // =========================

    public static void broadcast(
            String message,
            List<ClientHandler> clients) {

        for (ClientHandler client : clients) {

            client.sendMessage(message);
        }
    }
}

