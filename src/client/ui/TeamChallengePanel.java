package client.ui;

import client.NetworkClient;
import java.awt.*;
import java.util.List;
import javax.swing.*;

public class TeamChallengePanel extends JPanel {

    private NetworkClient networkClient;
    private NetworkCanvasPanel networkCanvas;
    private ClueBackpackBar backpackBar;

    private JLabel titleLabel;
    private JLabel formatLabel;
    private JLabel exampleLabel;
    private JPanel cluesContainer;
    private ModernComponents.ModernTextField commandField;
    private ModernComponents.ModernButton executeBtn;
    private JLabel feedbackLabel;

    private int currentRoom = 1;

    public TeamChallengePanel(NetworkClient networkClient) {
        this.networkClient = networkClient;
        setLayout(new BorderLayout(0, 10));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        initComponents();
    }

    private void initComponents() {
        // Top: Animated Network Canvas
        networkCanvas = new NetworkCanvasPanel();

        // Center: Collaborative Workbench Console
        ModernComponents.CardPanel card = new ModernComponents.CardPanel(16, UITheme.BG_CARD, UITheme.BORDER_SUBTLE);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        titleLabel = new JLabel(">> MULTIPLAYER PROTOCOL OVERRIDE");
        titleLabel.setFont(UITheme.FONT_SUBTITLE);
        titleLabel.setForeground(UITheme.ACCENT_PURPLE);

        JLabel badge = new JLabel("[TEAM WORKBENCH]");
        badge.setFont(UITheme.FONT_SMALL_BOLD);
        badge.setForeground(UITheme.ACCENT_CYAN);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(badge, BorderLayout.EAST);

        // Center: Format Instructions & Clickable Clue Chips
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        ModernComponents.CardPanel infoBox = new ModernComponents.CardPanel(10, UITheme.BG_INPUT, UITheme.BORDER_SUBTLE);
        infoBox.setLayout(new BoxLayout(infoBox, BoxLayout.Y_AXIS));
        infoBox.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        formatLabel = new JLabel("SYNTAX REQUIREMENT: CONNECT <IP> <PORT>");
        formatLabel.setFont(UITheme.FONT_MONO_BOLD);
        formatLabel.setForeground(UITheme.ACCENT_CYAN);

        exampleLabel = new JLabel("TEMPLATE: CONNECT 10.0.0.1 8080");
        exampleLabel.setFont(UITheme.FONT_MONO);
        exampleLabel.setForeground(UITheme.TEXT_SECONDARY);

        infoBox.add(formatLabel);
        infoBox.add(Box.createVerticalStrut(4));
        infoBox.add(exampleLabel);

        JPanel clueHeader = new JPanel(new BorderLayout());
        clueHeader.setOpaque(false);
        JLabel clueTitle = new JLabel("OPERATIONAL ARTIFACTS (Click chip to insert into terminal):");
        clueTitle.setFont(UITheme.FONT_SMALL_BOLD);
        clueTitle.setForeground(UITheme.TEXT_SECONDARY);
        clueHeader.add(clueTitle, BorderLayout.WEST);

        cluesContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        cluesContainer.setOpaque(false);

        centerPanel.add(infoBox);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(clueHeader);
        centerPanel.add(Box.createVerticalStrut(4));
        centerPanel.add(cluesContainer);

        // Bottom - Input & Execute
        JPanel bottomPanel = new JPanel(new BorderLayout(12, 8));
        bottomPanel.setOpaque(false);

        commandField = new ModernComponents.ModernTextField("Synthesize team parameters...");
        commandField.setFont(UITheme.FONT_MONO_BOLD);
        commandField.addActionListener(e -> submitCommand());

        executeBtn = new ModernComponents.ModernButton("TRANSMIT OVERRIDE >>", ModernComponents.ButtonStyle.PURPLE);
        executeBtn.setPreferredSize(new Dimension(240, 42));
        executeBtn.addActionListener(e -> submitCommand());

        feedbackLabel = new JLabel("Collaborate with teammates via live chat to combine your decoded tokens.", SwingConstants.LEFT);
        feedbackLabel.setFont(UITheme.FONT_SMALL);
        feedbackLabel.setForeground(UITheme.TEXT_MUTED);

        JPanel inputRow = new JPanel(new BorderLayout(10, 0));
        inputRow.setOpaque(false);
        inputRow.add(commandField, BorderLayout.CENTER);
        inputRow.add(executeBtn, BorderLayout.EAST);

        bottomPanel.add(inputRow, BorderLayout.CENTER);
        bottomPanel.add(feedbackLabel, BorderLayout.SOUTH);

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

    public void setupTeamChallenge(int room, String title, String format, String example, List<String> clues) {
        this.currentRoom = room;
        if (networkCanvas != null) {
            networkCanvas.setRoom(room);
        }

        titleLabel.setText(">> " + title.toUpperCase());
        formatLabel.setText("SYNTAX REQUIREMENT: " + format);
        exampleLabel.setText("TEMPLATE: " + example);
        commandField.setText("");
        feedbackLabel.setText("Combine collected team tokens into the required format.");
        feedbackLabel.setForeground(UITheme.TEXT_MUTED);

        updateCluesList(clues);
    }

    public void updateCluesList(List<String> clues) {
        cluesContainer.removeAll();

        if (clues == null || clues.isEmpty()) {
            JLabel emptyLbl = new JLabel("No tokens gathered yet. Ask your team operatives!");
            emptyLbl.setFont(UITheme.FONT_SMALL);
            emptyLbl.setForeground(UITheme.TEXT_MUTED);
            cluesContainer.add(emptyLbl);
        } else {
            for (String clue : clues) {
                String insertText = clue;
                if (clue.contains("=")) {
                    insertText = clue.substring(clue.indexOf('=') + 1).trim();
                }

                final String toInsert = insertText;
                ModernComponents.ModernButton chip = new ModernComponents.ModernButton("[KEY] " + clue, ModernComponents.ButtonStyle.SECONDARY);
                chip.setFont(UITheme.FONT_SMALL_BOLD);
                chip.setMargin(new Insets(4, 10, 4, 10));
                chip.addActionListener(e -> {
                    String current = commandField.getText();
                    commandField.setText(current.isEmpty() ? toInsert : current + " " + toInsert);
                    commandField.requestFocus();
                });
                cluesContainer.add(chip);
            }
        }

        if (backpackBar != null) {
            backpackBar.updateClues(clues);
        }

        cluesContainer.revalidate();
        cluesContainer.repaint();
    }

    private void submitCommand() {
        String cmd = commandField.getText().trim();
        if (cmd.isEmpty()) return;
        networkClient.sendTeamAnswer(cmd);
    }

    public void handleTeamResult(boolean correct, String message) {
        if (correct) {
            feedbackLabel.setText("[OK] " + message);
            feedbackLabel.setForeground(UITheme.ACCENT_GREEN);
            SoundEffects.playRoomCleared();
        } else {
            feedbackLabel.setText("[FAIL] " + message);
            feedbackLabel.setForeground(UITheme.ACCENT_RED);
            SoundEffects.playError();
        }
    }
}
