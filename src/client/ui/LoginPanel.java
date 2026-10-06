package client.ui;

import client.NetworkClient;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;

public class LoginPanel extends JPanel {

    private NetworkClient networkClient;
    private ModernComponents.ModernTextField hostField;
    private ModernComponents.ModernTextField portField;
    private ModernComponents.ModernTextField nameField;
    private ModernComponents.ModernButton connectBtn;
    private JLabel statusLabel;

    private Timer animTimer;
    private int animTick = 0;
    private List<Point2D.Double> backgroundNodes = new ArrayList<>();
    private Random random = new Random();

    public LoginPanel(NetworkClient networkClient) {
        this.networkClient = networkClient;
        setLayout(new GridBagLayout());
        setBackground(UITheme.BG_DARK);

        initBackgroundNodes();
        initComponents();

        // 30 FPS animation for cyber grid and node glow
        animTimer = new Timer(33, e -> {
            animTick++;
            repaint();
        });
        animTimer.start();
    }

    private void initBackgroundNodes() {
        backgroundNodes.clear();
        for (int i = 0; i < 20; i++) {
            backgroundNodes.add(new Point2D.Double(random.nextDouble(), random.nextDouble()));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        UITheme.setupAntiAliasing(g);
        Graphics2D g2d = (Graphics2D) g.create();

        int w = getWidth();
        int h = getHeight();

        // Dark Cyber Space
        g2d.setColor(new Color(9, 13, 23));
        g2d.fillRect(0, 0, w, h);

        // Perspective Circuit Grid
        g2d.setColor(new Color(20, 32, 54, 80));
        g2d.setStroke(new BasicStroke(1.0f));
        int gridSize = 32;
        for (int x = 0; x < w; x += gridSize) {
            g2d.drawLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += gridSize) {
            g2d.drawLine(0, y, w, y);
        }

        // Connecting Network Mesh in background
        for (int i = 0; i < backgroundNodes.size(); i++) {
            Point2D.Double p1 = backgroundNodes.get(i);
            int x1 = (int) (p1.x * w);
            int y1 = (int) (p1.y * h);

            // Pulse node
            float pulse = (float) (Math.sin((animTick + i * 15) * 0.08) * 0.3 + 0.7);
            g2d.setColor(new Color(0, 229, 255, (int) (pulse * 60)));
            g2d.fillOval(x1 - 3, y1 - 3, 6, 6);

            for (int j = i + 1; j < backgroundNodes.size(); j++) {
                Point2D.Double p2 = backgroundNodes.get(j);
                int x2 = (int) (p2.x * w);
                int y2 = (int) (p2.y * h);

                double dist = Math.hypot(x2 - x1, y2 - y1);
                if (dist < 160) {
                    int alpha = (int) ((1.0 - dist / 160.0) * 35 * pulse);
                    g2d.setColor(new Color(0, 229, 255, alpha));
                    g2d.drawLine(x1, y1, x2, y2);
                }
            }
        }

        // Tactical Corner Tech Markers
        drawCornerMarkers(g2d, w, h);

        g2d.dispose();
    }

    private void drawCornerMarkers(Graphics2D g, int w, int h) {
        g.setColor(new Color(0, 229, 255, 120));
        g.setStroke(new BasicStroke(2.0f));
        int len = 20;

        // Top-left
        g.drawLine(15, 15, 15 + len, 15);
        g.drawLine(15, 15, 15, 15 + len);
        // Top-right
        g.drawLine(w - 15, 15, w - 15 - len, 15);
        g.drawLine(w - 15, 15, w - 15, 15 + len);
        // Bottom-left
        g.drawLine(15, h - 15, 15 + len, h - 15);
        g.drawLine(15, h - 15, 15, h - 15 - len);
        // Bottom-right
        g.drawLine(w - 15, h - 15, w - 15 - len, h - 15);
        g.drawLine(w - 15, h - 15, w - 15, h - 15 - len);

        g.setFont(UITheme.FONT_SMALL);
        g.setColor(new Color(156, 163, 175, 140));
        g.drawString("SYS.VER // 2.4.0-TCP", 22, h - 22);
        g.drawString("NET.PORT // 5000:ACTIVE", w - 175, h - 22);
    }

    private void initComponents() {
        ModernComponents.CardPanel card = new ModernComponents.CardPanel(18, UITheme.BG_CARD, UITheme.BORDER_SUBTLE);
        card.setLayout(new BorderLayout(0, 20));
        card.setBorder(BorderFactory.createEmptyBorder(32, 36, 32, 36));
        card.setPreferredSize(new Dimension(480, 520));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel titleBadge = new JLabel(">> MULTIPLAYER PROTOCOL TERMINAL <<");
        titleBadge.setFont(UITheme.FONT_SMALL_BOLD);
        titleBadge.setForeground(UITheme.ACCENT_CYAN);
        titleBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("NETWORK ESCAPE ROOM");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("Tactical Socket Decryption & Escape Simulator");
        subLabel.setFont(UITheme.FONT_SMALL);
        subLabel.setForeground(UITheme.TEXT_SECONDARY);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titleBadge);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subLabel);

