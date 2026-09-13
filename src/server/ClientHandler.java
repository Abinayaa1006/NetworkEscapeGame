package server;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler extends Thread {

    private Socket socket;

    private BufferedReader input;
    private PrintWriter output;

    private List<ClientHandler> clients;

    private GameManager gameManager;

    private String playerName;


    public ClientHandler(
            Socket socket,
            List<ClientHandler> clients,
            GameManager gameManager) {

        this.socket = socket;
        this.clients = clients;
        this.gameManager = gameManager;

        try {

            // Receive messages from client
            input = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));

            // Send messages to client
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

            // Ask for player name
            sendMessage("ENTER_NAME");

            // Receive player name
            playerName = input.readLine();


            // Add player to GameManager
            boolean added =
        gameManager.addPlayer(playerName);

if (!added) {

    if (gameManager.isGameStarted()) {

        sendMessage(
                "[LOBBY] Game has already started. "
                + "You cannot join this game.");

    } else {

        sendMessage(
                "[LOBBY] Game is full. "
                + "Maximum 4 players allowed.");
    }

    socket.close();

    return;
}


            // =========================
            // WELCOME
            // =========================

            sendMessage(
                    "WELCOME " + playerName);


            // Tell everyone about new player
            Server.broadcast(
                    "[SYSTEM] "
                    + playerName
                    + " joined the lobby. "
                    + "Players: "
                    + gameManager.getPlayerCount()
                    + "/4",
                    clients);


            // Tell player to wait
            sendMessage(
                    "[LOBBY] Waiting for game to start.");


            String message;


            // =========================
            // RECEIVE MESSAGES
            // =========================

            while ((message = input.readLine()) != null) {


                // =========================
                // EXIT
                // =========================

                if (message.equalsIgnoreCase("exit")) {

                    break;
                }


                // =========================
                // START GAME
                // =========================

                if (message.equalsIgnoreCase("start")) {

    // Check if game has already started
    if (gameManager.isGameStarted()) {

        sendMessage(
                "[GAME] The game has already started.");

        continue;
    }

    // Check minimum player requirement
    if (gameManager.getPlayerCount() < 2) {

        sendMessage(
                "[GAME] At least 2 players are required "
                + "to start the game.");

        sendMessage(
                "[LOBBY] Current players: "
                + gameManager.getPlayerCount()
                + "/4");

        continue;
    }

    // Start the game
    boolean started =
            gameManager.startGame();

    if (started) {

        Server.broadcast(
                "[GAME] Game started!",
                clients);

        Server.broadcast(
                "[GAME] Entering Room 1...",
                clients);

        // Send each player their own puzzle
        for (ClientHandler client : clients) {

            client.sendAssignedPuzzle();
        }

    }
                    continue;
                }

                if (message.toUpperCase().startsWith("ANSWER ")) {

    String answer =
            message.substring(7).trim();

    Player player =
            gameManager.getPlayer(playerName);

    // Already solved or used both attempts
    if (player.isPuzzleSolved()) {

        sendMessage(
                "[PUZZLE] You have already solved this puzzle.");

        continue;
    }

    if (player.getAttempts() >= 2) {

        sendMessage(
                "[PUZZLE] You have used both attempts.");

        continue;
    }

    // Count this attempt
    player.increaseAttempts();

    boolean correct =
            gameManager.checkPlayerAnswer(
                    playerName,
                    answer);

    if (correct) {

        gameManager.solvePuzzle(playerName);

        Puzzle puzzle =
                player.getAssignedPuzzle();

        sendMessage(
                "[PUZZLE] Correct!");

        sendMessage(
                "[SCORE] Your score: "
                + player.getScore());

        sendMessage(
                "[CLUE] "
                + puzzle.getClue());

    } else {

        int remaining =
                2 - player.getAttempts();

        if (remaining > 0) {

            sendMessage(
                    "[PUZZLE] Wrong answer.");

            sendMessage(
                    "[PUZZLE] Attempts remaining: "
                    + remaining);

        } else {

            sendMessage(
                    "[PUZZLE] Wrong answer.");

            sendMessage(
                    "[PUZZLE] No attempts remaining.");

            sendMessage(
                    "[PUZZLE] You failed this puzzle.");
        }
    }

    continue;
}

                // =========================
                // NORMAL CHAT
                // =========================

                System.out.println(
                        playerName
                        + ": "
                        + message);


                Server.broadcast(
                        playerName
                        + ": "
                        + message,
                        clients);
            }


        } catch (IOException e) {

            System.out.println(
                    playerName
                    + " disconnected.");

        } finally {

            // Remove player from GameManager
            if (playerName != null) {

                gameManager.removePlayer(
                        playerName);
            }

            // Remove client
            clients.remove(this);


            // Inform remaining players
            if (playerName != null) {

                Server.broadcast(
                        "[SYSTEM] "
                        + playerName
                        + " left the game. "
                        + "Players: "
                        + gameManager.getPlayerCount()
                        + "/4",
                        clients);
            }


            try {

                socket.close();

            } catch (IOException e) {

                System.out.println(
                        "Error closing connection.");
            }
        }
    }

    public void sendAssignedPuzzle() {

    Player player =
            gameManager.getPlayer(playerName);

    if (player == null) {
        return;
    }

    Puzzle puzzle =
            player.getAssignedPuzzle();

    if (puzzle == null) {
        return;
    }

    sendMessage("");
    sendMessage("=================================");
    sendMessage("        YOUR ROOM 1 PUZZLE");
    sendMessage("=================================");

    sendMessage("Puzzle ID: "
            + puzzle.getPuzzleId());

    sendMessage("Question: "
            + puzzle.getQuestion());

    for (String option : puzzle.getOptions()) {
        sendMessage(option);
    }

    sendMessage("=================================");
    sendMessage("Type: ANSWER A/B/C/D");
    sendMessage("=================================");
}


    // =========================
    // SEND MESSAGE
    // =========================

    public void sendMessage(String message) {

        output.println(message);
    }
}

