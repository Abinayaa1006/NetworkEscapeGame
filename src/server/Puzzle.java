package server;

public class Puzzle {

    private int puzzleId;
    private String question;
    private String[] options;
    private String correctAnswer;
    private String clue;

    public Puzzle(
            int puzzleId,
            String question,
            String[] options,
            String correctAnswer,
            String clue) {

        this.puzzleId = puzzleId;
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.clue = clue;
    }

    public int getPuzzleId() {
        return puzzleId;
    }

    public String getQuestion() {
        return question;
    }

    public String[] getOptions() {
        return options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String getClue() {
        return clue;
    }

    public boolean checkAnswer(String answer) {

        return correctAnswer.equalsIgnoreCase(answer);
    }
}


