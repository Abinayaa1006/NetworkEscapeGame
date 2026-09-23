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


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public ClientHandler(
            Socket socket,
            List<ClientHandler> clients,
            GameManager gameManager) {

        this.socket = socket;
        this.clients = clients;
        this.gameManager = gameManager;

        try {

            input =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()));

            output =
                    new PrintWriter(
                            socket.getOutputStream(),
                            true);

        } catch (IOException e) {

            System.out.println(
                    "Error creating client handler.");
        }
    }


    // =====================================================
    // RUN
    // =====================================================

    @Override
    public void run() {

        try {

            // Ask for name
            sendMessage("ENTER_NAME");

            playerName =
                    input.readLine();


            // =================================================
            // ADD PLAYER
            // =================================================

            boolean added =
                    gameManager.addPlayer(
                            playerName);


            if (!added) {

                if (gameManager.isGameStarted()) {

                    sendMessage(
                            "[LOBBY] Game has already "
                            + "started. You cannot "
                            + "join this game.");

                } else {

                    sendMessage(
                            "[LOBBY] Game is full. "
                            + "Maximum 4 players "
                            + "allowed.");
                }

                socket.close();

                return;
            }


            System.out.println(
                    playerName
                    + " joined the game.");


            // Welcome
            sendMessage(
                    "WELCOME "
                    + playerName);


            // Inform everyone
            Server.broadcast(
                    "[SYSTEM] "
                    + playerName
                    + " joined the lobby. "
                    + "Players: "
                    + gameManager.getPlayerCount()
                    + "/4",
                    clients);


            sendMessage(
                    "[LOBBY] Waiting for game "
                    + "to start.");


            // =================================================
            // MESSAGE LOOP
            // =================================================

            String message;

            while ((message =
                    input.readLine()) != null) {


                // =================================================
                // EXIT
                // =================================================

                if (message.equalsIgnoreCase("exit")) {

                    break;
                }


                // =================================================
                // START GAME
                // =================================================

                if (message.equalsIgnoreCase("start")) {


                    // Game already started
                    if (gameManager.isGameStarted()) {

                        sendMessage(
                                "[GAME] The game has "
                                + "already started.");

                        continue;
                    }


                    // Less than 2 players
                    if (gameManager.getPlayerCount()
                            < 2) {

                        sendMessage(
                                "[GAME] At least 2 "
                                + "players are required "
                                + "to start the game.");

                        sendMessage(
                                "[LOBBY] Current players: "
                                + gameManager.getPlayerCount()
                                + "/4");

                        continue;
                    }


                    // Start game
                    boolean started =
                            gameManager.startGame();


                    if (started) {

                        Server.broadcast(
                                "[GAME] Game started!",
                                clients);

                        Server.broadcast(
                                "[GAME] Entering Room 1...",
                                clients);


                        // Send individual puzzle
                        // to every player
                        for (ClientHandler client
                                : clients) {

                            client.sendAssignedPuzzle();
                        }
                    }

                    continue;
                }


                // =================================================
                // INDIVIDUAL PUZZLE ANSWER
                // =================================================

                if (message
                        .toUpperCase()
                        .startsWith("ANSWER ")) {


                    Player player =
                            gameManager.getPlayer(
                                    playerName);


                    if (player == null) {

                        continue;
                    }


                    // Already finished
                    if (player.isPuzzleSolved()
                            || player.isPuzzleFailed()) {

                        sendMessage(
                                "[PUZZLE] You have "
                                + "already finished "
                                + "your puzzle.");

                        continue;
                    }


                    // Maximum 2 attempts
                    if (player.getAttempts() >= 2) {

                        sendMessage(
                                "[PUZZLE] You have "
                                + "used both attempts.");

                        continue;
                    }


                    // Extract answer
                    String answer =
                            message.substring(7)
                                    .trim();


                    // Count attempt
                    player.increaseAttempts();


                    // Check answer
                    boolean correct =
                            gameManager.checkPlayerAnswer(
                                    playerName,
                                    answer);


                    // =================================================
                    // CORRECT
                    // =================================================

                    if (correct) {

                        gameManager.solvePuzzle(
                                playerName);


                        Puzzle puzzle =
                                player.getAssignedPuzzle();


                        sendMessage(
                                "[PUZZLE] Correct!");


                        sendMessage(
                                "[SCORE] Your score: "
                                + player.getScore());


                        // Give clue
                        sendMessage(
                                "[CLUE] "
                                + puzzle.getClue());


                        sendMessage(
                                "[GAME] You have "
                                + "finished your "
                                + "Room 1 puzzle.");
                    }


                    // =================================================
                    // WRONG
                    // =================================================

                    else {

                        int remaining =
                                2 - player.getAttempts();


                        if (remaining > 0) {

                            sendMessage(
                                    "[PUZZLE] Wrong "
                                    + "answer.");

                            sendMessage(
                                    "[PUZZLE] Attempts "
                                    + "remaining: "
                                    + remaining);
                        }


                        // Both attempts used
                        else {

                            player.setPuzzleFailed(
                                    true);


                            Puzzle puzzle =
                                    player.getAssignedPuzzle();


                            sendMessage(
                                    "[PUZZLE] Wrong "
                                    + "answer.");

                            sendMessage(
                                    "[PUZZLE] No attempts "
                                    + "remaining.");

                            sendMessage(
                                    "[PUZZLE] You failed "
                                    + "this puzzle.");


                            // Failed player ALSO
                            // receives clue
                            sendMessage(
                                    "[CLUE] "
                                    + puzzle.getClue());


                            sendMessage(
                                    "[SCORE] Your score: 0");


                            sendMessage(
                                    "[GAME] You have "
                                    + "finished your "
                                    + "Room 1 puzzle.");
                        }
                    }


                    // =================================================
                    // CHECK IF EVERYONE FINISHED
                    // =================================================

                    if (gameManager
                            .allPlayersFinished()) {


                        gameManager
                                .startTeamPuzzle();


                        Server.broadcast(
                                "[GAME] All players "
                                + "have finished their "
                                + "Room 1 puzzles!",
                                clients);


                        Server.broadcast(
                                "[GAME] All Room 1 "
                                + "clues have been "
                                + "collected.",
                                clients);


                        Server.broadcast(
                                "",
                                clients);


                        Server.broadcast(
                                "=================================",
                                clients);


                        Server.broadcast(
                                "       ROOM 1 TEAM CHALLENGE",
                                clients);


                        Server.broadcast(
                                "=================================",
                                clients);


                        Server.broadcast(
                                "Use the clues "
                                + "collected by your team.",
                                clients);


                        Server.broadcast(
                                "Format:",
                                clients);


                        Server.broadcast(
                                gameManager
                                        .getTeamPuzzleFormat(),
                                clients);


                        Server.broadcast(
                                "Example:",
                                clients);


                        Server.broadcast(
                                gameManager
                                        .getTeamPuzzleExample(),
                                clients);


                        Server.broadcast(
                                "=================================",
                                clients);
                    }


                    continue;
                }


                // =================================================
                // TEAM PUZZLE
                // =================================================

                if (gameManager
                        .isTeamPuzzleActive()) {


                    boolean correct =
                            gameManager
                                    .checkTeamPuzzle(
                                            message);


                    if (correct) {


                        gameManager
                                .finishTeamPuzzle();


                        Server.broadcast(
                                "[TEAM] Correct!",
                                clients);


                        Server.broadcast(
                                "[TEAM] Connection "
                                + "established!",
                                clients);


                        Server.broadcast(
                                "[GAME] Room 1 "
                                + "completed!",
                                clients);


                        Server.broadcast(
                                "[GAME] Preparing "
                                + "Room 2...",
                                clients);
                    }


                    else {

                        sendMessage(
                                "[TEAM] Incorrect "
                                + "team answer.");

                        sendMessage(
                                "[TEAM] Check the "
                                + "clues and try again.");
                    }


                    continue;
                }


                // =================================================
                // NORMAL CHAT
                // =================================================

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


            // Remove player from game
            if (playerName != null) {

                gameManager.removePlayer(
                        playerName);
            }


            // Remove handler
            clients.remove(this);


            // Tell remaining players
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


            // Close socket
            try {

                socket.close();

            } catch (IOException e) {

                System.out.println(
                        "Error closing connection.");
            }
        }
    }


    // =====================================================
    // SEND ASSIGNED PUZZLE
    // =====================================================

    public void sendAssignedPuzzle() {

        Player player =
                gameManager.getPlayer(
                        playerName);


        if (player == null) {
            return;
        }


        Puzzle puzzle =
                player.getAssignedPuzzle();


        if (puzzle == null) {
            return;
        }


        sendMessage("");

        sendMessage(
                "=================================");

        sendMessage(
                "        YOUR ROOM 1 PUZZLE");

        sendMessage(
                "=================================");


        sendMessage(
                "Puzzle ID: "
                + puzzle.getPuzzleId());


        sendMessage(
                "Question: "
                + puzzle.getQuestion());


        for (String option :
                puzzle.getOptions()) {

            sendMessage(option);
        }


        sendMessage(
                "=================================");

        sendMessage(
                "You have 2 attempts.");

        sendMessage(
                "Type: ANSWER A/B/C/D");

        sendMessage(
                "=================================");
    }


    // =====================================================
    // SEND MESSAGE
    // =====================================================

    public void sendMessage(String message) {

        output.println(message);
    }
}