package client.ui;

import java.awt.*;
import java.util.Map;
import javax.swing.*;

public class VictoryDialog extends JDialog {

    public interface DebriefActionListener {
        void onPlayAgain();
        void onExit();
    }

    public VictoryDialog(Frame parent, boolean escaped, Map<String, Integer> finalScores, DebriefActionListener listener) {
        super(parent, "Mission Debrief", true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(480, 500);
        setLocationRelativeTo(parent);

        ModernComponents.CardPanel card = new ModernComponents.CardPanel(20, UITheme.BG_CARD, escaped ? UITheme.ACCENT_GREEN : UITheme.ACCENT_RED);
        card.setLayout(new BorderLayout(0, 20));
        card.setBorder(BorderFactory.createEmptyBorder(30, 36, 30, 36));

        // Top Banner
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel(escaped ? "[MISSION ACCOMPLISHED]" : "[SYSTEM LOCKDOWN]", SwingConstants.CENTER);
        icon.setFont(UITheme.FONT_SUBTITLE);
        icon.setForeground(escaped ? UITheme.ACCENT_GREEN : UITheme.ACCENT_RED);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel(escaped ? "ESCAPE SUCCESSFUL!" : "MISSION FAILED", SwingConstants.CENTER);
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(escaped ? UITheme.ACCENT_GREEN : UITheme.ACCENT_RED);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(
            escaped ? "All security protocols bypassed. Network breached!" : "All attempts exhausted. Lockdown initiated.",
            SwingConstants.CENTER
        );
        subtitle.setFont(UITheme.FONT_SMALL);
        subtitle.setForeground(UITheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        top.add(icon);
        top.add(Box.createVerticalStrut(8));
        top.add(title);
        top.add(Box.createVerticalStrut(4));
        top.add(subtitle);

        // Center Leaderboard
        ModernComponents.CardPanel scoreCard = new ModernComponents.CardPanel(12, UITheme.BG_INPUT, UITheme.BORDER_SUBTLE);
        scoreCard.setLayout(new BoxLayout(scoreCard, BoxLayout.Y_AXIS));
        scoreCard.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel scoreTitle = new JLabel("TEAM LEADERBOARD SCORES");
        scoreTitle.setFont(UITheme.FONT_SMALL_BOLD);
        scoreTitle.setForeground(UITheme.ACCENT_AMBER);
        scoreTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        scoreCard.add(scoreTitle);
        scoreCard.add(Box.createVerticalStrut(12));

        if (finalScores != null && !finalScores.isEmpty()) {
            int rank = 1;
            for (Map.Entry<String, Integer> entry : finalScores.entrySet()) {
                JPanel row = new JPanel(new BorderLayout());
                row.setOpaque(false);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

                String medal = "[#" + rank + "] ";
                JLabel nameLbl = new JLabel(medal + entry.getKey());
                nameLbl.setFont(UITheme.FONT_BODY_BOLD);
                nameLbl.setForeground(UITheme.TEXT_PRIMARY);

                JLabel ptsLbl = new JLabel(entry.getValue() + " PTS");
                ptsLbl.setFont(UITheme.FONT_MONO_BOLD);
                ptsLbl.setForeground(UITheme.ACCENT_CYAN);

                row.add(nameLbl, BorderLayout.WEST);
                row.add(ptsLbl, BorderLayout.EAST);

                scoreCard.add(row);
                scoreCard.add(Box.createVerticalStrut(6));
                rank++;
            }
        } else {
            JLabel noScore = new JLabel("No score data recorded.");
            noScore.setForeground(UITheme.TEXT_MUTED);
            scoreCard.add(noScore);
        }

        // Bottom Actions
        JPanel bottom = new JPanel(new GridLayout(1, 2, 12, 0));
        bottom.setOpaque(false);

        ModernComponents.ModernButton playAgainBtn = new ModernComponents.ModernButton("RECONNECT / LOBBY", ModernComponents.ButtonStyle.PRIMARY);
        playAgainBtn.addActionListener(e -> {
            dispose();
            if (listener != null) listener.onPlayAgain();
        });

        ModernComponents.ModernButton exitBtn = new ModernComponents.ModernButton("CLOSE", ModernComponents.ButtonStyle.SECONDARY);
        exitBtn.addActionListener(e -> {
            dispose();
            if (listener != null) listener.onExit();
        });

        bottom.add(playAgainBtn);
        bottom.add(exitBtn);

        card.add(top, BorderLayout.NORTH);
        card.add(scoreCard, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        setContentPane(card);
    }
}
