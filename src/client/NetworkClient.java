package client;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;

public class NetworkClient {

    public interface GameEventListener {
        default void onConnected() {}
        default void onDisconnected(String reason) {}
        default void onError(String errorMessage) {}
        default void onLobbyUpdate(List<String> playerList, int count, int max) {}
        default void onGameStarted() {}
        default void onRoomChanged(int roomNumber, String roomTitle) {}
        default void onIndividualPuzzleReceived(int puzzleId, String question, List<String> options) {}
        default void onPuzzleResult(boolean correct, int remainingAttempts, String feedback) {}
        default void onScoreUpdated(int newScore) {}
        default void onClueDiscovered(String clue) {}
        default void onTeamChallengeStarted(int room, String title, String format, String example) {}
        default void onTeamChallengeResult(boolean correct, String message) {}
        default void onFinalRoomChallengeStarted(String question, List<String> options, int remainingAttempts) {}
        default void onFinalRoomResult(boolean correct, int attemptUsed, int remainingAttempts, String message) {}
        default void onGameEnded(boolean escaped, Map<String, Integer> finalScores) {}
        default void onChatMessageReceived(String sender, String message) {}
        default void onSystemLogReceived(String log, String category) {}
    }

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private Thread receiveThread;

    private String playerName = "";
    private String serverHost = "localhost";
    private int serverPort = 5000;
    private boolean connected = false;

    private int currentScore = 0;
    private int currentRoom = 0; // 0=Lobby, 1=Room 1, 2=Room 2, 3=Final Room
    private final List<String> discoveredClues = new CopyOnWriteArrayList<>();
    private final List<GameEventListener> listeners = new CopyOnWriteArrayList<>();

    // Parsing buffers
    private boolean collectingPuzzle = false;
    private int incomingPuzzleId = 0;
    private String incomingQuestion = "";
    private final List<String> incomingOptions = new ArrayList<>();

    private boolean collectingFinalPuzzle = false;
    private String incomingFinalQuestion = "";
    private final List<String> incomingFinalOptions = new ArrayList<>();

    private boolean collectingFinalScores = false;
    private final Map<String, Integer> collectedScores = new LinkedHashMap<>();

    public NetworkClient() {}

