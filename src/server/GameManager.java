package server;

import java.util.ArrayList;
import java.util.List;

public class GameManager {

    private static final int MAX_PLAYERS = 4;
    private static final int MIN_PLAYERS = 2;

    // Number of attempts allowed in Final Room
    private static final int FINAL_MAX_ATTEMPTS = 3;

    private final List<Player> players =
            new ArrayList<>();


    /*
     * Room numbers:
     *
     * 0 = Lobby
     * 1 = Room 1
     * 2 = Room 2
     * 3 = Final Room
     */

    private int currentRoom = 0;

    private boolean gameStarted = false;

    private boolean teamPuzzleActive = false;

    // Attempts used by the whole team in Final Room
    private int finalAttempts = 0;


    // =====================================================
    // ADD PLAYER
    // =====================================================

    public synchronized boolean addPlayer(
            String name) {

        // Do not allow players after game starts
        if (gameStarted) {
            return false;
        }

        // Maximum 4 players
        if (players.size() >= MAX_PLAYERS) {
            return false;
        }

        // Prevent duplicate names
        for (Player player : players) {

            if (player.getName()
                    .equalsIgnoreCase(name)) {

                return false;
            }
        }

        Player player =
                new Player(name);

        players.add(player);

        System.out.println(
                "Player added: " + name);

        return true;
    }


    // =====================================================
    // REMOVE PLAYER
    // =====================================================

