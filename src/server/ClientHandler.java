package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
    }


    // =====================================================
    // RUN
    // =====================================================

    @Override
    public void run() {

        try {

            input =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()));


            output =
                    new PrintWriter(
                            socket.getOutputStream(),
                            true);


            // =================================================
            // ASK FOR NAME
            // =================================================

            sendMessage("ENTER_NAME");


            String name =
                    input.readLine();


            if (name == null
                    || name.trim().isEmpty()) {

                sendMessage(
                        "[ERROR] Name cannot be empty.");

                closeConnection();

                return;
            }


            playerName =
                    name.trim();


            // =================================================
            // ADD PLAYER
            // =================================================

            boolean added =
                    gameManager.addPlayer(
                            playerName);


            if (!added) {

                if (gameManager.isGameStarted()) {

                    sendMessage(
                            "[ERROR] Game has already started.");

                } else if (
                        gameManager.getPlayerCount()
                                >= 4) {

                    sendMessage(
                            "[ERROR] Game is full. "
                            + "Maximum 4 players.");

                } else {

                    sendMessage(
                            "[ERROR] This player name "
                            + "is already in use.");
                }


                closeConnection();

                return;
            }


            System.out.println(
                    "Player connected: "
                    + playerName);


            // =================================================
            // BROADCAST JOIN
            // =================================================

            Server.broadcast(
                    "[LOBBY] "
                    + playerName
                    + " joined the game. Players: "
                    + gameManager.getPlayerCount()
                    + "/4",
                    clients);


            // =================================================
            // WELCOME
            // =================================================

            sendMessage("");

            sendMessage(
                    "=================================");

            sendMessage(
                    "       WELCOME "
                    + playerName);

            sendMessage(
                    "=================================");

            sendMessage(
                    "Players: "
                    + gameManager.getPlayerCount()
                    + "/4");

            sendMessage(
                    "Minimum players required: 2");

            sendMessage(
                    "Maximum players allowed: 4");

            sendMessage("");

            sendMessage(
                    "Commands:");

            sendMessage(
                    "start - Start the game");

            sendMessage(
                    "ANSWER A/B/C/D - Answer puzzle");

            sendMessage(
                    "exit - Leave the game");

            sendMessage(
                    "=================================");


            // =================================================
            // MAIN MESSAGE LOOP
            // =================================================

            String message;


            while ((message = input.readLine()) != null) {

                message =
                        message.trim();


                // Ignore empty messages
                if (message.isEmpty()) {
                    continue;
                }


                // =================================================
                // EXIT
                // =================================================

                if (message.equalsIgnoreCase("exit")) {

                    sendMessage(
                            "[SYSTEM] You left the game.");

                    break;
                }


                // =================================================
                // START GAME
                // =================================================

                if (message.equalsIgnoreCase("start")) {


                    if (gameManager.isGameStarted()) {

                        sendMessage(
                                "[GAME] The game "
                                + "has already started.");

                        continue;
                    }


                    if (gameManager.getPlayerCount() < 2) {

                        sendMessage(
                                "[GAME] At least 2 players "
                                + "are required to start.");

                        sendMessage(
                                "[LOBBY] Current players: "
                                + gameManager.getPlayerCount()
                                + "/4");

                        continue;
                    }


                    boolean started =
                            gameManager.startGame();


                    if (started) {

                        Server.broadcast(
                                "",
                                clients);

                        Server.broadcast(
                                "=================================",
                                clients);

                        Server.broadcast(
                                "          GAME STARTED",
                                clients);

                        Server.broadcast(
                                "=================================",
                                clients);

                        Server.broadcast(
                                "[GAME] Entering Room 1...",
                                clients);

                        Server.broadcast(
                                "[GAME] Each player has "
                                + "one individual puzzle.",
                                clients);

                        Server.broadcast(
                                "[GAME] You have 2 attempts.",
                                clients);

                        Server.broadcast(
                                "",
                                clients);


                        // Send Room 1 puzzle
                        for (ClientHandler client :
                                clients) {

                            client.sendAssignedPuzzle();
                        }
                    }


                    continue;
                }


                // =================================================
                // FINAL ROOM
                // =================================================

                /*
                 * Final Room must be checked BEFORE the
                 * normal team-puzzle section because
                 * currentRoom == 3 is also a team puzzle.
                 */

                if (gameManager.getCurrentRoom() == 3
                        && gameManager.isTeamPuzzleActive()) {


                    // Make sure format is FINAL A/B/C/D
                    if (!message
                            .toUpperCase()
                            .startsWith("FINAL ")) {

                        sendMessage(
                                "[FINAL] Please answer using:");

                        sendMessage(
                                "FINAL A/B/C/D");

                        continue;
                    }


                    // Check whether 3 attempts
                    // have already been used
                    if (gameManager.finalAttemptsFinished()) {

                        sendMessage(
                                "[FINAL] Your team has "
                                + "used all 3 attempts.");

                        continue;
                    }


                    // Extract answer
                    String answer =
                            message.substring(6).trim();


                    // Increase shared team attempt
                    gameManager.increaseFinalAttempts();


                    int attemptNumber =
                            gameManager.getFinalAttempts();


                    int remaining =
                            gameManager
                                    .getFinalAttemptsRemaining();


                    // Check answer
                    boolean correct =
                            gameManager.checkFinalPuzzle(
                                    answer);


                    // =================================================
                    // FINAL ANSWER CORRECT
                    // =================================================

                    if (correct) {

                        gameManager.finishTeamPuzzle();


                        Server.broadcast(
                                "",
                                clients);

                        Server.broadcast(
                                "=================================",
                                clients);

                        Server.broadcast(
                                "          ESCAPED!",
                                clients);

                        Server.broadcast(
                                "=================================",
                                clients);

                        Server.broadcast(
                                "[FINAL] Correct answer!",
                                clients);

                        Server.broadcast(
                                "[FINAL] Network sequence verified.",
                                clients);

                        Server.broadcast(
                                "[GAME] Congratulations!",
                                clients);

                        Server.broadcast(
                                "[GAME] Your team escaped the "
                                + "Network Escape Room!",
                                clients);

                        Server.broadcast(
                                "",
                                clients);

                        Server.broadcast(
                                "========== FINAL SCORES ==========",
                                clients);


                        for (Player player :
                                gameManager.getPlayers()) {

                            Server.broadcast(
                                    player.getName()
                                    + " : "
                                    + player.getScore()
                                    + " points",
                                    clients);
                        }


                        Server.broadcast(
                                "=================================",
                                clients);

                        Server.broadcast(
                                "[GAME] GAME COMPLETED!",
                                clients);
                    }


                    // =================================================
                    // FINAL ANSWER WRONG
                    // =================================================

                    else {

                        sendMessage(
                                "[FINAL] Incorrect answer.");

                        sendMessage(
                                "[FINAL] Attempt "
                                + attemptNumber
                                + "/3 used.");


                        // Attempts remain
                        if (remaining > 0) {

                            sendMessage(
                                    "[FINAL] Attempts remaining: "
                                    + remaining);

                            sendMessage(
                                    "[FINAL] Discuss the clues "
                                    + "with your team.");

                            sendMessage(
                                    "[FINAL] Try again using "
                                    + "FINAL A/B/C/D");
                        }


                        // No attempts remain
                        else {

                            gameManager.finishTeamPuzzle();


                            Server.broadcast(
                                    "",
                                    clients);

                            Server.broadcast(
                                    "=================================",
                                    clients);

                            Server.broadcast(
                                    "        GAME OVER",
                                    clients);

                            Server.broadcast(
                                    "=================================",
                                    clients);

                            Server.broadcast(
                                    "[FINAL] Your team used "
                                    + "all 3 attempts.",
                                    clients);

                            Server.broadcast(
                                    "[FINAL] The final puzzle "
                                    + "was not solved.",
                                    clients);

                            Server.broadcast(
                                    "[GAME] The team failed "
                                    + "to escape.",
                                    clients);


                            Server.broadcast(
                                    "",
                                    clients);

                            Server.broadcast(
                                    "========== FINAL SCORES ==========",
                                    clients);


                            for (Player player :
                                    gameManager.getPlayers()) {

                                Server.broadcast(
                                        player.getName()
                                        + " : "
                                        + player.getScore()
                                        + " points",
                                        clients);
                            }


                            Server.broadcast(
                                    "=================================",
                                    clients);

                            Server.broadcast(
                                    "[GAME] GAME OVER.",
                                    clients);
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
                                "[PUZZLE] You have already "
                                + "finished your puzzle.");

                        continue;
                    }


                    // Maximum 2 attempts
                    if (player.getAttempts() >= 2) {

                        sendMessage(
                                "[PUZZLE] You have used "
                                + "both attempts.");

                        continue;
                    }


                    // Extract answer
                    String answer =
                            message.substring(7).trim();


                    // Increase attempt
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


                        sendMessage(
                                "[CLUE] "
                                + puzzle.getClue());


                        sendMessage(
                                "[GAME] You have finished "
                                + "your Room "
                                + gameManager.getCurrentRoom()
                                + " puzzle.");
                    }


                    // =================================================
                    // WRONG
                    // =================================================

                    else {

                        int remaining =
                                2 - player.getAttempts();


                        // First wrong answer
                        if (remaining > 0) {

                            sendMessage(
                                    "[PUZZLE] Wrong answer.");

                            sendMessage(
                                    "[PUZZLE] Attempts remaining: "
                                    + remaining);
                        }


                        // Second wrong answer
                        else {

                            player.setPuzzleFailed(
                                    true);


                            Puzzle puzzle =
                                    player.getAssignedPuzzle();


                            sendMessage(
                                    "[PUZZLE] Wrong answer.");

                            sendMessage(
                                    "[PUZZLE] No attempts remaining.");

                            sendMessage(
                                    "[PUZZLE] You failed this puzzle.");

                            sendMessage(
                                    "[CLUE] "
                                    + puzzle.getClue());

                            sendMessage(
                                    "[SCORE] Your score: "
                                    + player.getScore());

                            sendMessage(
                                    "[GAME] You have finished "
                                    + "your Room "
                                    + gameManager.getCurrentRoom()
                                    + " puzzle.");
                        }
                    }


                    // =================================================
                    // CHECK WHETHER ALL PLAYERS FINISHED
                    // =================================================

                    if (gameManager.allPlayersFinished()) {


                        // -----------------------------------------
                        // ROOM 1
                        // -----------------------------------------

                        if (gameManager.getCurrentRoom() == 1) {

                            gameManager.startTeamPuzzle();


                            Server.broadcast(
                                    "",
                                    clients);

                            Server.broadcast(
                                    "[GAME] All players have "
                                    + "finished their Room 1 puzzles!",
                                    clients);

                            Server.broadcast(
                                    "[GAME] All Room 1 clues "
                                    + "have been collected.",
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
                                    "Use the clues collected "
                                    + "by your team.",
                                    clients);

                            Server.broadcast(
                                    "",
                                    clients);

                            Server.broadcast(
                                    "Format:",
                                    clients);

                            Server.broadcast(
                                    gameManager
                                            .getTeamPuzzleFormat(),
                                    clients);

                            Server.broadcast(
                                    "",
                                    clients);

                            Server.broadcast(
                                    "Example format:",
                                    clients);

                            Server.broadcast(
                                    gameManager
                                            .getTeamPuzzleExample(),
                                    clients);

                            Server.broadcast(
                                    "",
                                    clients);

                            Server.broadcast(
                                    "=================================",
                                    clients);

                            Server.broadcast(
                                    "Type your team answer.",
                                    clients);

                            Server.broadcast(
                                    "=================================",
                                    clients);
                        }


                        // -----------------------------------------
                        // ROOM 2
                        // -----------------------------------------

                        else if (
                                gameManager.getCurrentRoom() == 2) {

                            gameManager.startTeamPuzzle();


                            Server.broadcast(
                                    "",
                                    clients);

                            Server.broadcast(
                                    "[GAME] All players have "
                                    + "finished their Room 2 puzzles!",
                                    clients);

                            Server.broadcast(
                                    "[GAME] All Room 2 clues "
                                    + "have been collected.",
                                    clients);

                            Server.broadcast(
                                    "",
                                    clients);

                            Server.broadcast(
                                    "=================================",
                                    clients);

                            Server.broadcast(
                                    "       ROOM 2 TEAM CHALLENGE",
                                    clients);

                            Server.broadcast(
                                    "=================================",
                                    clients);

                            Server.broadcast(
                                    "Use the routing clues "
                                    + "collected by your team.",
                                    clients);

                            Server.broadcast(
                                    "",
                                    clients);

                            Server.broadcast(
                                    "Your team must reconstruct "
                                    + "the network path.",
                                    clients);

                            Server.broadcast(
                                    "",
                                    clients);

                            Server.broadcast(
                                    "Format:",
                                    clients);

                            Server.broadcast(
                                    gameManager
                                            .getTeamPuzzleFormat(),
                                    clients);

                            Server.broadcast(
                                    "",
                                    clients);

                            Server.broadcast(
                                    "Example format:",
                                    clients);

                            Server.broadcast(
                                    gameManager
                                            .getTeamPuzzleExample(),
                                    clients);

                            Server.broadcast(
                                    "",
                                    clients);

                            Server.broadcast(
                                    "=================================",
                                    clients);

                            Server.broadcast(
                                    "Type your team answer.",
                                    clients);

                            Server.broadcast(
                                    "=================================",
                                    clients);
                        }
                    }


                    continue;
                }


                // =================================================
                // ROOM 1 / ROOM 2 TEAM PUZZLE
                // =================================================

                if (gameManager.isTeamPuzzleActive()) {


                    boolean correct =
                            gameManager.checkTeamPuzzle(
                                    message);


                    // =================================================
                    // CORRECT TEAM ANSWER
                    // =================================================

                    if (correct) {


                        // -----------------------------------------
                        // ROOM 1 → ROOM 2
                        // -----------------------------------------

                        if (gameManager.getCurrentRoom() == 1) {

                            gameManager.finishTeamPuzzle();

                            gameManager.startRoom2();

                            gameManager.assignRoom2Puzzles();


                            Server.broadcast(
                                    "",
                                    clients);

                            Server.broadcast(
                                    "[TEAM] Correct!",
                                    clients);

                            Server.broadcast(
                                    "[TEAM] Connection established!",
                                    clients);

                            Server.broadcast(
                                    "[GAME] Room 1 completed!",
                                    clients);

                            Server.broadcast(
                                    "",
                                    clients);

                            Server.broadcast(
                                    "=================================",
                                    clients);

                            Server.broadcast(
                                    "          ROOM 2",
                                    clients);

                            Server.broadcast(
                                    "=================================",
                                    clients);

                            Server.broadcast(
                                    "[GAME] Entering Room 2...",
                                    clients);

                            Server.broadcast(
                                    "[GAME] New individual "
                                    + "puzzles assigned.",
                                    clients);

                            Server.broadcast(
                                    "[GAME] You have 2 attempts.",
                                    clients);

                            Server.broadcast(
                                    "",
                                    clients);


                            // Send Room 2 puzzles
                            for (ClientHandler client :
                                    clients) {

                                client.sendRoom2Puzzle();
                            }
                        }


                        // -----------------------------------------
                        // ROOM 2 → FINAL ROOM
                        // -----------------------------------------

                        else if (
                                gameManager.getCurrentRoom() == 2) {

                            gameManager.finishTeamPuzzle();

                            gameManager.startFinalRoom();


                            Server.broadcast(
                                    "",
                                    clients);

                            Server.broadcast(
                                    "[TEAM] Correct!",
                                    clients);

                            Server.broadcast(
                                    "[TEAM] Network path reconstructed!",
                                    clients);

                            Server.broadcast(
                                    "[GAME] Room 2 completed!",
                                    clients);

                            Server.broadcast(
                                    "",
                                    clients);

                            Server.broadcast(
                                    "=================================",
                                    clients);

                            Server.broadcast(
                                    "          FINAL ROOM",
                                    clients);

                            Server.broadcast(
                                    "=================================",
                                    clients);

                            Server.broadcast(
                                    "[GAME] You have reached "
                                    + "the Final Room!",
                                    clients);

                            Server.broadcast(
                                    "[GAME] All previous clues "
                                    + "are now available.",
                                    clients);

                            Server.broadcast(
                                    "",
                                    clients);

                            Server.broadcast(
                                    gameManager
                                            .getFinalPuzzleQuestion(),
                                    clients);

                            Server.broadcast(
                                    "",
                                    clients);


                            String[] options =
                                    gameManager
                                            .getFinalPuzzleOptions();


                            for (String option :
                                    options) {

                                Server.broadcast(
                                        option,
                                        clients);
                            }


                            Server.broadcast(
                                    "",
                                    clients);

                            Server.broadcast(
                                    "=================================",
                                    clients);

                            Server.broadcast(
                                    "You have 3 team attempts.",
                                    clients);

                            Server.broadcast(
                                    "Type: FINAL A/B/C/D",
                                    clients);

                            Server.broadcast(
                                    "=================================",
                                    clients);
                        }
                    }


                    // =================================================
                    // WRONG TEAM ANSWER
                    // =================================================

                    else {

                        sendMessage(
                                "[TEAM] Incorrect team answer.");

                        sendMessage(
                                "[TEAM] Check the clues "
                                + "and try again.");
                    }


                    continue;
                }


                // =================================================
                // NORMAL CHAT
                // =================================================

                Server.broadcast(
                        "[" + playerName + "] "
                        + message,
                        clients);
            }


        } catch (IOException e) {

            System.out.println(
                    "Connection error with "
                    + playerName
                    + ": "
                    + e.getMessage());

        } finally {

            disconnect();
        }
    }


    // =====================================================
    // SEND ROOM 1 PUZZLE
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
    // SEND ROOM 2 PUZZLE
    // =====================================================

    public void sendRoom2Puzzle() {

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
                "        YOUR ROOM 2 PUZZLE");

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

    public void sendMessage(
            String message) {

        if (output != null) {

            output.println(message);
        }
    }


    // =====================================================
    // DISCONNECT
    // =====================================================

    private void disconnect() {

        try {

            if (playerName != null) {

                gameManager.removePlayer(
                        playerName);


                Server.broadcast(
                        "[LOBBY] "
                        + playerName
                        + " left the game. Players: "
                        + gameManager.getPlayerCount()
                        + "/4",
                        clients);
            }


            clients.remove(this);


            if (socket != null
                    && !socket.isClosed()) {

                socket.close();
            }


        } catch (IOException e) {

            System.out.println(
                    "Error while disconnecting "
                    + playerName);
        }
    }


    // =====================================================
    // CLOSE CONNECTION
    // =====================================================

    private void closeConnection() {

        try {

            if (socket != null
                    && !socket.isClosed()) {

                socket.close();
            }

        } catch (IOException e) {

            System.out.println(
                    "Error closing connection.");
        }
    }
}