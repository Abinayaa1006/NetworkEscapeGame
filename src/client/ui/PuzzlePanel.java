package client.ui;

import client.NetworkClient;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class PuzzlePanel extends JPanel {

    private NetworkClient networkClient;
    private NetworkCanvasPanel networkCanvas;
    private ClueBackpackBar backpackBar;

    private JLabel puzzleHeaderLabel;
    private JLabel questionLabel;
    private JPanel optionsContainer;
    private ModernComponents.ModernButton submitBtn;
    private JLabel feedbackLabel;
    private JPanel feedbackCard;

    private int puzzleId = 0;
    private String selectedOption = null;
    private final List<ModernComponents.OptionTile> optionTiles = new ArrayList<>();
    private boolean isFinished = false;

    public PuzzlePanel(NetworkClient networkClient) {
        this.networkClient = networkClient;
        setLayout(new BorderLayout(0, 10));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        initComponents();
    }

    private void initComponents() {
        // Top: Animated Network Chamber Visualizer
        networkCanvas = new NetworkCanvasPanel();

        // Center: High-Tech Puzzle Console Card
        ModernComponents.CardPanel consoleCard = new ModernComponents.CardPanel(16, UITheme.BG_CARD, UITheme.BORDER_SUBTLE);
        consoleCard.setLayout(new BorderLayout(0, 12));
        consoleCard.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Console Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        puzzleHeaderLabel = new JLabel(">> PROTOCOL DECRYPTION TERMINAL");
        puzzleHeaderLabel.setFont(UITheme.FONT_SUBTITLE);
        puzzleHeaderLabel.setForeground(UITheme.ACCENT_CYAN);

        JLabel maxAttemptsLbl = new JLabel("[2 ATTEMPTS REMAINING]");
        maxAttemptsLbl.setFont(UITheme.FONT_SMALL_BOLD);
        maxAttemptsLbl.setForeground(UITheme.ACCENT_GREEN);

        headerPanel.add(puzzleHeaderLabel, BorderLayout.WEST);
        headerPanel.add(maxAttemptsLbl, BorderLayout.EAST);

        // Question Display Console
        ModernComponents.CardPanel qBox = new ModernComponents.CardPanel(10, UITheme.BG_INPUT, UITheme.BORDER_SUBTLE);
        qBox.setLayout(new BorderLayout());
        qBox.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        questionLabel = new JLabel("<html>Waiting for puzzle assignment from central server...</html>");
        questionLabel.setFont(UITheme.FONT_BODY_BOLD);
        questionLabel.setForeground(UITheme.TEXT_PRIMARY);
        qBox.add(questionLabel, BorderLayout.CENTER);

        // 2x2 or 4x1 Options Grid
        optionsContainer = new JPanel(new GridLayout(2, 2, 12, 10));
        optionsContainer.setOpaque(false);

        JPanel puzzleBody = new JPanel(new BorderLayout(0, 10));
        puzzleBody.setOpaque(false);
        puzzleBody.add(qBox, BorderLayout.NORTH);
        puzzleBody.add(optionsContainer, BorderLayout.CENTER);

        // Feedback Banner
        feedbackCard = new JPanel(new BorderLayout());
        feedbackCard.setOpaque(false);
        feedbackLabel = new JLabel("", SwingConstants.LEFT);
        feedbackLabel.setFont(UITheme.FONT_BODY_BOLD);
        feedbackCard.add(feedbackLabel, BorderLayout.CENTER);
        feedbackCard.setVisible(false);

        // Bottom Action Bar
        JPanel bottomPanel = new JPanel(new BorderLayout(14, 0));
        bottomPanel.setOpaque(false);

        submitBtn = new ModernComponents.ModernButton("TRANSMIT ANSWER >>", ModernComponents.ButtonStyle.PRIMARY);
        submitBtn.setPreferredSize(new Dimension(210, 42));
        submitBtn.setEnabled(false);
        submitBtn.addActionListener(e -> submitAnswer());

        bottomPanel.add(feedbackCard, BorderLayout.CENTER);
        bottomPanel.add(submitBtn, BorderLayout.EAST);

        consoleCard.add(headerPanel, BorderLayout.NORTH);
        consoleCard.add(puzzleBody, BorderLayout.CENTER);
        consoleCard.add(bottomPanel, BorderLayout.SOUTH);

        // Bottom: Visual Clue Vault Bar
        backpackBar = new ClueBackpackBar();

        JPanel mainLayout = new JPanel(new BorderLayout(0, 10));
        mainLayout.setOpaque(false);
        mainLayout.add(networkCanvas, BorderLayout.NORTH);
        mainLayout.add(consoleCard, BorderLayout.CENTER);
        mainLayout.add(backpackBar, BorderLayout.SOUTH);

        add(mainLayout, BorderLayout.CENTER);
    }

    public void setRoom(int room) {
        if (networkCanvas != null) {
            networkCanvas.setRoom(room);
        }
    }

    public void displayPuzzle(int id, String question, List<String> options) {
        this.puzzleId = id;
        this.selectedOption = null;
        this.isFinished = false;

        puzzleHeaderLabel.setText(">> PROTOCOL CHALLENGE #" + id + " (ROOM " + (id <= 4 ? "1" : "2") + ")");
        questionLabel.setText("<html><body style='width: 460px; line-height: 1.4;'>" + question.replace("\n", "<br>") + "</body></html>");

        optionsContainer.removeAll();
        optionTiles.clear();

        for (String opt : options) {
            String key = opt.substring(0, 1);
            String text = opt.length() > 3 ? opt.substring(3) : opt;

            ModernComponents.OptionTile tile = new ModernComponents.OptionTile(key, text);
            tile.setOnSelectListener(e -> {
                if (isFinished) return;
                selectOption(key);
            });
            optionTiles.add(tile);
            optionsContainer.add(tile);
        }

        submitBtn.setEnabled(false);
        submitBtn.setText("TRANSMIT ANSWER >>");
        submitBtn.setButtonStyle(ModernComponents.ButtonStyle.PRIMARY);
        feedbackCard.setVisible(false);
        optionsContainer.revalidate();
        optionsContainer.repaint();

        if (backpackBar != null) {
            backpackBar.updateClues(networkClient.getDiscoveredClues());
        }
    }

    private void selectOption(String key) {
        this.selectedOption = key;
        for (ModernComponents.OptionTile tile : optionTiles) {
            tile.setSelected(tile.getOptionKey().equalsIgnoreCase(key));
        }
        submitBtn.setEnabled(true);
    }

    private void submitAnswer() {
        if (selectedOption == null || isFinished) return;
        submitBtn.setEnabled(false);
        networkClient.sendAnswer(selectedOption);
    }

    public void handlePuzzleResult(boolean correct, int remainingAttempts, String feedback) {
        feedbackCard.setVisible(true);
        if (correct) {
            isFinished = true;
            feedbackLabel.setText("[OK] SOLVED! +100 PTS -- CLUE UNLOCKED");
            feedbackLabel.setForeground(UITheme.ACCENT_GREEN);
            submitBtn.setEnabled(false);
            submitBtn.setText("SOLVED [OK]");
            submitBtn.setButtonStyle(ModernComponents.ButtonStyle.SUCCESS);
            SoundEffects.playSuccess();
        } else {
            SoundEffects.playError();
            triggerShakeEffect();
            if (remainingAttempts <= 0) {
                isFinished = true;
                feedbackLabel.setText("[FAIL] LOCKOUT (CLUE BYPASSED TO TEAM)");
                feedbackLabel.setForeground(UITheme.ACCENT_RED);
                submitBtn.setEnabled(false);
                submitBtn.setText("FAILED [X]");
                submitBtn.setButtonStyle(ModernComponents.ButtonStyle.DANGER);
            } else {
                feedbackLabel.setText("[!] INCORRECT -- 1 ATTEMPT LEFT");
                feedbackLabel.setForeground(UITheme.ACCENT_AMBER);
                submitBtn.setEnabled(true);
            }
        }

        if (backpackBar != null) {
            backpackBar.updateClues(networkClient.getDiscoveredClues());
        }
    }

    private void triggerShakeEffect() {
        Point original = getLocation();
        Timer shakeTimer = new Timer(25, null);
        final int[] count = {0};
        shakeTimer.addActionListener(e -> {
            int offset = (count[0] % 2 == 0) ? 6 : -6;
            setLocation(original.x + offset, original.y);
            count[0]++;
            if (count[0] >= 6) {
                shakeTimer.stop();
                setLocation(original);
            }
        });
        shakeTimer.start();
    }
}
