package server;

import java.util.ArrayList;
import java.util.List;

public class GameManager {

    private static final int MAX_PLAYERS = 4;
    private static final int MIN_PLAYERS = 2;

    private final List<Player> players =
            new ArrayList<>();

    // 0 = Lobby
    // 1 = Room 1
    // 2 = Room 2
    // 3 = Final Room
    private int currentRoom = 0;

    private boolean gameStarted = false;

    private boolean teamPuzzleActive = false;


    // =====================================================
    // ADD PLAYER
    // =====================================================

    public synchronized boolean addPlayer(String name) {

        // Do not allow new players after game starts
        if (gameStarted) {
            return false;
        }

        // Maximum 4 players
        if (players.size() >= MAX_PLAYERS) {
            return false;
        }

        Player player = new Player(name);

        players.add(player);

        System.out.println(
                "Player added: " + name);

        return true;
    }


    // =====================================================
    // REMOVE PLAYER
    // =====================================================

    public synchronized void removePlayer(String name) {

        players.removeIf(
                player -> player.getName().equals(name));

        System.out.println(
                "Player removed: " + name);
    }


    // =====================================================
    // PLAYER COUNT
    // =====================================================

    public synchronized int getPlayerCount() {
        return players.size();
    }


    // =====================================================
    // GET PLAYER
    // =====================================================

    public synchronized Player getPlayer(String name) {

        for (Player player : players) {

            if (player.getName().equals(name)) {
                return player;
            }
        }

        return null;
    }


    // =====================================================
    // START GAME
    // =====================================================

    public synchronized boolean startGame() {

        if (gameStarted) {
            return false;
        }

        // At least 2 players required
        if (players.size() < MIN_PLAYERS) {
            return false;
        }

        // Create Room 1 puzzles
        List<Puzzle> puzzles =
                createRoom1Puzzles();

        gameStarted = true;
        currentRoom = 1;

        // Assign one puzzle to each player
        for (int i = 0; i < players.size(); i++) {

            Player player = players.get(i);

            player.setCurrentRoom(1);

            player.setAssignedPuzzle(
                    puzzles.get(i));

            player.setPuzzleSolved(false);

            player.setPuzzleFailed(false);
        }

        System.out.println(
                "Game started with "
                + players.size()
                + " player(s).");

        return true;
    }


    // =====================================================
    // GAME STATUS
    // =====================================================

    public synchronized boolean isGameStarted() {
        return gameStarted;
    }


    public synchronized int getCurrentRoom() {
        return currentRoom;
    }


    public synchronized List<Player> getPlayers() {
        return new ArrayList<>(players);
    }


    // =====================================================
    // CHECK INDIVIDUAL ANSWER
    // =====================================================

    public synchronized boolean checkPlayerAnswer(
            String playerName,
            String answer) {

        Player player =
                getPlayer(playerName);

        if (player == null) {
            return false;
        }

        Puzzle puzzle =
                player.getAssignedPuzzle();

        if (puzzle == null) {
            return false;
        }

        return puzzle.checkAnswer(answer);
    }


    // =====================================================
    // SOLVE INDIVIDUAL PUZZLE
    // =====================================================

    public synchronized void solvePuzzle(
            String playerName) {

        Player player =
                getPlayer(playerName);

        if (player == null) {
            return;
        }

        if (!player.isPuzzleSolved()) {

            player.setPuzzleSolved(true);

            player.addScore(100);
        }
    }


    // =====================================================
    // CHECK WHETHER EVERYONE HAS FINISHED
    // =====================================================

    public synchronized boolean allPlayersFinished() {

        if (players.isEmpty()) {
            return false;
        }

        for (Player player : players) {

            /*
             * A player is finished if:
             *
             * 1. They solved the puzzle
             * OR
             * 2. They failed after two attempts
             */

            if (!player.isPuzzleSolved()
                    && !player.isPuzzleFailed()) {

                return false;
            }
        }

        return true;
    }


