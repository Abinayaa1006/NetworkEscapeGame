package client.ui;

import client.NetworkClient;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class LobbyPanel extends JPanel {

    private NetworkClient networkClient;
    private JLabel countBadgeLabel;
    private JPanel playerGrid;
    private ModernComponents.ModernButton startBtn;
    private JLabel hintLabel;

    private int currentPlayers = 1;
    private int maxPlayers = 4;

    public LobbyPanel(NetworkClient networkClient) {
        this.networkClient = networkClient;
        setLayout(new BorderLayout(0, 20));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        initComponents();
    }

    private void initComponents() {
        // Top Header
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        ModernComponents.SectionHeader header = new ModernComponents.SectionHeader(
            "MULTIPLAYER MISSION BRIEFING LOBBY",
            "Waiting for 2 to 4 network operatives to connect before starting.",
            UITheme.ACCENT_CYAN
        );

        countBadgeLabel = new JLabel("PLAYERS: 1 / 4", SwingConstants.RIGHT);
        countBadgeLabel.setFont(UITheme.FONT_SUBTITLE);
        countBadgeLabel.setForeground(UITheme.ACCENT_AMBER);

        topHeader.add(header, BorderLayout.WEST);
        topHeader.add(countBadgeLabel, BorderLayout.EAST);

        // Center Content Card
        ModernComponents.CardPanel rosterCard = new ModernComponents.CardPanel(16, UITheme.BG_CARD, UITheme.BORDER_SUBTLE);
        rosterCard.setLayout(new BorderLayout(0, 20));
        rosterCard.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel rosterTitle = new JLabel("CONNECTED AGENTS ROSTER");
        rosterTitle.setFont(UITheme.FONT_SMALL_BOLD);
        rosterTitle.setForeground(UITheme.TEXT_SECONDARY);

        playerGrid = new JPanel(new GridLayout(2, 2, 16, 16));
        playerGrid.setOpaque(false);

        updatePlayerCards();

        rosterCard.add(rosterTitle, BorderLayout.NORTH);
        rosterCard.add(playerGrid, BorderLayout.CENTER);

        // Info Banner
        ModernComponents.CardPanel tipCard = new ModernComponents.CardPanel(12, UITheme.BG_INPUT, UITheme.BORDER_SUBTLE);
        tipCard.setLayout(new BorderLayout(12, 0));
        tipCard.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        
        JLabel iconLbl = new JLabel(">>");
        iconLbl.setFont(UITheme.FONT_TITLE);
        iconLbl.setForeground(UITheme.ACCENT_CYAN);

        JLabel infoText = new JLabel("<html><b>HOW IT WORKS:</b> Each agent will receive an individual networking puzzle. Solving puzzles unlocks crucial clues. When all agents finish, you must combine your clues to solve the Room Team Challenge and escape!</html>");
        infoText.setFont(UITheme.FONT_SMALL);
        infoText.setForeground(UITheme.TEXT_SECONDARY);

        tipCard.add(iconLbl, BorderLayout.WEST);
        tipCard.add(infoText, BorderLayout.CENTER);

        JPanel centerWrapper = new JPanel(new BorderLayout(0, 16));
        centerWrapper.setOpaque(false);
        centerWrapper.add(rosterCard, BorderLayout.CENTER);
        centerWrapper.add(tipCard, BorderLayout.SOUTH);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);

        hintLabel = new JLabel("Minimum 2 players required to begin mission.", SwingConstants.LEFT);
        hintLabel.setFont(UITheme.FONT_SMALL);
        hintLabel.setForeground(UITheme.TEXT_MUTED);

        startBtn = new ModernComponents.ModernButton("LAUNCH MISSION (START)", ModernComponents.ButtonStyle.PRIMARY);
        startBtn.setPreferredSize(new Dimension(240, 46));
        startBtn.addActionListener(e -> {
            SoundEffects.playClick();
            networkClient.startGame();
        });

        bottomBar.add(hintLabel, BorderLayout.WEST);
        bottomBar.add(startBtn, BorderLayout.EAST);

        add(topHeader, BorderLayout.NORTH);
        add(centerWrapper, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    public void updateLobbyState(int count, int max) {
        this.currentPlayers = count;
        this.maxPlayers = max;

        countBadgeLabel.setText("PLAYERS: " + count + " / " + max);

        if (count >= 2) {
            countBadgeLabel.setForeground(UITheme.ACCENT_GREEN);
            hintLabel.setText("Ready! Any player can press Launch Mission.");
            hintLabel.setForeground(UITheme.ACCENT_GREEN);
            startBtn.setButtonStyle(ModernComponents.ButtonStyle.SUCCESS);
        } else {
            countBadgeLabel.setForeground(UITheme.ACCENT_AMBER);
            hintLabel.setText("Waiting for at least 1 more player to join...");
            hintLabel.setForeground(UITheme.ACCENT_AMBER);
            startBtn.setButtonStyle(ModernComponents.ButtonStyle.PRIMARY);
        }

        updatePlayerCards();
    }

    private void updatePlayerCards() {
        playerGrid.removeAll();

        for (int i = 1; i <= 4; i++) {
            boolean isSlotActive = (i <= currentPlayers);
            boolean isMe = (i == 1); // Primary slot representation

            ModernComponents.CardPanel slot = new ModernComponents.CardPanel(
                12,
                isSlotActive ? UITheme.BG_CARD_HOVER : UITheme.BG_INPUT,
                isSlotActive ? UITheme.ACCENT_CYAN_DIM : UITheme.BORDER_SUBTLE
            );
            slot.setLayout(new BorderLayout(12, 0));
            slot.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

            // Status Indicator Dot
            JPanel dot = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    UITheme.setupAntiAliasing(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setColor(isSlotActive ? UITheme.ACCENT_GREEN : UITheme.TEXT_MUTED);
                    g2d.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
                    g2d.dispose();
                }
            };
            dot.setPreferredSize(new Dimension(14, 14));
            dot.setOpaque(false);

            JPanel info = new JPanel();
            info.setOpaque(false);
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

            String slotName = isSlotActive 
                ? (isMe ? networkClient.getPlayerName() + " (You)" : "Operative #" + i)
                : "Empty Agent Slot";

            JLabel nameLbl = new JLabel(slotName);
            nameLbl.setFont(UITheme.FONT_BODY_BOLD);
            nameLbl.setForeground(isSlotActive ? UITheme.TEXT_PRIMARY : UITheme.TEXT_MUTED);

            JLabel statLbl = new JLabel(isSlotActive ? "STATUS: READY IN LOBBY" : "WAITING FOR CONNECTION");
            statLbl.setFont(UITheme.FONT_SMALL);
            statLbl.setForeground(isSlotActive ? UITheme.ACCENT_CYAN : UITheme.TEXT_MUTED);

            info.add(nameLbl);
            info.add(Box.createVerticalStrut(2));
            info.add(statLbl);

            slot.add(dot, BorderLayout.WEST);
            slot.add(info, BorderLayout.CENTER);

            playerGrid.add(slot);
        }

        playerGrid.revalidate();
        playerGrid.repaint();
    }
}
