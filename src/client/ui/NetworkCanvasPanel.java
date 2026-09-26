package client.ui;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;

public class NetworkCanvasPanel extends JPanel {

    private int currentRoom = 1;
    private int animationTick = 0;
    private Timer animTimer;
    private List<PacketParticle> particles = new ArrayList<>();
    private Random random = new Random();

    // Node coordinates for Room 2 Topology
    private static class NetNode {
        String label;
        int x, y;
        Color color;
        String desc;

        NetNode(String label, int x, int y, Color color, String desc) {
            this.label = label;
            this.x = x;
            this.y = y;
            this.color = color;
            this.desc = desc;
        }
    }

    private static class PacketParticle {
        double x, y;
        double targetX, targetY;
        double speed;
        Color color;

        PacketParticle(double x, double y, double tx, double ty, double speed, Color color) {
            this.x = x;
            this.y = y;
            this.targetX = tx;
            this.targetY = ty;
            this.speed = speed;
            this.color = color;
        }

        boolean update() {
            double dx = targetX - x;
            double dy = targetY - y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < speed || dist < 2) {
                return true; // reached target
            }
            x += (dx / dist) * speed;
            y += (dy / dist) * speed;
            return false;
        }
    }

    private List<NetNode> topologyNodes = new ArrayList<>();

    public NetworkCanvasPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(500, 220));
        setMinimumSize(new Dimension(300, 180));

        initNodes();

        // 30 FPS animation timer for glowing cables, pulsing radar, and traveling packets
        animTimer = new Timer(33, e -> {
            animationTick++;
            updateParticles();
            repaint();
        });
        animTimer.start();
    }

    public void setRoom(int room) {
        this.currentRoom = room;
        particles.clear();
        repaint();
    }

    private void initNodes() {
        topologyNodes.clear();
        // A, B, C, D, E for Room 2 routing
        topologyNodes.add(new NetNode("A", 60, 110, UITheme.ACCENT_CYAN, "Source (Client)"));
        topologyNodes.add(new NetNode("B", 180, 50, UITheme.TEXT_MUTED, "Router B"));
        topologyNodes.add(new NetNode("C", 240, 150, UITheme.ACCENT_GREEN, "Router C (Fast Path)"));
        topologyNodes.add(new NetNode("D", 320, 50, UITheme.TEXT_MUTED, "Router D"));
        topologyNodes.add(new NetNode("E", 430, 110, UITheme.ACCENT_AMBER, "Target (Server)"));
    }

    private void updateParticles() {
        // Spawn periodic packets
        if (animationTick % 12 == 0) {
            if (currentRoom == 2) {
                // Packet traveling on A -> C -> E (Shortest Path)
                if (random.nextBoolean()) {
                    particles.add(new PacketParticle(60, 110, 240, 150, 4.0, UITheme.ACCENT_GREEN));
                } else {
                    particles.add(new PacketParticle(240, 150, 430, 110, 4.0, UITheme.ACCENT_GREEN));
                }
            } else if (currentRoom == 1) {
                // Data streaming into Firewall
                particles.add(new PacketParticle(50, 110, getWidth() / 2.0, 110, 5.0, UITheme.ACCENT_CYAN));
            } else {
                // Final Room: Orbiting escape packets
                double angle = (animationTick * 0.08) % (2 * Math.PI);
                double cx = getWidth() / 2.0;
                double cy = getHeight() / 2.0;
                particles.add(new PacketParticle(cx + Math.cos(angle) * 70, cy + Math.sin(angle) * 50, cx, cy, 3.0, UITheme.ACCENT_PURPLE));
            }
        }

        particles.removeIf(PacketParticle::update);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        UITheme.setupAntiAliasing(g);
        Graphics2D g2d = (Graphics2D) g.create();

        int w = getWidth();
        int h = getHeight();

        // 1. Cyber Grid Background with subtle glowing lines
        drawCyberGrid(g2d, w, h);

        // 2. Render Scene based on Current Room
        if (currentRoom == 1) {
            drawRoom1GatewayChamber(g2d, w, h);
        } else if (currentRoom == 2) {
            drawRoom2RoutingTopology(g2d, w, h);
        } else {
            drawRoom3MainframeEscape(g2d, w, h);
        }

        // 3. Render Traveling Data Packets
        drawParticles(g2d);

        // 4. Subtle scanline / vignette effect
        drawVignette(g2d, w, h);

        g2d.dispose();
    }

    private void drawCyberGrid(Graphics2D g, int w, int h) {
        g.setColor(new Color(13, 20, 35));
        g.fillRect(0, 0, w, h);

        g.setColor(new Color(24, 38, 64, 70));
        g.setStroke(new BasicStroke(1.0f));

        int gridSize = 24;
        for (int x = 0; x < w; x += gridSize) {
            g.drawLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += gridSize) {
            g.drawLine(0, y, w, y);
        }

        // Pulsing radar sweep in top corner
        int radarX = w - 40;
        int radarY = 30;
        int radarR = 20;
        g.setColor(new Color(0, 229, 255, 40));
        g.drawOval(radarX - radarR, radarY - radarR, radarR * 2, radarR * 2);
        g.drawOval(radarX - radarR / 2, radarY - radarR / 2, radarR, radarR);
        double angle = (animationTick * 0.05) % (2 * Math.PI);
        g.setColor(UITheme.ACCENT_CYAN);
        g.drawLine(radarX, radarY, (int) (radarX + Math.cos(angle) * radarR), (int) (radarY + Math.sin(angle) * radarR));
    }

    private void drawRoom1GatewayChamber(Graphics2D g, int w, int h) {
        // Left: Terminal Workstation
        int leftX = 70;
        int centerY = h / 2;

        // Terminal Box
        g.setColor(new Color(18, 28, 48));
        g.fillRoundRect(leftX - 35, centerY - 45, 70, 90, 10, 10);
        g.setColor(UITheme.ACCENT_CYAN);
        g.setStroke(new BasicStroke(1.8f));
        g.drawRoundRect(leftX - 35, centerY - 45, 70, 90, 10, 10);

        // Screen glow
        g.setColor(new Color(0, 229, 255, 30));
        g.fillRect(leftX - 25, centerY - 35, 50, 45);
        g.setColor(UITheme.ACCENT_CYAN);
        g.setFont(UITheme.FONT_SMALL_BOLD);
        g.drawString("CLIENT", leftX - 20, centerY + 30);

        // Center: Locked Firewall Laser Gate
        int midX = w / 2;
        float laserPulse = (float) (Math.sin(animationTick * 0.15) * 0.3 + 0.7);
        g.setColor(new Color(239, 68, 68, (int) (laserPulse * 220)));
        g.setStroke(new BasicStroke(3.5f));
        g.drawLine(midX, 20, midX, h - 20);

        // Firewall Gate Icon / Shield
        g.setColor(new Color(30, 20, 25));
        g.fillRoundRect(midX - 32, centerY - 30, 64, 60, 12, 12);
        g.setColor(UITheme.ACCENT_RED);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(midX - 32, centerY - 30, 64, 60, 12, 12);
        g.setFont(UITheme.FONT_SMALL_BOLD);
        g.drawString("FIREWALL", midX - 28, centerY - 10);
        g.drawString("[LOCKED]", midX - 26, centerY + 12);

        // Right: Target Host Port 80
        int rightX = w - 70;
        g.setColor(new Color(18, 28, 48));
        g.fillRoundRect(rightX - 35, centerY - 45, 70, 90, 10, 10);
        g.setColor(UITheme.ACCENT_AMBER);
        g.setStroke(new BasicStroke(1.8f));
        g.drawRoundRect(rightX - 35, centerY - 45, 70, 90, 10, 10);
        g.drawString("SERVER", rightX - 22, centerY + 30);
        g.setFont(UITheme.FONT_SMALL);
        g.setColor(UITheme.TEXT_SECONDARY);
        g.drawString("PORT :80", rightX - 24, centerY - 15);

        // Connecting Cable
        g.setColor(new Color(0, 229, 255, 100));
        g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{6, 6}, animationTick % 12));
        g.drawLine(leftX + 35, centerY, midX - 32, centerY);
        g.setColor(new Color(245, 158, 11, 80));
        g.drawLine(midX + 32, centerY, rightX - 35, centerY);
    }

    private void drawRoom2RoutingTopology(Graphics2D g, int w, int h) {
        // Adjust coordinates relative to current width
        int startX = 60;
        int endX = w - 60;
        int midX = w / 2;
        int topY = 45;
        int botY = h - 45;
        int midY = h / 2;

        int ax = startX, ay = midY;
        int bx = startX + (endX - startX) / 4, by = topY;
        int cx = midX, cy = botY;
        int dx = startX + 3 * (endX - startX) / 4, dy = topY;
        int ex = endX, ey = midY;

        // Draw Links: A-B-D-E and A-C-E
        g.setColor(new Color(55, 65, 81, 150));
        g.setStroke(new BasicStroke(2.0f));
        g.drawLine(ax, ay, bx, by);
        g.drawLine(bx, by, dx, dy);
        g.drawLine(dx, dy, ex, ey);

        // Highlight Shortest Path A-C-E with pulsing green glow
        float glow = (float) (Math.sin(animationTick * 0.1) * 0.25 + 0.75);
        g.setColor(new Color(16, 185, 129, (int) (glow * 200)));
        g.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{8, 6}, (animationTick * 2) % 14));
        g.drawLine(ax, ay, cx, cy);
        g.drawLine(cx, cy, ex, ey);

        // Draw Nodes
        drawNode(g, ax, ay, "A", "Client", UITheme.ACCENT_CYAN);
        drawNode(g, bx, by, "B", "Hops=3", UITheme.TEXT_MUTED);
        drawNode(g, cx, cy, "C", "Hops=2 ★", UITheme.ACCENT_GREEN);
        drawNode(g, dx, dy, "D", "Router", UITheme.TEXT_MUTED);
        drawNode(g, ex, ey, "E", "Server", UITheme.ACCENT_AMBER);
    }

    private void drawNode(Graphics2D g, int x, int y, String name, String sub, Color accent) {
        int r = 18;
        g.setColor(new Color(15, 23, 42));
        g.fillOval(x - r, y - r, r * 2, r * 2);
        g.setColor(accent);
        g.setStroke(new BasicStroke(2.0f));
        g.drawOval(x - r, y - r, r * 2, r * 2);

        g.setFont(UITheme.FONT_BODY_BOLD);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(name, x - fm.stringWidth(name) / 2, y + fm.getAscent() / 2 - 2);

        g.setFont(UITheme.FONT_SMALL);
        g.setColor(UITheme.TEXT_SECONDARY);
        FontMetrics fmSub = g.getFontMetrics();
        g.drawString(sub, x - fmSub.stringWidth(sub) / 2, y + r + 14);
    }

    private void drawRoom3MainframeEscape(Graphics2D g, int w, int h) {
        int cx = w / 2;
        int cy = h / 2;

        // Concentric Rotating Sequence Rings
        for (int ring = 1; ring <= 3; ring++) {
            int rad = ring * 32;
            double rot = (animationTick * 0.02 * (ring % 2 == 0 ? 1 : -1));
            g.setColor(new Color(139, 92, 246, 50 + ring * 25));
            g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{10 + ring * 4, 12}, (float) rot * 10));
            g.drawOval(cx - rad, cy - rad, rad * 2, rad * 2);
        }

        // Center Mainframe Core
        g.setColor(new Color(25, 18, 48));
        g.fillOval(cx - 26, cy - 26, 52, 52);
        g.setColor(UITheme.ACCENT_PURPLE);
        g.setStroke(new BasicStroke(2.5f));
        g.drawOval(cx - 26, cy - 26, 52, 52);

        g.setFont(UITheme.FONT_HUGE);
        g.setColor(Color.WHITE);
        g.drawString("🔓", cx - 14, cy + 10);

        // 3 Telemetry Sequence Locks (DNS -> ROUTE -> CONNECT)
        drawTelemetryLock(g, cx - 140, cy, "1. DNS", "8.8.8.8", UITheme.ACCENT_CYAN);
        drawTelemetryLock(g, cx, cy - 65, "2. ROUTE", "A-C-E (2 Hops)", UITheme.ACCENT_GREEN);
        drawTelemetryLock(g, cx + 140, cy, "3. CONNECT", "192.168.1.10:80", UITheme.ACCENT_AMBER);
    }

    private void drawTelemetryLock(Graphics2D g, int x, int y, String title, String val, Color c) {
        int boxW = 100;
        int boxH = 40;
        g.setColor(new Color(15, 23, 42, 220));
        g.fillRoundRect(x - boxW / 2, y - boxH / 2, boxW, boxH, 8, 8);
        g.setColor(c);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x - boxW / 2, y - boxH / 2, boxW, boxH, 8, 8);

        g.setFont(UITheme.FONT_SMALL_BOLD);
        g.drawString(title, x - boxW / 2 + 8, y - 4);
        g.setFont(UITheme.FONT_SMALL);
        g.setColor(UITheme.TEXT_SECONDARY);
        g.drawString(val, x - boxW / 2 + 8, y + 12);
    }

    private void drawParticles(Graphics2D g) {
        for (PacketParticle p : particles) {
            g.setColor(p.color);
            g.fillOval((int) p.x - 3, (int) p.y - 3, 7, 7);
            g.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), 90));
            g.fillOval((int) p.x - 6, (int) p.y - 6, 13, 13);
        }
    }

    private void drawVignette(Graphics2D g, int w, int h) {
        // Border frame
        g.setColor(new Color(0, 229, 255, 80));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRect(0, 0, w - 1, h - 1);

        // Corner brackets
        int len = 14;
        g.setColor(UITheme.ACCENT_CYAN);
        g.setStroke(new BasicStroke(2.5f));
        // Top-left
        g.drawLine(0, 0, len, 0); g.drawLine(0, 0, 0, len);
        // Top-right
        g.drawLine(w - 1, 0, w - 1 - len, 0); g.drawLine(w - 1, 0, w - 1, len);
        // Bottom-left
        g.drawLine(0, h - 1, len, h - 1); g.drawLine(0, h - 1, 0, h - 1 - len);
        // Bottom-right
        g.drawLine(w - 1, h - 1, w - 1 - len, h - 1); g.drawLine(w - 1, h - 1, w - 1, h - 1 - len);
    }
}