    public void addListener(GameEventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(GameEventListener listener) {
        listeners.remove(listener);
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public int getCurrentRoom() {
        return currentRoom;
    }

    public List<String> getDiscoveredClues() {
        return Collections.unmodifiableList(discoveredClues);
    }

    // ==========================================
    // CONNECT TO SERVER
    // ==========================================
    public void connect(String host, int port, String name) {
        this.serverHost = host;
        this.serverPort = port;
        this.playerName = name.trim();
        this.currentScore = 0;
        this.currentRoom = 0;
        this.discoveredClues.clear();

        new Thread(() -> {
            try {
                socket = new Socket(serverHost, serverPort);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);

                // Check for ENTER_NAME prompt
                String firstLine = input.readLine();
                if ("ENTER_NAME".equals(firstLine)) {
                    output.println(playerName);
                } else if (firstLine != null && firstLine.startsWith("[ERROR]")) {
                    notifyError(firstLine);
                    disconnect();
                    return;
                }

                connected = true;
                notifyConnected();

                startListening();

            } catch (IOException e) {
                connected = false;
                notifyError("Could not connect to server at " + serverHost + ":" + serverPort + ". Make sure server is running.");
            }
        }).start();
    }

    // ==========================================
    // DISCONNECT
    // ==========================================
    public void disconnect() {
        try {
            if (connected && output != null) {
                output.println("exit");
            }
        } catch (Exception ignored) {}

        connected = false;
        try {
            if (socket != null && !socket.isClosed()) socket.close();
            if (input != null) input.close();
            if (output != null) output.close();
        } catch (Exception ignored) {}

        notifyDisconnected("Disconnected from server");
    }

    // ==========================================
    // OUTGOING COMMANDS
    // ==========================================
    public void startGame() {
        sendRawMessage("start");
    }

    public void sendAnswer(String optionKey) {
        sendRawMessage("ANSWER " + optionKey.toUpperCase().trim());
    }

    public void sendTeamAnswer(String answerText) {
        sendRawMessage(answerText.trim());
    }

    public void sendFinalAnswer(String optionKey) {
        sendRawMessage("FINAL " + optionKey.toUpperCase().trim());
    }

    public void sendChatMessage(String chat) {
        if (chat != null && !chat.trim().isEmpty()) {
            sendRawMessage(chat.trim());
        }
    }

    private void sendRawMessage(String msg) {
        if (output != null && connected) {
            output.println(msg);
        }
    }

    // ==========================================
    // INCOMING MESSAGE LISTENER
    // ==========================================
    private void startListening() {
        receiveThread = new Thread(() -> {
            try {
                String line;
                while ((line = input.readLine()) != null) {
                    processIncomingLine(line);
                }
            } catch (IOException e) {
                if (connected) {
                    connected = false;
                    notifyDisconnected("Connection lost to server.");
                }
            }
        });
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    // ==========================================
    // PROCESS PROTOCOL MESSAGES
    // ==========================================
    private void processIncomingLine(String line) {
        String trimmed = line.trim();

        // Pass raw to system log
        notifySystemLog(line);

        // 1. Error messages
        if (trimmed.startsWith("[ERROR]")) {
            notifyError(trimmed.replace("[ERROR]", "").trim());
            return;
        }

        // 2. Lobby Updates
        if (trimmed.startsWith("[LOBBY]")) {
            String lobbyMsg = trimmed.replace("[LOBBY]", "").trim();
            // Example: Alice joined the game. Players: 2/4
            notifyLobbyMessage(lobbyMsg);
            return;
        }

        // 3. Game Start & Room Transitions
        if (trimmed.equals("GAME STARTED")) {
            currentRoom = 1;
            notifyGameStarted();
            notifyRoomChanged(1, "Room 1: Initial Access & Configuration");
            return;
        }

        if (trimmed.contains("Entering Room 1...")) {
            currentRoom = 1;
            notifyRoomChanged(1, "Room 1: Initial Access & Configuration");
            return;
        }

        if (trimmed.contains("Entering Room 2...")) {
            currentRoom = 2;
            notifyRoomChanged(2, "Room 2: Network Topology & Routing");
            return;
        }

        if (trimmed.contains("Entering Final Room.") || trimmed.contains("You have reached the Final Room!")) {
            currentRoom = 3;
            notifyRoomChanged(3, "Final Room: Network Sequence Escape");
            return;
        }

        // 4. Individual Puzzle Headers
        if (trimmed.contains("YOUR ROOM 1 PUZZLE") || trimmed.contains("YOUR ROOM 2 PUZZLE")) {
            collectingPuzzle = true;
            incomingPuzzleId = 0;
            incomingQuestion = "";
            incomingOptions.clear();
            return;
        }

        if (collectingPuzzle) {
            if (trimmed.startsWith("Puzzle ID:")) {
                try {
                    incomingPuzzleId = Integer.parseInt(trimmed.replace("Puzzle ID:", "").trim());
                } catch (Exception ignored) {}
                return;
            }
            if (trimmed.startsWith("Question:")) {
                incomingQuestion = trimmed.replace("Question:", "").trim();
                return;
            }
            if (trimmed.matches("^[A-D]\\..*")) {
                incomingOptions.add(trimmed);
                return;
            }
            if (trimmed.contains("Type: ANSWER A/B/C/D")) {
                collectingPuzzle = false;
                notifyIndividualPuzzle(incomingPuzzleId, incomingQuestion, new ArrayList<>(incomingOptions));
                return;
            }
        }

        // 5. Individual Puzzle Results
        if (trimmed.startsWith("[PUZZLE]")) {
            String pMsg = trimmed.replace("[PUZZLE]", "").trim();
            if (pMsg.equalsIgnoreCase("Correct!")) {
                notifyPuzzleResult(true, 2, "Correct Answer! Puzzle Solved.");
            } else if (pMsg.contains("Wrong answer.")) {
                int rem = 1;
                if (pMsg.contains("Attempts remaining: 1")) rem = 1;
                else if (pMsg.contains("No attempts remaining") || pMsg.contains("failed this puzzle")) rem = 0;
                notifyPuzzleResult(false, rem, pMsg);
            }
            return;
        }

        // 6. Score & Clue
        if (trimmed.startsWith("[SCORE]")) {
            try {
                String scoreStr = trimmed.replaceAll("[^0-9]", "");
                currentScore = Integer.parseInt(scoreStr);
                notifyScoreUpdated(currentScore);
            } catch (Exception ignored) {}
            return;
        }

        if (trimmed.startsWith("[CLUE]")) {
            String clueText = trimmed.replace("[CLUE]", "").trim();
            if (!discoveredClues.contains(clueText)) {
                discoveredClues.add(clueText);
            }
            notifyClueDiscovered(clueText);
            return;
        }

        // 7. Team Challenge
        if (trimmed.contains("ROOM 1 TEAM CHALLENGE") || trimmed.contains("ROOM 2 TEAM CHALLENGE")) {
            int room = trimmed.contains("ROOM 1") ? 1 : 2;
            String format = (room == 1) ? "CONNECT <IP> <PORT> [USER] [PASS]" : "ROUTE <PATH> HOPS <NUM> [TTL] [DNS]";
            String example = (room == 1) ? "CONNECT 192.168.1.10 80" : "ROUTE A-C-E HOPS 2";
            notifyTeamChallengeStarted(room, "Room " + room + " Team Challenge", format, example);
            return;
        }

        if (trimmed.startsWith("[TEAM]")) {
            String tMsg = trimmed.replace("[TEAM]", "").trim();
            boolean isCorrect = tMsg.toLowerCase().contains("correct") || tMsg.toLowerCase().contains("established") || tMsg.toLowerCase().contains("reconstructed");
            notifyTeamChallengeResult(isCorrect, tMsg);
            return;
        }

        // 8. Final Room Puzzle
        if (currentRoom == 3 && trimmed.contains("A client wants to reach the game server")) {
            collectingFinalPuzzle = true;
            incomingFinalQuestion = trimmed;
            incomingFinalOptions.clear();
            return;
        }

        if (collectingFinalPuzzle) {
            if (trimmed.matches("^[A-D]\\..*")) {
                incomingFinalOptions.add(trimmed);
            }
            if (trimmed.contains("Type: FINAL A/B/C/D")) {
                collectingFinalPuzzle = false;
                notifyFinalRoomChallenge(incomingFinalQuestion, new ArrayList<>(incomingFinalOptions), 3);
            }
            return;
        }

        if (trimmed.startsWith("[FINAL]")) {
            String fMsg = trimmed.replace("[FINAL]", "").trim();
            if (fMsg.contains("Correct answer!")) {
                notifyFinalRoomResult(true, 1, 3, "Network sequence verified! Escape Successful!");
            } else if (fMsg.contains("Incorrect answer")) {
                int remaining = 2;
                if (fMsg.contains("2/3 used")) remaining = 1;
                else if (fMsg.contains("3/3 used") || fMsg.contains("all 3 attempts")) remaining = 0;
                notifyFinalRoomResult(false, 3 - remaining, remaining, fMsg);
            }
            return;
        }

        // 9. Escape Victory & Game Over
        if (trimmed.contains("ESCAPED!")) {
            collectingFinalScores = true;
            collectedScores.clear();
            return;
        }

        if (trimmed.contains("GAME OVER")) {
            collectingFinalScores = true;
            collectedScores.clear();
            return;
        }

        if (collectingFinalScores) {
            if (trimmed.contains(":") && trimmed.contains("points")) {
                try {
                    String[] parts = trimmed.split(":");
                    String pName = parts[0].trim();
                    int pts = Integer.parseInt(parts[1].replace("points", "").trim());
                    collectedScores.put(pName, pts);
                } catch (Exception ignored) {}
            }
            if (trimmed.contains("GAME COMPLETED!") || trimmed.contains("GAME OVER.")) {
                collectingFinalScores = false;
                boolean escaped = !trimmed.contains("GAME OVER.");
                notifyGameEnded(escaped, new LinkedHashMap<>(collectedScores));
            }
            return;
        }

        // 10. Player Chat message: "[Alice] Hello"
        if (trimmed.matches("^\\[[^\\]]+\\].*") && !trimmed.startsWith("[GAME]") && !trimmed.startsWith("[LOBBY]") && !trimmed.startsWith("[PUZZLE]") && !trimmed.startsWith("[TEAM]") && !trimmed.startsWith("[FINAL]") && !trimmed.startsWith("[SCORE]") && !trimmed.startsWith("[CLUE]") && !trimmed.startsWith("[SYSTEM]")) {
            int closingBracket = trimmed.indexOf(']');
            String sender = trimmed.substring(1, closingBracket);
            String message = trimmed.substring(closingBracket + 1).trim();
            notifyChatMessage(sender, message);
        }
    }

    // ==========================================
    // NOTIFY LISTENERS (SWING EDT SAFE)
    // ==========================================
    private void notifyConnected() {
        SwingUtilities.invokeLater(() -> {
            for (GameEventListener l : listeners) l.onConnected();
        });
    }

    private void notifyDisconnected(String reason) {
        SwingUtilities.invokeLater(() -> {
            for (GameEventListener l : listeners) l.onDisconnected(reason);
        });
    }

    private void notifyError(String err) {
        SwingUtilities.invokeLater(() -> {
            for (GameEventListener l : listeners) l.onError(err);
        });
    }

    private void notifyLobbyMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            // Extract player count if available
            int count = 1;
            int max = 4;
            if (msg.contains("Players: ")) {
                try {
                    String sub = msg.substring(msg.indexOf("Players: ") + 9);
                    String[] parts = sub.split("/");
                    count = Integer.parseInt(parts[0].trim());
                    max = Integer.parseInt(parts[1].replaceAll("[^0-9]", "").trim());
                } catch (Exception ignored) {}
            }
            List<String> mockList = new ArrayList<>();
            mockList.add(playerName);
            for (GameEventListener l : listeners) l.onLobbyUpdate(mockList, count, max);
        });
    }

    private void notifyGameStarted() {
        SwingUtilities.invokeLater(() -> {
            for (GameEventListener l : listeners) l.onGameStarted();
        });
    }

    private void notifyRoomChanged(int room, String title) {
        SwingUtilities.invokeLater(() -> {
            for (GameEventListener l : listeners) l.onRoomChanged(room, title);
        });
    }

    private void notifyIndividualPuzzle(int id, String q, List<String> opts) {
        SwingUtilities.invokeLater(() -> {
            for (GameEventListener l : listeners) l.onIndividualPuzzleReceived(id, q, opts);
        });
    }

    private void notifyPuzzleResult(boolean correct, int remAttempts, String feedback) {
        SwingUtilities.invokeLater(() -> {
            for (GameEventListener l : listeners) l.onPuzzleResult(correct, remAttempts, feedback);
        });
    }

    private void notifyScoreUpdated(int score) {
        SwingUtilities.invokeLater(() -> {
            for (GameEventListener l : listeners) l.onScoreUpdated(score);
        });
    }

    private void notifyClueDiscovered(String clue) {
        SwingUtilities.invokeLater(() -> {
            for (GameEventListener l : listeners) l.onClueDiscovered(clue);
        });
    }

    private void notifyTeamChallengeStarted(int room, String title, String fmt, String ex) {
        SwingUtilities.invokeLater(() -> {
            for (GameEventListener l : listeners) l.onTeamChallengeStarted(room, title, fmt, ex);
        });
    }

    private void notifyTeamChallengeResult(boolean correct, String msg) {
        SwingUtilities.invokeLater(() -> {
            for (GameEventListener l : listeners) l.onTeamChallengeResult(correct, msg);
        });
    }

    private void notifyFinalRoomChallenge(String q, List<String> opts, int rem) {
        SwingUtilities.invokeLater(() -> {
            for (GameEventListener l : listeners) l.onFinalRoomChallengeStarted(q, opts, rem);
        });
    }

    private void notifyFinalRoomResult(boolean correct, int used, int rem, String msg) {
        SwingUtilities.invokeLater(() -> {
            for (GameEventListener l : listeners) l.onFinalRoomResult(correct, used, rem, msg);
        });
    }

    private void notifyGameEnded(boolean escaped, Map<String, Integer> scores) {
        SwingUtilities.invokeLater(() -> {
            for (GameEventListener l : listeners) l.onGameEnded(escaped, scores);
        });
    }

    private void notifyChatMessage(String sender, String msg) {
        SwingUtilities.invokeLater(() -> {
            for (GameEventListener l : listeners) l.onChatMessageReceived(sender, msg);
        });
    }

    private void notifySystemLog(String line) {
        SwingUtilities.invokeLater(() -> {
            String cat = "INFO";
            if (line.startsWith("[GAME]")) cat = "GAME";
            else if (line.startsWith("[PUZZLE]")) cat = "PUZZLE";
            else if (line.startsWith("[TEAM]")) cat = "TEAM";
            else if (line.startsWith("[FINAL]")) cat = "FINAL";
            else if (line.startsWith("[LOBBY]")) cat = "LOBBY";
            else if (line.startsWith("[CLUE]")) cat = "CLUE";
            else if (line.startsWith("[ERROR]")) cat = "ERROR";
            for (GameEventListener l : listeners) l.onSystemLogReceived(line, cat);
        });
    }
}
