package client.ui;

import client.NetworkClient;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class PuzzlePanel extends JPanel {

    private NetworkClient networkClient;
    private JLabel puzzleHeaderLabel;
    private JLabel attemptsLabel;
    private JLabel questionLabel;
    private JPanel optionsContainer;
    private ModernComponents.ModernButton submitBtn;
    private JLabel feedbackLabel;

    private int puzzleId = 0;
    private String selectedOption = null;
    private final List<ModernComponents.OptionTile> optionTiles = new ArrayList<>();
    private boolean isFinished = false;
    private int clientAttemptCount = 0;

    public PuzzlePanel(NetworkClient networkClient) {
        this.networkClient = networkClient;
        setLayout(new BorderLayout(0, 8));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        initComponents();
    }

    private void initComponents() {
        // ── CENTER: Puzzle console card ────────────────────────────────────

        // ── CENTER: Puzzle console card ────────────────────────────────────
        ModernComponents.CardPanel consoleCard = new ModernComponents.CardPanel(14, UITheme.BG_CARD, UITheme.BORDER_SUBTLE);
        consoleCard.setLayout(new BorderLayout(0, 10));
        consoleCard.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        // Header row: challenge label + attempts badge on same line
        JPanel headerRow = new JPanel(new BorderLayout(8, 0));
        headerRow.setOpaque(false);

        puzzleHeaderLabel = new JLabel(">> PROTOCOL CHALLENGE #? (ROOM 1)");
        puzzleHeaderLabel.setFont(UITheme.FONT_SUBTITLE);
        puzzleHeaderLabel.setForeground(UITheme.ACCENT_CYAN);

        attemptsLabel = new JLabel("[2 ATTEMPTS]");
        attemptsLabel.setFont(UITheme.FONT_SMALL_BOLD);
        attemptsLabel.setForeground(UITheme.ACCENT_GREEN);
        attemptsLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        headerRow.add(puzzleHeaderLabel, BorderLayout.CENTER);
        headerRow.add(attemptsLabel, BorderLayout.EAST);

        // Question box
        questionLabel = new JLabel("<html>Waiting for puzzle assignment from server...</html>");
        questionLabel.setFont(UITheme.FONT_BODY_BOLD);
        questionLabel.setForeground(UITheme.TEXT_PRIMARY);
        questionLabel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        ModernComponents.CardPanel qBox = new ModernComponents.CardPanel(8, UITheme.BG_INPUT, UITheme.BORDER_SUBTLE);
        qBox.setLayout(new BorderLayout());
        qBox.add(questionLabel, BorderLayout.CENTER);

        // 2x2 options grid — fixed minimum heights per tile
        optionsContainer = new JPanel(new GridLayout(2, 2, 10, 8));
        optionsContainer.setOpaque(false);
        optionsContainer.setPreferredSize(new Dimension(0, 160));

        JPanel bodyPanel = new JPanel(new BorderLayout(0, 8));
        bodyPanel.setOpaque(false);
        bodyPanel.add(qBox, BorderLayout.NORTH);
        bodyPanel.add(optionsContainer, BorderLayout.CENTER);

        // Bottom: feedback text + submit button on one line
        JPanel bottomRow = new JPanel(new BorderLayout(12, 0));
        bottomRow.setOpaque(false);

        feedbackLabel = new JLabel(" ");
        feedbackLabel.setFont(UITheme.FONT_SMALL_BOLD);
        feedbackLabel.setForeground(UITheme.TEXT_MUTED);

        submitBtn = new ModernComponents.ModernButton("TRANSMIT ANSWER >>", ModernComponents.ButtonStyle.PRIMARY);
        submitBtn.setPreferredSize(new Dimension(200, 40));
        submitBtn.setEnabled(false);
        submitBtn.addActionListener(e -> submitAnswer());

        bottomRow.add(feedbackLabel, BorderLayout.CENTER);
        bottomRow.add(submitBtn, BorderLayout.EAST);

        consoleCard.add(headerRow, BorderLayout.NORTH);
        consoleCard.add(bodyPanel, BorderLayout.CENTER);
        consoleCard.add(bottomRow, BorderLayout.SOUTH);

        // ── SOUTH: Clue vault bar ──────────────────────────────────────────
        ClueBackpackBar backpackBar = new ClueBackpackBar() {
            @Override public void updateClues(List<String> clues) {
                super.updateClues(clues);
            }
        };
        // Store ref for later updates
        this.backpackBarRef = backpackBar;

        // Assemble
        add(consoleCard, BorderLayout.CENTER);
        add(backpackBar, BorderLayout.SOUTH);
    }

    // Reference to the clue bar so it can be updated from handlePuzzleResult
    private ClueBackpackBar backpackBarRef;

    public void setRoom(int room) {
        // Top visual banner removed
    }

    public void displayPuzzle(int id, String question, List<String> options) {
        this.puzzleId = id;
        this.selectedOption = null;
        this.isFinished = false;
        this.clientAttemptCount = 0;

        int roomNum = (id <= 4) ? 1 : 2;
        puzzleHeaderLabel.setText(">> PROTOCOL CHALLENGE #" + id + "  —  ROOM " + roomNum);
        attemptsLabel.setText("[2 ATTEMPTS]");
        attemptsLabel.setForeground(UITheme.ACCENT_GREEN);
        questionLabel.setText("<html><body style='width:380px; line-height:1.45;'>"
                + question.replace("\n", "<br>") + "</body></html>");

        optionsContainer.removeAll();
        optionTiles.clear();

        for (String opt : options) {
            String key  = opt.substring(0, 1);
            String text = opt.length() > 3 ? opt.substring(3) : opt;
            ModernComponents.OptionTile tile = new ModernComponents.OptionTile(key, text);
            tile.setOnSelectListener(e -> { if (!isFinished && clientAttemptCount < 2) selectOption(key); });
            optionTiles.add(tile);
            optionsContainer.add(tile);
        }

        submitBtn.setEnabled(false);
        submitBtn.setText("TRANSMIT ANSWER >>");
        submitBtn.setButtonStyle(ModernComponents.ButtonStyle.PRIMARY);
        feedbackLabel.setText(" ");
        feedbackLabel.setForeground(UITheme.TEXT_MUTED);
        optionsContainer.revalidate();
        optionsContainer.repaint();

        if (backpackBarRef != null) backpackBarRef.updateClues(networkClient.getDiscoveredClues());
    }

    private void selectOption(String key) {
        if (isFinished || clientAttemptCount >= 2) return;
        this.selectedOption = key;
        for (ModernComponents.OptionTile t : optionTiles) t.setSelected(t.getOptionKey().equalsIgnoreCase(key));
        submitBtn.setEnabled(true);
    }

    private void submitAnswer() {
        if (selectedOption == null || isFinished || clientAttemptCount >= 2) return;
        clientAttemptCount++;
        // Update attempts label immediately
        int left = 2 - clientAttemptCount;
        attemptsLabel.setText(left == 0 ? "[LAST ATTEMPT]" : "[" + left + " ATTEMPT LEFT]");
        attemptsLabel.setForeground(left == 0 ? UITheme.ACCENT_RED : UITheme.ACCENT_AMBER);
        submitBtn.setEnabled(false);
        networkClient.sendAnswer(selectedOption);
    }

    public void handlePuzzleResult(boolean correct, int remainingAttempts, String feedback) {
        if (correct) {
            isFinished = true;
            feedbackLabel.setText("[OK] SOLVED! +100 PTS — CLUE UNLOCKED");
            feedbackLabel.setForeground(UITheme.ACCENT_GREEN);
            submitBtn.setEnabled(false);
            submitBtn.setText("SOLVED [OK]");
            submitBtn.setButtonStyle(ModernComponents.ButtonStyle.SUCCESS);
            attemptsLabel.setText("[SOLVED]");
            attemptsLabel.setForeground(UITheme.ACCENT_GREEN);
            SoundEffects.playSuccess();
        } else {
            SoundEffects.playError();
            if (remainingAttempts <= 0 || clientAttemptCount >= 2) {
                isFinished = true;
                feedbackLabel.setText("[FAIL] All attempts used — clue auto-bypassed to team.");
                feedbackLabel.setForeground(UITheme.ACCENT_RED);
                submitBtn.setEnabled(false);
                submitBtn.setText("FAILED [X]");
                submitBtn.setButtonStyle(ModernComponents.ButtonStyle.DANGER);
                attemptsLabel.setText("[LOCKED OUT]");
                attemptsLabel.setForeground(UITheme.ACCENT_RED);
            } else {
                feedbackLabel.setText("[!] Incorrect — 1 attempt remaining.");
                feedbackLabel.setForeground(UITheme.ACCENT_AMBER);
                submitBtn.setEnabled(true);
            }
        }
        if (backpackBarRef != null) backpackBarRef.updateClues(networkClient.getDiscoveredClues());
    }
}
