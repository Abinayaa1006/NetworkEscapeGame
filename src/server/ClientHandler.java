package server;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler extends Thread {

    private Socket socket;

    private BufferedReader input;
    private PrintWriter output;

    private List<ClientHandler> clients;

    private String playerName;

    public ClientHandler(
            Socket socket,
            List<ClientHandler> clients) {

        this.socket = socket;
        this.clients = clients;

        try {

            // Receive data from client
            input = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));

            // Send data to client
            output = new PrintWriter(
                    socket.getOutputStream(), true);

        } catch (IOException e) {

            System.out.println(
                    "Error creating client handler.");
        }
    }

    @Override
    public void run() {

        try {

            // Ask client for player name
            sendMessage("ENTER_NAME");

            // Read player name
            playerName = input.readLine();

            System.out.println(
                    playerName + " joined the game.");

            // Tell this player that they joined
            sendMessage(
                    "WELCOME " + playerName);

            // Tell everyone that a new player joined
            Server.broadcast(
                    playerName + " joined the game.",
                    clients);

            String message;

            // Continuously receive messages
            while ((message = input.readLine()) != null) {

                System.out.println(
                        playerName + ": " + message);

                // Send message to all players
                Server.broadcast(
                        playerName + ": " + message,
                        clients);
            }

        } catch (IOException e) {

            System.out.println(
                    playerName + " disconnected.");

        } finally {

            try {

                // Remove client from list
                clients.remove(this);

                // Tell remaining players
                if (playerName != null) {

                    Server.broadcast(
                            playerName + " left the game.",
                            clients);
                }

                socket.close();

            } catch (IOException e) {

                System.out.println(
                        "Error closing connection.");
            }
        }
    }

    // Send a message to this particular client
    public void sendMessage(String message) {

        output.println(message);
    }
}

