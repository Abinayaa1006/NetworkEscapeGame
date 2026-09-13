package server;

import java.util.ArrayList;
import java.util.List;

public class GameManager {

    // Maximum number of players
    private static final int MAX_PLAYERS = 4;

    // List of players
    private final List<Player> players =
            new ArrayList<>();

    // Current room
    // 0 = Lobby
    // 1 = Room 1
    // 2 = Room 2
    // 3 = Final Room
    private int currentRoom = 0;

    // Whether the game has started
    private boolean gameStarted = false;


    // =========================
    // ADD PLAYER
    // =========================

   public synchronized boolean addPlayer(String name) {

    // Do not allow players after game has started
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

    // =========================
    // REMOVE PLAYER
    // =========================

    public synchronized void removePlayer(String name) {

        players.removeIf(
                player -> player.getName().equals(name));

        System.out.println(
                "Player removed: " + name);
    }


    // =========================
    // GET PLAYER COUNT
    // =========================

    public synchronized int getPlayerCount() {

        return players.size();
    }


    // =========================
    // START GAME
    // =========================

    public synchronized boolean startGame() {

    // Don't start if already started
    if (gameStarted) {
        return false;
    }

    // Don't start if there are no players
   if (players.size() < 2) {
    return false;
}

    // Create Room 1 puzzles
    List<Puzzle> puzzles = createRoom1Puzzles();

    // Start the game
    gameStarted = true;

    // Move to Room 1
    currentRoom = 1;

    // Assign one puzzle to each player
    for (int i = 0; i < players.size(); i++) {

        Player player = players.get(i);

        player.setCurrentRoom(1);
        player.setAssignedPuzzle(puzzles.get(i));
        player.setPuzzleSolved(false);
    }

    System.out.println(
            "Game started with "
                    + players.size()
                    + " player(s).");

    return true;
}


    // =========================
    // CHECK GAME STATUS
    // =========================

    public synchronized boolean isGameStarted() {

        return gameStarted;
    }


    // =========================
    // GET CURRENT ROOM
    // =========================

    public synchronized int getCurrentRoom() {

        return currentRoom;
    }


    // =========================
    // GET PLAYERS
    // =========================

    public synchronized List<Player> getPlayers() {

        return new ArrayList<>(players);
    }

    public synchronized Player getPlayer(String name) {

    for (Player player : players) {

        if (player.getName().equals(name)) {
            return player;
        }
    }

    return null;
}

// =========================
// CREATE ROOM 1 PUZZLES
// =========================

private List<Puzzle> createRoom1Puzzles() {

    List<Puzzle> puzzles = new ArrayList<>();

    // Puzzle 1 - IP Address
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


    // Puzzle 2 - Port
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


    // Puzzle 3 - Username
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


    // Puzzle 4 - Password
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
            )      );

        return puzzles;
        }

        public synchronized boolean checkPlayerAnswer(
        String playerName,
        String answer) {

    Player player = getPlayer(playerName);

    if (player == null) {
        return false;
    }

    Puzzle puzzle = player.getAssignedPuzzle();

    if (puzzle == null) {
        return false;
    }

    return puzzle.checkAnswer(answer);
}
public synchronized void solvePuzzle(
        String playerName) {

    Player player = getPlayer(playerName);

    if (player == null) {
        return;
    }

    if (!player.isPuzzleSolved()) {

        player.setPuzzleSolved(true);

        player.addScore(100);
    }
}
}

