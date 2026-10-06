package client.ui;

import client.NetworkClient;
import java.awt.*;
import java.util.List;
import javax.swing.*;

public class TeamChallengePanel extends JPanel {

    private NetworkClient networkClient;
    private ClueBackpackBar backpackBarRef;

    private JLabel titleLabel;
    private JLabel formatLabel;
    private JLabel exampleLabel;
    private JPanel cluesContainer;
    private ModernComponents.ModernTextField commandField;
    private ModernComponents.ModernButton executeBtn;
    private JLabel feedbackLabel;

    public TeamChallengePanel(NetworkClient networkClient) {
        this.networkClient = networkClient;
        setLayout(new BorderLayout(0, 8));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        initComponents();
    }

    private void initComponents() {
        // ── CENTER: Team workbench card ─────────────────────────────────────
        ModernComponents.CardPanel card = new ModernComponents.CardPanel(14, UITheme.BG_CARD, UITheme.BORDER_SUBTLE);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        // Header
        titleLabel = new JLabel(">> MULTIPLAYER PROTOCOL OVERRIDE");
        titleLabel.setFont(UITheme.FONT_SUBTITLE);
        titleLabel.setForeground(UITheme.ACCENT_PURPLE);

        // Syntax info box — two lines, no overlap
        ModernComponents.CardPanel infoBox = new ModernComponents.CardPanel(8, UITheme.BG_INPUT, UITheme.BORDER_SUBTLE);
        infoBox.setLayout(new GridLayout(2, 1, 0, 4));
        infoBox.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        infoBox.setPreferredSize(new Dimension(0, 58));

        formatLabel = new JLabel("SYNTAX: CONNECT <IP> <PORT>");
        formatLabel.setFont(UITheme.FONT_MONO_BOLD);
        formatLabel.setForeground(UITheme.ACCENT_CYAN);

        exampleLabel = new JLabel("EXAMPLE: CONNECT 10.0.0.1 8080");
        exampleLabel.setFont(UITheme.FONT_MONO);
        exampleLabel.setForeground(UITheme.TEXT_SECONDARY);

        infoBox.add(formatLabel);
        infoBox.add(exampleLabel);

        // Clue chips label
        JLabel clueTitle = new JLabel("COLLECTED TOKENS  (click to insert into terminal):");
        clueTitle.setFont(UITheme.FONT_SMALL_BOLD);
        clueTitle.setForeground(UITheme.TEXT_MUTED);

        cluesContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        cluesContainer.setOpaque(false);

        JPanel clueSection = new JPanel(new BorderLayout(0, 4));
        clueSection.setOpaque(false);
        clueSection.add(clueTitle, BorderLayout.NORTH);
        clueSection.add(cluesContainer, BorderLayout.CENTER);

        JPanel bodyPanel = new JPanel(new BorderLayout(0, 8));
        bodyPanel.setOpaque(false);
        bodyPanel.add(infoBox, BorderLayout.NORTH);
        bodyPanel.add(clueSection, BorderLayout.CENTER);

        // Input row + feedback on same bottom bar
        JPanel bottomRow = new JPanel(new BorderLayout(10, 6));
        bottomRow.setOpaque(false);

        commandField = new ModernComponents.ModernTextField("Synthesize team parameters...");
        commandField.setFont(UITheme.FONT_MONO_BOLD);
        commandField.addActionListener(e -> submitCommand());

        executeBtn = new ModernComponents.ModernButton("TRANSMIT >>", ModernComponents.ButtonStyle.PURPLE);
        executeBtn.setPreferredSize(new Dimension(160, 40));
        executeBtn.addActionListener(e -> submitCommand());

        feedbackLabel = new JLabel("Collaborate via chat to combine your decoded tokens.", SwingConstants.LEFT);
        feedbackLabel.setFont(UITheme.FONT_SMALL);
        feedbackLabel.setForeground(UITheme.TEXT_MUTED);

        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setOpaque(false);
        inputRow.add(commandField, BorderLayout.CENTER);
        inputRow.add(executeBtn, BorderLayout.EAST);

        bottomRow.add(inputRow, BorderLayout.CENTER);
        bottomRow.add(feedbackLabel, BorderLayout.SOUTH);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(bodyPanel, BorderLayout.CENTER);
        card.add(bottomRow, BorderLayout.SOUTH);

        // ── SOUTH: Clue vault bar ───────────────────────────────────────────
        backpackBarRef = new ClueBackpackBar();

        add(card,            BorderLayout.CENTER);
        add(backpackBarRef,  BorderLayout.SOUTH);
    }

    public void setupTeamChallenge(int room, String title, String format, String example, List<String> clues) {
        titleLabel.setText(">> " + title.toUpperCase());
        formatLabel.setText("SYNTAX: " + format);
        exampleLabel.setText("EXAMPLE: " + example);
        commandField.setText("");
        feedbackLabel.setText("Combine collected team tokens into the required format.");
        feedbackLabel.setForeground(UITheme.TEXT_MUTED);
        updateCluesList(clues);
    }

    public void updateCluesList(List<String> clues) {
        cluesContainer.removeAll();

        if (clues == null || clues.isEmpty()) {
            JLabel empty = new JLabel("No tokens yet — check System Log for your puzzle clue.");
            empty.setFont(UITheme.FONT_SMALL);
            empty.setForeground(UITheme.TEXT_MUTED);
            cluesContainer.add(empty);
        } else {
            for (String clue : clues) {
                String insertText = clue.contains("=") ? clue.substring(clue.indexOf('=') + 1).trim() : clue;
                final String toInsert = insertText;

                ModernComponents.ModernButton chip = new ModernComponents.ModernButton(clue, ModernComponents.ButtonStyle.SECONDARY);
                chip.setFont(UITheme.FONT_SMALL_BOLD);
                chip.setMargin(new Insets(3, 8, 3, 8));
                chip.addActionListener(e -> {
                    String cur = commandField.getText().trim();
                    commandField.setText(cur.isEmpty() ? toInsert : cur + " " + toInsert);
                    commandField.requestFocus();
                });
                cluesContainer.add(chip);
            }
        }

        if (backpackBarRef != null) backpackBarRef.updateClues(clues != null ? clues : List.of());
        cluesContainer.revalidate();
        cluesContainer.repaint();
    }

    private void submitCommand() {
        String cmd = commandField.getText().trim();
        if (cmd.isEmpty()) return;
        networkClient.sendTeamAnswer(cmd);
    }

    public void handleTeamResult(boolean correct, String message) {
        feedbackLabel.setText(correct ? "[OK] " + message : "[FAIL] " + message);
        feedbackLabel.setForeground(correct ? UITheme.ACCENT_GREEN : UITheme.ACCENT_RED);
        if (correct) SoundEffects.playRoomCleared();
        else SoundEffects.playError();
    }
}
