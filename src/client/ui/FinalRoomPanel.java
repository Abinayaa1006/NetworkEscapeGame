package client.ui;

import client.NetworkClient;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class FinalRoomPanel extends JPanel {

    private NetworkClient networkClient;
    private ClueBackpackBar backpackBarRef;

    private JLabel questionLabel;
    private JPanel optionsContainer;
    private ModernComponents.ModernButton escapeBtn;
    private JLabel attemptsLabel;
    private JLabel feedbackLabel;

    private String selectedFinalOption = null;
    private final List<ModernComponents.OptionTile> optionTiles = new ArrayList<>();

    public FinalRoomPanel(NetworkClient networkClient) {
        this.networkClient = networkClient;
        setLayout(new BorderLayout(0, 8));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        initComponents();
    }

    private void initComponents() {
        // ── CENTER: Final challenge card ────────────────────────────────────
        ModernComponents.CardPanel card = new ModernComponents.CardPanel(14, UITheme.BG_CARD, UITheme.BORDER_SUBTLE);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        // Header row: title + attempts badge inline
        JPanel headerRow = new JPanel(new BorderLayout(8, 0));
        headerRow.setOpaque(false);

        JLabel titleLbl = new JLabel(">> FINAL GATEWAY ESCAPE SEQUENCE");
        titleLbl.setFont(UITheme.FONT_SUBTITLE);
        titleLbl.setForeground(UITheme.ACCENT_PURPLE);

        attemptsLabel = new JLabel("[3 / 3 TEAM ATTEMPTS]");
        attemptsLabel.setFont(UITheme.FONT_SMALL_BOLD);
        attemptsLabel.setForeground(UITheme.ACCENT_GREEN);
        attemptsLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        headerRow.add(titleLbl, BorderLayout.CENTER);
        headerRow.add(attemptsLabel, BorderLayout.EAST);

        // Question box
        questionLabel = new JLabel("<html>Loading final challenge from server...</html>");
        questionLabel.setFont(UITheme.FONT_BODY_BOLD);
        questionLabel.setForeground(UITheme.TEXT_PRIMARY);
        questionLabel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        ModernComponents.CardPanel qBox = new ModernComponents.CardPanel(8, UITheme.BG_INPUT, UITheme.BORDER_SUBTLE);
        qBox.setLayout(new BorderLayout());
        qBox.add(questionLabel, BorderLayout.CENTER);

        // 2x2 options grid
        optionsContainer = new JPanel(new GridLayout(2, 2, 10, 8));
        optionsContainer.setOpaque(false);
        optionsContainer.setPreferredSize(new Dimension(0, 160));

        JPanel bodyPanel = new JPanel(new BorderLayout(0, 8));
        bodyPanel.setOpaque(false);
        bodyPanel.add(qBox, BorderLayout.NORTH);
        bodyPanel.add(optionsContainer, BorderLayout.CENTER);

        // Bottom: feedback + escape button inline
        JPanel bottomRow = new JPanel(new BorderLayout(12, 0));
        bottomRow.setOpaque(false);

        feedbackLabel = new JLabel("Synthesize all clues from Rooms 1 & 2 — only 3 total team attempts!");
        feedbackLabel.setFont(UITheme.FONT_SMALL);
        feedbackLabel.setForeground(UITheme.TEXT_MUTED);

        escapeBtn = new ModernComponents.ModernButton("INITIATE ESCAPE >>", ModernComponents.ButtonStyle.PURPLE);
        escapeBtn.setPreferredSize(new Dimension(200, 40));
        escapeBtn.setEnabled(false);
        escapeBtn.addActionListener(e -> submitFinalAnswer());

        bottomRow.add(feedbackLabel, BorderLayout.CENTER);
        bottomRow.add(escapeBtn, BorderLayout.EAST);

        card.add(headerRow, BorderLayout.NORTH);
        card.add(bodyPanel, BorderLayout.CENTER);
        card.add(bottomRow, BorderLayout.SOUTH);

        // ── SOUTH: Clue vault bar ───────────────────────────────────────────
        backpackBarRef = new ClueBackpackBar();

        add(card,            BorderLayout.CENTER);
        add(backpackBarRef,  BorderLayout.SOUTH);
    }

    public void setupFinalChallenge(String question, List<String> options, int remAttempts) {
        this.selectedFinalOption = null;

        questionLabel.setText("<html><body style='width:380px; line-height:1.45;'>"
                + (question != null ? question.replace("\n", "<br>") : "Loading...") + "</body></html>");

        optionsContainer.removeAll();
        optionTiles.clear();

        for (String opt : options) {
            String key  = opt.substring(0, 1);
            String text = opt.length() > 3 ? opt.substring(3) : opt;
            ModernComponents.OptionTile tile = new ModernComponents.OptionTile(key, text);
            tile.setOnSelectListener(e -> selectOption(key));
            optionTiles.add(tile);
            optionsContainer.add(tile);
        }

        updateAttemptsDisplay(remAttempts);
        escapeBtn.setEnabled(false);
        feedbackLabel.setText("Synthesize all clues — only 3 total team attempts!");
        feedbackLabel.setForeground(UITheme.TEXT_MUTED);
        optionsContainer.revalidate();
        optionsContainer.repaint();

        if (backpackBarRef != null) backpackBarRef.updateClues(networkClient.getDiscoveredClues());
    }

    private void selectOption(String key) {
        this.selectedFinalOption = key;
        for (ModernComponents.OptionTile t : optionTiles) t.setSelected(t.getOptionKey().equalsIgnoreCase(key));
        escapeBtn.setEnabled(true);
    }

    private void submitFinalAnswer() {
        if (selectedFinalOption == null) return;
        escapeBtn.setEnabled(false);
        networkClient.sendFinalAnswer(selectedFinalOption);
    }

    public void handleFinalResult(boolean correct, int attemptUsed, int remaining, String message) {
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
            escapeBtn.setEnabled(remaining > 0);
        }
    }

    private void updateAttemptsDisplay(int remaining) {
        attemptsLabel.setText("[" + remaining + " / 3 TEAM ATTEMPTS]");
        if (remaining == 3)      attemptsLabel.setForeground(UITheme.ACCENT_GREEN);
        else if (remaining == 2) attemptsLabel.setForeground(UITheme.ACCENT_CYAN);
        else if (remaining == 1) attemptsLabel.setForeground(UITheme.ACCENT_AMBER);
        else                     attemptsLabel.setForeground(UITheme.ACCENT_RED);
    }
}
