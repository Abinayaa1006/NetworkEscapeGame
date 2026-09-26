package client.ui;

import client.NetworkClient;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class FinalRoomPanel extends JPanel {

    private NetworkClient networkClient;
    private NetworkCanvasPanel networkCanvas;
    private ClueBackpackBar backpackBar;

    private JLabel questionLabel;
    private JPanel optionsContainer;
    private ModernComponents.ModernButton escapeBtn;
    private JLabel attemptsLabel;
    private JLabel feedbackLabel;

    private String selectedFinalOption = null;
    private final List<ModernComponents.OptionTile> optionTiles = new ArrayList<>();
    private int remainingAttempts = 3;

    public FinalRoomPanel(NetworkClient networkClient) {
        this.networkClient = networkClient;
        setLayout(new BorderLayout(0, 10));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        initComponents();
    }

    private void initComponents() {
        // Top: Animated Network Canvas (Room 3 Mainframe Core)
        networkCanvas = new NetworkCanvasPanel();
        networkCanvas.setRoom(3);

        // Center Card
        ModernComponents.CardPanel card = new ModernComponents.CardPanel(16, UITheme.BG_CARD, UITheme.BORDER_SUBTLE);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLbl = new JLabel(">> FINAL GATEWAY ESCAPE SEQUENCE");
        titleLbl.setFont(UITheme.FONT_SUBTITLE);
        titleLbl.setForeground(UITheme.ACCENT_PURPLE);

        attemptsLabel = new JLabel("TEAM SHIELDS: [3 / 3 REMAINING]");
        attemptsLabel.setFont(UITheme.FONT_SMALL_BOLD);
        attemptsLabel.setForeground(UITheme.ACCENT_GREEN);

        headerPanel.add(titleLbl, BorderLayout.WEST);
        headerPanel.add(attemptsLabel, BorderLayout.EAST);

        // Question Box
        questionLabel = new JLabel("<html>A client wants to reach the central server. Which sequence correctly describes the end-to-end TCP/IP connection?</html>");
        questionLabel.setFont(UITheme.FONT_BODY_BOLD);
        questionLabel.setForeground(UITheme.TEXT_PRIMARY);

        // 2x2 Options Grid for nice balanced console view
        optionsContainer = new JPanel(new GridLayout(2, 2, 12, 10));
        optionsContainer.setOpaque(false);

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        centerPanel.add(questionLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(optionsContainer);

        // Bottom Bar
        JPanel bottomPanel = new JPanel(new BorderLayout(14, 0));
        bottomPanel.setOpaque(false);

        feedbackLabel = new JLabel("Synthesize all Room 1 & 2 telemetry. Only 3 total team attempts!", SwingConstants.LEFT);
        feedbackLabel.setFont(UITheme.FONT_SMALL);
        feedbackLabel.setForeground(UITheme.TEXT_MUTED);

        escapeBtn = new ModernComponents.ModernButton("INITIATE ESCAPE >>", ModernComponents.ButtonStyle.PURPLE);
        escapeBtn.setPreferredSize(new Dimension(220, 42));
        escapeBtn.setEnabled(false);
        escapeBtn.addActionListener(e -> submitFinalAnswer());

        bottomPanel.add(feedbackLabel, BorderLayout.CENTER);
        bottomPanel.add(escapeBtn, BorderLayout.EAST);

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);
        card.add(bottomPanel, BorderLayout.SOUTH);

        // Bottom Backpack Bar
        backpackBar = new ClueBackpackBar();

        JPanel mainLayout = new JPanel(new BorderLayout(0, 10));
        mainLayout.setOpaque(false);
        mainLayout.add(networkCanvas, BorderLayout.NORTH);
        mainLayout.add(card, BorderLayout.CENTER);
        mainLayout.add(backpackBar, BorderLayout.SOUTH);

        add(mainLayout, BorderLayout.CENTER);
    }

    public void setupFinalChallenge(String question, List<String> options, int remAttempts) {
        this.remainingAttempts = remAttempts;
        this.selectedFinalOption = null;

        if (networkCanvas != null) {
            networkCanvas.setRoom(3);
        }

        if (question != null && !question.isEmpty()) {
            questionLabel.setText("<html><body style='width: 460px; line-height: 1.4;'>" + question.replace("\n", "<br>") + "</body></html>");
        }

        optionsContainer.removeAll();
        optionTiles.clear();

        for (String opt : options) {
            String key = opt.substring(0, 1);
            String text = opt.length() > 3 ? opt.substring(3) : opt;

            ModernComponents.OptionTile tile = new ModernComponents.OptionTile(key, text);
            tile.setOnSelectListener(e -> selectOption(key));
            optionTiles.add(tile);
            optionsContainer.add(tile);
        }

        updateAttemptsDisplay(remAttempts);
        escapeBtn.setEnabled(false);
        optionsContainer.revalidate();
        optionsContainer.repaint();

        if (backpackBar != null) {
            backpackBar.updateClues(networkClient.getDiscoveredClues());
        }
    }

    private void selectOption(String key) {
        this.selectedFinalOption = key;
        for (ModernComponents.OptionTile tile : optionTiles) {
            tile.setSelected(tile.getOptionKey().equalsIgnoreCase(key));
        }
        escapeBtn.setEnabled(true);
    }

    private void submitFinalAnswer() {
        if (selectedFinalOption == null) return;
        escapeBtn.setEnabled(false);
        networkClient.sendFinalAnswer(selectedFinalOption);
    }

    public void handleFinalResult(boolean correct, int attemptUsed, int remaining, String message) {
        this.remainingAttempts = remaining;
        updateAttemptsDisplay(remaining);

        if (correct) {
            feedbackLabel.setText("[OK] " + message);
            feedbackLabel.setForeground(UITheme.ACCENT_GREEN);
            escapeBtn.setEnabled(false);
            SoundEffects.playVictory();
        } else {
            feedbackLabel.setText("[FAIL] " + message);
            feedbackLabel.setForeground(UITheme.ACCENT_RED);
            SoundEffects.playError();
            if (remaining > 0) {
                escapeBtn.setEnabled(true);
            }
        }
    }

    private void updateAttemptsDisplay(int remaining) {
        attemptsLabel.setText("TEAM ATTEMPTS: [" + remaining + " / 3 REMAINING]");
        if (remaining == 3) attemptsLabel.setForeground(UITheme.ACCENT_GREEN);
        else if (remaining == 2) attemptsLabel.setForeground(UITheme.ACCENT_AMBER);
        else attemptsLabel.setForeground(UITheme.ACCENT_RED);
    }
}
