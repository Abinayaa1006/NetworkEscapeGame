package client.ui;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Displays a real cyber/escape-room photograph as the room banner,
 * with a dark gradient overlay and room label on top.
 * No animation timers — zero lag contribution.
 */
public class RoomAtmospherePanel extends JPanel {

    private int roomNumber = 1;

    private BufferedImage room1Img;
    private BufferedImage room2Img;
    private BufferedImage room3Img;

    public RoomAtmospherePanel() {
        setPreferredSize(new Dimension(800, 130));
        setMinimumSize(new Dimension(300, 110));
        setOpaque(false);
        loadImages();
    }

    // ── Load images from bundled resources ───────────────────────────────────
    private void loadImages() {
        room1Img = loadImg("/client/ui/images/room1.jpg");
        room2Img = loadImg("/client/ui/images/room2.jpg");
        room3Img = loadImg("/client/ui/images/room3.jpg");
    }

    private BufferedImage loadImg(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is != null) {
                return ImageIO.read(is);
            }
        } catch (Exception ignored) {}
        return null;
    }

    public void setRoom(int room) {
        this.roomNumber = room;
        repaint();
    }

    // ── Painting ─────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        UITheme.setupAntiAliasing(g2);

        int w = getWidth(), h = getHeight();

        // Pick the right image and accent colour per room
        BufferedImage img;
        Color accent;
        String labelText;
        switch (roomNumber) {
            case 1 -> { img = room1Img; accent = UITheme.ACCENT_CYAN;
                        labelText = "ROOM 1  —  INITIAL ACCESS & CONFIGURATION"; }
            case 2 -> { img = room2Img; accent = UITheme.ACCENT_GREEN;
                        labelText = "ROOM 2  —  NETWORK TOPOLOGY & ROUTING"; }
            default -> { img = room3Img; accent = UITheme.ACCENT_PURPLE;
                         labelText = "FINAL ROOM  —  NETWORK ESCAPE SEQUENCE"; }
        }

        // Draw the photograph scaled to fill the banner
        if (img != null) {
            g2.drawImage(img, 0, 0, w, h, null);
        } else {
            // Fallback solid background if image failed to load
            g2.setColor(new Color(10, 18, 35));
            g2.fillRect(0, 0, w, h);
        }

        // Dark gradient overlay (bottom-heavy so text is readable)
        GradientPaint overlay = new GradientPaint(
            0, 0,      new Color(5, 8, 18, 140),
            0, h,      new Color(5, 8, 18, 220)
        );
        g2.setPaint(overlay);
        g2.fillRect(0, 0, w, h);

        // Left accent bar
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 220));
        g2.fillRect(0, 0, 4, h);

        // Subtle top border glow line
        GradientPaint topLine = new GradientPaint(
            0, 0, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 180),
            w, 0, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 20)
        );
        g2.setPaint(topLine);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(0, 0, w, 0);

        // Room label pill in bottom-left
        drawRoomLabel(g2, 14, h - 14, labelText, accent);

        // Corner brackets (top-right)
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 160));
        g2.setStroke(new BasicStroke(1.8f));
        int len = 12;
        g2.drawLine(w - 18, 8, w - 8, 8);
        g2.drawLine(w - 8, 8, w - 8, 8 + len);
        g2.drawLine(w - 8, h - 8, w - 8, h - 8 - len);
        g2.drawLine(w - 8, h - 8, w - 8 - len, h - 8);

        g2.dispose();
    }

    private void drawRoomLabel(Graphics2D g, int x, int y, String text, Color accent) {
        g.setFont(new Font("Segoe UI", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(text);
        int pad = 10, h = fm.getHeight() + 6;

        // Pill background
        g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 35));
        g.fill(new RoundRectangle2D.Float(x - pad, y - fm.getAscent() - 3, tw + pad * 2, h, 8, 8));
        g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 110));
        g.setStroke(new BasicStroke(1f));
        g.draw(new RoundRectangle2D.Float(x - pad, y - fm.getAscent() - 3, tw + pad * 2, h, 8, 8));

        // Label text
        g.setColor(accent);
        g.drawString(text, x, y);
    }
}