    public synchronized void removePlayer(
            String name) {

        players.removeIf(
                player ->
                        player.getName()
                                .equals(name));

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

    public synchronized Player getPlayer(
            String name) {

        for (Player player : players) {

            if (player.getName()
                    .equals(name)) {

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

        // Minimum 2 players
        if (players.size() < MIN_PLAYERS) {
            return false;
        }

        List<Puzzle> puzzles =
                createRoom1Puzzles();

        gameStarted = true;

        currentRoom = 1;

        teamPuzzleActive = false;

        finalAttempts = 0;


        // Assign Room 1 puzzles
        for (int i = 0;
             i < players.size();
             i++) {

            Player player =
                    players.get(i);

            player.setCurrentRoom(1);

            player.setAssignedPuzzle(
                    puzzles.get(i));

            player.setPuzzleSolved(false);

            player.setPuzzleFailed(false);

            // Fresh attempts
            player.resetAttempts();
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


    // =====================================================
    // CURRENT ROOM
    // =====================================================

    public synchronized int getCurrentRoom() {

        return currentRoom;
    }


    // =====================================================
    // GET PLAYERS
    // =====================================================

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
    // CHECK WHETHER ALL PLAYERS FINISHED
    // =====================================================

    public synchronized boolean allPlayersFinished() {

        if (players.isEmpty()) {
            return false;
        }

        for (Player player : players) {

            /*
             * A player is finished when:
             *
             * 1. Puzzle solved
             *
             * OR
             *
             * 2. Puzzle failed after 2 attempts
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
    // START ROOM 2
    // =====================================================

    public synchronized void startRoom2() {

        currentRoom = 2;

        teamPuzzleActive = false;


        for (Player player : players) {

            player.setCurrentRoom(2);

            player.setPuzzleSolved(false);

            player.setPuzzleFailed(false);

            // Give each player fresh 2 attempts
            player.resetAttempts();
        }


        System.out.println(
                "Entering Room 2.");
    }


    // =====================================================
    // CHECK WHETHER ROOM 2
    // =====================================================

    public synchronized boolean isRoom2() {

        return currentRoom == 2;
    }


    // =====================================================
    // ASSIGN ROOM 2 PUZZLES
    // =====================================================

    public synchronized void assignRoom2Puzzles() {

        List<Puzzle> puzzles =
                createRoom2Puzzles();


        for (int i = 0;
             i < players.size();
             i++) {

            Player player =
                    players.get(i);

            player.setAssignedPuzzle(
                    puzzles.get(i));

            player.setPuzzleSolved(false);

            player.setPuzzleFailed(false);

            // Fresh 2 attempts
            player.resetAttempts();
        }


        System.out.println(
                "Room 2 puzzles assigned.");
    }


    // =====================================================
    // START FINAL ROOM
    // =====================================================

    public synchronized void startFinalRoom() {

        currentRoom = 3;

        teamPuzzleActive = true;

        // Reset Final Room team attempts
        finalAttempts = 0;


        System.out.println(
                "Entering Final Room.");
    }


    // =====================================================
    // FINAL ROOM ATTEMPTS
    // =====================================================

    public synchronized int getFinalAttempts() {

        return finalAttempts;
    }


    public synchronized int getFinalAttemptsRemaining() {

        return FINAL_MAX_ATTEMPTS
                - finalAttempts;
    }


    public synchronized void increaseFinalAttempts() {

        finalAttempts++;
    }


    public synchronized boolean finalAttemptsFinished() {

        return finalAttempts >= FINAL_MAX_ATTEMPTS;
    }


    // =====================================================
    // FINAL PUZZLE
    // =====================================================

    public synchronized boolean checkFinalPuzzle(
            String answer) {

        return answer.equalsIgnoreCase("A");
    }


    // =====================================================
    // FINAL PUZZLE QUESTION
    // =====================================================

    public synchronized String getFinalPuzzleQuestion() {

        return
                "A client wants to reach the game server.\n\n"
                + "Server IP: 192.168.1.10\n"
                + "Port: 80\n"
                + "Route: A-C-E\n"
                + "DNS: 8.8.8.8\n\n"
                + "Which sequence correctly describes "
                + "the connection?";
    }


    // =====================================================
    // FINAL PUZZLE OPTIONS
    // =====================================================

    public synchronized String[] getFinalPuzzleOptions() {

        return new String[]{

                "A. DNS -> ROUTE -> CONNECT",

                "B. CONNECT -> DNS -> ROUTE",

                "C. ROUTE -> CONNECT -> DNS",

                "D. DNS -> CONNECT -> ROUTE"
        };
    }


    // =====================================================
    // TEAM PUZZLE FORMAT
    // =====================================================

    public synchronized String getTeamPuzzleFormat() {

        // -------------------------------------------------
        // ROOM 1
        // -------------------------------------------------

        if (currentRoom == 1) {

            if (players.size() == 2) {

                return "CONNECT <IP> <PORT>";

            } else if (players.size() == 3) {

                return "CONNECT <IP> <PORT> "
                        + "<USERNAME>";

            } else {

                return "CONNECT <IP> <PORT> "
                        + "<USERNAME> <PASSWORD>";
            }
        }


        // -------------------------------------------------
        // ROOM 2
        // -------------------------------------------------

        else if (currentRoom == 2) {

            if (players.size() == 2) {

                return "ROUTE <PATH> "
                        + "HOPS <NUMBER>";

            } else if (players.size() == 3) {

                return "ROUTE <PATH> "
                        + "HOPS <NUMBER> "
                        + "TTL <NUMBER>";

            } else {

                return "ROUTE <PATH> "
                        + "HOPS <NUMBER> "
                        + "TTL <NUMBER> "
                        + "DNS <IP>";
            }
        }

        return "";
    }


    // =====================================================
    // TEAM PUZZLE EXAMPLE
    // =====================================================

    public synchronized String getTeamPuzzleExample() {

        /*
         * These are only format examples.
         * They do NOT reveal the real answer.
         */

        // -------------------------------------------------
        // ROOM 1
        // -------------------------------------------------

        if (currentRoom == 1) {

            if (players.size() == 2) {

                return "CONNECT 10.0.0.1 8080";

            } else if (players.size() == 3) {

                return "CONNECT 10.0.0.1 "
                        + "8080 user";

            } else {

                return "CONNECT 10.0.0.1 "
                        + "8080 user PASSWORD123";
            }
        }


        // -------------------------------------------------
        // ROOM 2
        // -------------------------------------------------

        else if (currentRoom == 2) {

            if (players.size() == 2) {

                return "ROUTE A-B-C HOPS 2";

            } else if (players.size() == 3) {

                return "ROUTE A-B-C "
                        + "HOPS 2 TTL 5";

            } else {

                return "ROUTE A-B-C "
                        + "HOPS 2 "
                        + "TTL 5 "
                        + "DNS 1.1.1.1";
            }
        }

        return "";
    }


    // =====================================================
    // CHECK TEAM PUZZLE
    // =====================================================

    public synchronized boolean checkTeamPuzzle(
            String answer) {

        String correctAnswer;


        // -------------------------------------------------
        // ROOM 1
        // -------------------------------------------------

        if (currentRoom == 1) {

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
        }


        // -------------------------------------------------
        // ROOM 2
        // -------------------------------------------------

        else if (currentRoom == 2) {

            if (players.size() == 2) {

                correctAnswer =
                        "ROUTE A-C-E "
                        + "HOPS 2";

            } else if (players.size() == 3) {

                correctAnswer =
                        "ROUTE A-C-E "
                        + "HOPS 2 "
                        + "TTL 2";

            } else {

                correctAnswer =
                        "ROUTE A-C-E "
                        + "HOPS 2 "
                        + "TTL 2 "
                        + "DNS 8.8.8.8";
            }
        }

        else {

            return false;
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


        // -------------------------------------------------
        // PUZZLE 1
        // -------------------------------------------------

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


        // -------------------------------------------------
        // PUZZLE 2
        // -------------------------------------------------

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


        // -------------------------------------------------
        // PUZZLE 3
        // -------------------------------------------------

        puzzles.add(
                new Puzzle(

                        3,

                        "Which word represents a user's "
                        + "identity during login?",

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


        // -------------------------------------------------
        // PUZZLE 4
        // -------------------------------------------------

        puzzles.add(
                new Puzzle(

                        4,

                        "Which of the following is the "
                        + "correct game password?",

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


    // =====================================================
    // ROOM 2 PUZZLES
    // =====================================================

    private List<Puzzle> createRoom2Puzzles() {

        List<Puzzle> puzzles =
                new ArrayList<>();


        // =================================================
        // PUZZLE 5 - SHORTEST ROUTE
        // =================================================

        puzzles.add(
                new Puzzle(

                        5,

                        "Consider the network topology:\n"
                        + "A-B-D-E\n"
                        + "A-C-E\n\n"
                        + "Which is the shortest path "
                        + "from A to E?",

                        new String[]{
                                "A. A-B-D-E",
                                "B. A-C-E",
                                "C. A-B-C-E",
                                "D. A-D-E"
                        },

                        "B",

                        "ROUTE = A-C-E"
                )
        );


        // =================================================
        // PUZZLE 6 - HOP COUNT
        // =================================================

        puzzles.add(
                new Puzzle(

                        6,

                        "A packet travels from A to B to C. "
                        + "How many hops does it make?",

                        new String[]{
                                "A. 1",
                                "B. 2",
                                "C. 3",
                                "D. 4"
                        },

                        "B",

                        "HOPS = 2"
                )
        );


        // =================================================
        // PUZZLE 7 - TTL
        // =================================================

        puzzles.add(
                new Puzzle(

                        7,

                        "What does TTL help prevent "
                        + "in IP networks?",

                        new String[]{
                                "A. Packet looping forever",
                                "B. Data encryption",
                                "C. DNS failure",
                                "D. Port blocking"
                        },

                        "A",

                        "TTL = 2"
                )
        );


        // =================================================
        // PUZZLE 8 - DNS
        // =================================================

        puzzles.add(
                new Puzzle(

                        8,

                        "Which service translates "
                        + "domain names into IP addresses?",

                        new String[]{
                                "A. DHCP",
                                "B. DNS",
                                "C. FTP",
                                "D. ARP"
                        },

                        "B",

                        "DNS = 8.8.8.8"
                )
        );


        return puzzles;
    }
}