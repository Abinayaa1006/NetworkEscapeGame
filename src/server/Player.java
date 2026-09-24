package server;

public class Player {

    private String name;
    private int score;
    private int currentRoom;

    private Puzzle assignedPuzzle;

    private boolean puzzleSolved;
    private boolean puzzleFailed;

    private int attempts;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public Player(String name) {

        this.name = name;

        this.score = 0;

        this.currentRoom = 0;

        this.puzzleSolved = false;

        this.puzzleFailed = false;

        this.attempts = 0;
    }


    // =====================================================
    // GET NAME
    // =====================================================

    public String getName() {

        return name;
    }


    // =====================================================
    // SCORE
    // =====================================================

    public int getScore() {

        return score;
    }


    public void addScore(int points) {

        score = score + points;
    }


    // =====================================================
    // CURRENT ROOM
    // =====================================================

    public int getCurrentRoom() {

        return currentRoom;
    }


    public void setCurrentRoom(int room) {

        currentRoom = room;
    }


    // =====================================================
    // ASSIGNED PUZZLE
    // =====================================================

    public Puzzle getAssignedPuzzle() {

        return assignedPuzzle;
    }


    public void setAssignedPuzzle(Puzzle puzzle) {

        assignedPuzzle = puzzle;
    }


    // =====================================================
    // PUZZLE SOLVED
    // =====================================================

    public boolean isPuzzleSolved() {

        return puzzleSolved;
    }


    public void setPuzzleSolved(
            boolean solved) {

        puzzleSolved = solved;
    }


    // =====================================================
    // PUZZLE FAILED
    // =====================================================

    public boolean isPuzzleFailed() {

        return puzzleFailed;
    }


    public void setPuzzleFailed(
            boolean failed) {

        puzzleFailed = failed;
    }


    // =====================================================
    // ATTEMPTS
    // =====================================================

    public int getAttempts() {

        return attempts;
    }


    public void increaseAttempts() {

        attempts++;
    }


    // Reset attempts when entering a new room
    public void resetAttempts() {

        attempts = 0;
    }
}