        // Form Fields
        JPanel formPanel = new JPanel(new GridLayout(3, 1, 0, 14));
        formPanel.setOpaque(false);

        // Player Name
        JPanel nameGroup = createFieldGroup("OPERATIVE CALL-SIGN / NAME", nameField = new ModernComponents.ModernTextField("e.g. Alice, Bob, Cipher, Neo"));
        
        // Host & Port Row
        JPanel netRow = new JPanel(new GridLayout(1, 2, 12, 0));
        netRow.setOpaque(false);
        JPanel hostGroup = createFieldGroup("SERVER HOST / IP", hostField = new ModernComponents.ModernTextField("localhost"));
        JPanel portGroup = createFieldGroup("PORT", portField = new ModernComponents.ModernTextField("5000"));
        netRow.add(hostGroup);
        netRow.add(portGroup);

        formPanel.add(nameGroup);
        formPanel.add(netRow);

        // Action Bar & Status
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        connectBtn = new ModernComponents.ModernButton("ENTER THE ESCAPE ROOM >>", ModernComponents.ButtonStyle.PRIMARY);
        connectBtn.setPreferredSize(new Dimension(390, 46));
        connectBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        connectBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectBtn.addActionListener(e -> performConnect());

        nameField.addActionListener(e -> performConnect());

        statusLabel = new JLabel("Socket Ready // Enter credentials to connect", SwingConstants.CENTER);
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.TEXT_MUTED);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        bottomPanel.add(connectBtn);
        bottomPanel.add(Box.createVerticalStrut(12));
        bottomPanel.add(statusLabel);

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(formPanel, BorderLayout.CENTER);
        card.add(bottomPanel, BorderLayout.SOUTH);

        add(card);
    }

    private JPanel createFieldGroup(String labelText, JComponent field) {
        JPanel group = new JPanel(new BorderLayout(0, 6));
        group.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_SMALL_BOLD);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        group.add(lbl, BorderLayout.NORTH);
        group.add(field, BorderLayout.CENTER);
        return group;
    }

    private void performConnect() {
        String name = nameField.getText().trim();
        String host = hostField.getText().trim();
        String portStr = portField.getText().trim();

        if (name.isEmpty()) {
            setStatus("Please enter your operative call-sign.", UITheme.ACCENT_RED);
            SoundEffects.playError();
            nameField.requestFocus();
            return;
        }

        int port = 5000;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            setStatus("Invalid port number.", UITheme.ACCENT_RED);
            SoundEffects.playError();
            return;
        }

        setStatus("Opening socket to " + host + ":" + port + "...", UITheme.ACCENT_CYAN);
        connectBtn.setEnabled(false);
        networkClient.connect(host, port, name);
    }

    public void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
        connectBtn.setEnabled(true);
    }

    public void reset() {
        connectBtn.setEnabled(true);
        statusLabel.setText("Socket Ready // Enter credentials to connect");
        statusLabel.setForeground(UITheme.TEXT_MUTED);
    }
}
