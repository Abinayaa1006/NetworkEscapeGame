package client.ui;

import java.awt.*;
import javax.swing.*;

public class GameHUDPanel extends JPanel {

    private ModernComponents.StatusBadge roomBadge;
    private ModernComponents.StatusBadge attemptsBadge;
    private ModernComponents.StatusBadge scoreBadge;
    private ModernComponents.StatusBadge playerBadge;
    private JLabel connectionDot;

    public GameHUDPanel() {
        setLayout(new BorderLayout(16, 0));
        setBackground(UITheme.BG_DARKER);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        initComponents();
    }

    private void initComponents() {
        // Left - Brand & Room
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel logo = new JLabel("NETWORK ESCAPE");
        logo.setFont(UITheme.FONT_SUBTITLE);
        logo.setForeground(UITheme.TEXT_PRIMARY);

        roomBadge = new ModernComponents.StatusBadge("ROOM 1: ACCESS", new Color(0, 229, 255, 30), UITheme.ACCENT_CYAN);

        left.add(logo);
        left.add(roomBadge);

        // Center - Attempts & Clues
        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        center.setOpaque(false);

        attemptsBadge = new ModernComponents.StatusBadge("ATTEMPTS: 2/2", new Color(16, 185, 129, 30), UITheme.ACCENT_GREEN);
        center.add(attemptsBadge);

        // Right - Player, Score, Status
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        playerBadge = new ModernComponents.StatusBadge("AGENT: --", UITheme.BG_CARD, UITheme.TEXT_PRIMARY);
        scoreBadge = new ModernComponents.StatusBadge("SCORE: 0 PTS", new Color(245, 158, 11, 30), UITheme.ACCENT_AMBER);

        connectionDot = new JLabel("ONLINE ●");
        connectionDot.setFont(UITheme.FONT_SMALL_BOLD);
        connectionDot.setForeground(UITheme.ACCENT_GREEN);

        right.add(playerBadge);
        right.add(scoreBadge);
        right.add(connectionDot);

        add(left, BorderLayout.WEST);
        add(center, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
    }

    public void setPlayerName(String name) {
        playerBadge.setBadge("AGENT: " + name, UITheme.BG_CARD, UITheme.TEXT_PRIMARY);
    }

    public void setScore(int score) {
        scoreBadge.setBadge("SCORE: " + score + " PTS", new Color(245, 158, 11, 30), UITheme.ACCENT_AMBER);
    }

    public void setRoom(int room, String roomName) {
        String label = "ROOM " + room;
        if (room == 1) label = "ROOM 1: INITIAL ACCESS";
        else if (room == 2) label = "ROOM 2: ROUTING & TOPOLOGY";
        else if (room == 3) label = "FINAL ROOM: ESCAPE SEQUENCE";

        Color c = (room == 3) ? UITheme.ACCENT_PURPLE : (room == 2 ? UITheme.ACCENT_BLUE : UITheme.ACCENT_CYAN);
        roomBadge.setBadge(label, new Color(c.getRed(), c.getGreen(), c.getBlue(), 35), c);
    }

    public void setAttempts(int remaining, int max) {
        Color c = (remaining == 2) ? UITheme.ACCENT_GREEN : (remaining == 1 ? UITheme.ACCENT_AMBER : UITheme.ACCENT_RED);
        attemptsBadge.setBadge("ATTEMPTS: " + remaining + "/" + max, new Color(c.getRed(), c.getGreen(), c.getBlue(), 30), c);
    }
}
