package server;

public class Player {

    private String name;
    private int score;
    private int currentRoom;

    private Puzzle assignedPuzzle;
    private boolean puzzleSolved;

    private int attempts;

    public Player(String name) {

        this.name = name;
        this.score = 0;
        this.currentRoom = 0;
        this.puzzleSolved = false;
        this.attempts = 0;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        score = score + points;
    }

    public int getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(int room) {
        currentRoom = room;
    }

    public Puzzle getAssignedPuzzle() {
        return assignedPuzzle;
    }

    public void setAssignedPuzzle(Puzzle puzzle) {
        assignedPuzzle = puzzle;
    }

    public boolean isPuzzleSolved() {
        return puzzleSolved;
    }

    public void setPuzzleSolved(boolean solved) {
        puzzleSolved = solved;
    }

    public int getAttempts() {
        return attempts;
    }

    public void increaseAttempts() {
        attempts++;
    }
}