    // =====================================================
    // START TEAM PUZZLE
    // =====================================================

    public synchronized void startTeamPuzzle() {

        teamPuzzleActive = true;
    }


    // =====================================================
    // TEAM PUZZLE STATUS
    // =====================================================

    public synchronized boolean isTeamPuzzleActive() {

        return teamPuzzleActive;
    }


    // =====================================================
    // FINISH TEAM PUZZLE
    // =====================================================

    public synchronized void finishTeamPuzzle() {

        teamPuzzleActive = false;
    }


    // =====================================================
    // GET TEAM PUZZLE FORMAT
    // =====================================================

    public synchronized String getTeamPuzzleFormat() {

        if (players.size() == 2) {

            return "CONNECT <IP> <PORT>";

        } else if (players.size() == 3) {

            return "CONNECT <IP> <PORT> <USERNAME>";

        } else {

            return "CONNECT <IP> <PORT> "
                    + "<USERNAME> <PASSWORD>";
        }
    }


    // =====================================================
    // GET TEAM PUZZLE EXAMPLE
    // =====================================================

    public synchronized String getTeamPuzzleExample() {

        if (players.size() == 2) {

            return "CONNECT "
                    + "192.168.1.10 "
                    + "80";

        } else if (players.size() == 3) {

            return "CONNECT "
                    + "192.168.1.10 "
                    + "80 "
                    + "admin";

        } else {

            return "CONNECT "
                    + "192.168.1.10 "
                    + "80 "
                    + "admin "
                    + "NETGAME123";
        }
    }


    // =====================================================
    // CHECK TEAM PUZZLE
    // =====================================================

    public synchronized boolean checkTeamPuzzle(
            String answer) {

        String correctAnswer;

        if (players.size() == 2) {

            correctAnswer =
                    "CONNECT "
                    + "192.168.1.10 "
                    + "80";

        } else if (players.size() == 3) {

            correctAnswer =
                    "CONNECT "
                    + "192.168.1.10 "
                    + "80 "
                    + "admin";

        } else {

            correctAnswer =
                    "CONNECT "
                    + "192.168.1.10 "
                    + "80 "
                    + "admin "
                    + "NETGAME123";
        }

        return answer.equalsIgnoreCase(
                correctAnswer);
    }


    // =====================================================
    // ROOM 1 PUZZLES
    // =====================================================

    private List<Puzzle> createRoom1Puzzles() {

        List<Puzzle> puzzles =
                new ArrayList<>();


        // Puzzle 1
        puzzles.add(
                new Puzzle(
                        1,

                        "Which is a valid IPv4 address?",

                        new String[]{
                                "A. 192.168.1.10",
                                "B. 300.168.1.10",
                                "C. 192.168.1",
                                "D. 192.168.1.999"
                        },

                        "A",

                        "Server IP = 192.168.1.10"
                )
        );


        // Puzzle 2
        puzzles.add(
                new Puzzle(
                        2,

                        "Which port is commonly used by HTTP?",

                        new String[]{
                                "A. 21",
                                "B. 25",
                                "C. 80",
                                "D. 110"
                        },

                        "C",

                        "Server Port = 80"
                )
        );


        // Puzzle 3
        puzzles.add(
                new Puzzle(
                        3,

                        "Which word represents a user's identity during login?",

                        new String[]{
                                "A. Username",
                                "B. Port",
                                "C. Packet",
                                "D. Router"
                        },

                        "A",

                        "Username = admin"
                )
        );


        // Puzzle 4
        puzzles.add(
                new Puzzle(
                        4,

                        "Which of the following is the correct game password?",

                        new String[]{
                                "A. NETWORK",
                                "B. NETGAME123",
                                "C. PASSWORD",
                                "D. ESCAPE"
                        },

                        "B",

                        "Password = NETGAME123"
                )
        );


        return puzzles;
    }
}