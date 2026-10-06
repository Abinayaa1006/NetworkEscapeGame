package client.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Landing / splash screen for NETWORK ESCAPE ROOM.
 * Clean, high-performance static presentation using the cyber-room photo background,
 * dark gradient overlay, properly spaced titles, room chips, and interactive enter button.
 */
public class LandingPanel extends JPanel {

    public interface LandingListener {
        void onEnterClicked();
    }

    private LandingListener listener;

    // Background photo
    private BufferedImage bgImage;

    // Enter button
    private boolean btnHovered = false;
    private final Rectangle enterBtnBounds = new Rectangle();

    public LandingPanel(LandingListener listener) {
        this.listener = listener;
        setBackground(new Color(5, 8, 15));
        setLayout(null);
        loadBackground();
        setupMouseListeners();
    }

    public void stopAnimation() {
        // Retained for API compatibility; animations are disabled
    }

    // ── Load background photo ─────────────────────────────────────────────
    private void loadBackground() {
        try {
            InputStream is = getClass().getResourceAsStream("/client/ui/images/landing_bg.png");
            if (is != null) bgImage = ImageIO.read(is);
        } catch (Exception ignored) {}
    }

    // ── Mouse ────────────────────────────────────────────────────────────
    private void setupMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                boolean was = btnHovered;
                btnHovered = enterBtnBounds.contains(e.getPoint());
                if (was != btnHovered) repaint();
                setCursor(btnHovered
                    ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    : Cursor.getDefaultCursor());
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (enterBtnBounds.contains(e.getPoint())) {
                    if (listener != null) listener.onEnterClicked();
                }
            }
        });
    }

    // ── Paint ────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        UITheme.setupAntiAliasing(g2);

        int w = getWidth(), h = getHeight();

        drawBgPhoto(g2, w, h);
        drawOverlay(g2, w, h);
        drawCentralGlow(g2, w, h);
        drawTitle(g2, w, h);
        drawEnterButton(g2, w, h);
        drawFooter(g2, w, h);

        g2.dispose();
    }

    // ── Background photo ─────────────────────────────────────────────────
    private void drawBgPhoto(Graphics2D g, int w, int h) {
        if (bgImage != null) {
            // Scale to fill entire panel while preserving aspect ratio
            double imgRatio = (double) bgImage.getWidth() / bgImage.getHeight();
            double panRatio = (double) w / h;
            int dw, dh, dx, dy;
            if (panRatio > imgRatio) {
                dw = w; dh = (int) (w / imgRatio);
            } else {
                dh = h; dw = (int) (h * imgRatio);
            }
            dx = (w - dw) / 2; dy = (h - dh) / 2;
            g.drawImage(bgImage, dx, dy, dw, dh, null);
        } else {
            // Fallback solid
            g.setColor(new Color(5, 8, 18));
            g.fillRect(0, 0, w, h);
        }
    }

    // ── Dark overlay so text is legible ──────────────────────────────────
    private void drawOverlay(Graphics2D g, int w, int h) {
        GradientPaint top = new GradientPaint(0, 0, new Color(5, 8, 20, 210),
                                               0, h / 3, new Color(5, 8, 20, 110));
        g.setPaint(top);
        g.fillRect(0, 0, w, h / 3);

        GradientPaint mid = new GradientPaint(0, h / 3, new Color(5, 8, 20, 110),
                                               0, 2 * h / 3, new Color(5, 8, 20, 90));
        g.setPaint(mid);
        g.fillRect(0, h / 3, w, h / 3);

        GradientPaint bot = new GradientPaint(0, 2 * h / 3, new Color(5, 8, 20, 90),
                                               0, h, new Color(5, 8, 20, 240));
        g.setPaint(bot);
        g.fillRect(0, 2 * h / 3, w, h / 3);
    }

    // ── Central glow ─────────────────────────────────────────────────────
    private void drawCentralGlow(Graphics2D g, int w, int h) {
        int r = 260;
        RadialGradientPaint glow = new RadialGradientPaint(
            new Point2D.Float(w / 2f, h / 2f - 60), r,
            new float[]{0f, .55f, 1f},
            new Color[]{new Color(0, 160, 255, 22), new Color(0, 80, 180, 8), new Color(0, 0, 0, 0)}
        );
        g.setPaint(glow);
        g.fillOval(w / 2 - r, h / 2 - 60 - r, r * 2, r * 2);
    }

    // ── Title block ──────────────────────────────────────────────────────
    private void drawTitle(Graphics2D g, int w, int h) {
        int cy = h / 2 - 110;

        // Main title font & metrics first
        String title = "NETWORK ESCAPE ROOM";
        Font titleFont = new Font("Segoe UI", Font.BOLD, 54);
        g.setFont(titleFont);
        FontMetrics fm = g.getFontMetrics();
        int tx = (w - fm.stringWidth(title)) / 2;

        // Glow layers for title (static)
        g.setFont(titleFont);
        for (int lay = 6; lay >= 1; lay--) {
            float a = (6f - lay) / 6f * 0.14f;
            g.setColor(new Color(0f, 0.9f, 1f, a));
            g.drawString(title, tx + lay, cy + lay);
            g.drawString(title, tx - lay, cy + lay);
        }
        // Main gradient
        GradientPaint tg = new GradientPaint(tx, cy - fm.getAscent(),
                                              new Color(0, 230, 255),
                                              tx + fm.stringWidth(title), cy,
                                              new Color(120, 210, 255));
        g.setPaint(tg);
        g.drawString(title, tx, cy);

        // Subtitle
        String sub = "MULTIPLAYER CYBER ESCAPE CHALLENGE";
        Font subFont = new Font("Segoe UI", Font.BOLD, 15);
        g.setFont(subFont);
        FontMetrics sfm = g.getFontMetrics();
        g.setColor(new Color(200, 220, 255, 200));
        g.drawString(sub, (w - sfm.stringWidth(sub)) / 2, cy + 38);

        // Separator (static)
        int lineW = 360;
        int lx = (w - lineW) / 2;
        int sepY = cy + 54;
        GradientPaint lg1 = new GradientPaint(lx, 0, new Color(0, 0, 0, 0), w / 2f, 0, new Color(0, 229, 255, 180));
        g.setPaint(lg1);
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(lx, sepY, w / 2, sepY);
        GradientPaint lg2 = new GradientPaint(w / 2f, 0, new Color(0, 229, 255, 180), lx + lineW, 0, new Color(0, 0, 0, 0));
        g.setPaint(lg2);
        g.drawLine(w / 2, sepY, lx + lineW, sepY);

        // Room chips — centred as a group
        int chipGroupW = 360;
        int chipStartX = (w - chipGroupW) / 2;
        int chipY = cy + 68;
        drawRoomChip(g, chipStartX,       chipY, "ROOM 1",     "Initial Access",     UITheme.ACCENT_CYAN);
        drawRoomChip(g, chipStartX + 122, chipY, "ROOM 2",     "Topology & Routing", UITheme.ACCENT_GREEN);
        drawRoomChip(g, chipStartX + 244, chipY, "FINAL ROOM", "Escape Sequence",    UITheme.ACCENT_PURPLE);
    }

    private void drawRoomChip(Graphics2D g, int x, int y, String rm, String desc, Color accent) {
        int cw = 118, ch = 46;
        g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 22));
        g.fill(new RoundRectangle2D.Float(x, y, cw, ch, 10, 10));
        g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 110));
        g.setStroke(new BasicStroke(1f));
        g.draw(new RoundRectangle2D.Float(x, y, cw, ch, 10, 10));
        g.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g.setColor(accent);
        g.drawString(rm, x + 8, y + 17);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g.setColor(new Color(210, 220, 240, 200));
        g.drawString(desc, x + 8, y + 32);
    }

    // ── Enter button ─────────────────────────────────────────────────────
    private void drawEnterButton(Graphics2D g, int w, int h) {
        int bw = 290, bh = 56;
        int bx = (w - bw) / 2;
        int chipEndY = (h / 2 - 110) + 68 + 46;
        int by = Math.max(chipEndY + 28, Math.min(h / 2 + 40, h - bh - 90));
        enterBtnBounds.setBounds(bx, by, bw, bh);

        int glowA = btnHovered ? 100 : 45;

        // Outer glow rings
        for (int glow = 10; glow >= 1; glow--) {
            g.setColor(new Color(0, 180, 255, glowA / glow));
            g.setStroke(new BasicStroke(glow * 1.3f));
            g.draw(new RoundRectangle2D.Float(bx, by, bw, bh, 14, 14));
        }

        // Fill
        GradientPaint fill = new GradientPaint(bx, by,     new Color(0, 70, 120, 230),
                                                bx, by+bh,  new Color(0, 30, 75, 230));
        g.setPaint(fill); g.fill(new RoundRectangle2D.Float(bx, by, bw, bh, 14, 14));

        // Border
        g.setColor(btnHovered ? new Color(0, 229, 255) : new Color(0, 180, 220));
        g.setStroke(new BasicStroke(btnHovered ? 2f : 1.5f));
        g.draw(new RoundRectangle2D.Float(bx+.5f, by+.5f, bw-1, bh-1, 14, 14));

        // Button text
        String bt = ">> ENTER THE ESCAPE ROOM <<";
        g.setFont(new Font("Segoe UI", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        int tx = bx + (bw - fm.stringWidth(bt)) / 2;
        int ty = by + (bh + fm.getAscent() - fm.getDescent()) / 2;
        g.setColor(btnHovered ? Color.WHITE : new Color(0, 229, 255));
        g.drawString(bt, tx, ty);

        // Hint below the button
        int hintY = enterBtnBounds.y + bh + 18;
        if (hintY < h - 40) {
            g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            String hint = "Click to connect and begin your mission";
            FontMetrics hfm = g.getFontMetrics();
            g.setColor(new Color(160, 180, 210, 180));
            g.drawString(hint, bx + (bw - hfm.stringWidth(hint)) / 2, hintY);
        }
    }

    // ── Footer ───────────────────────────────────────────────────────────
    private void drawFooter(Graphics2D g, int w, int h) {
        g.setFont(new Font("Consolas", Font.PLAIN, 10));
        g.setColor(new Color(0, 229, 255, 70));
        g.drawString("SYS.VER // 2.4.0-TCP", 18, h - 16);

        String footer = "2-4 Players  |  3 Rooms  |  Network Puzzles & Team Challenges";
        g.setColor(new Color(150, 170, 200, 110));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(footer, (w - fm.stringWidth(footer)) / 2, h - 16);

        g.setColor(new Color(0, 229, 255, 70));
        g.drawString("NET.PORT // 5000:ACTIVE", w - 165, h - 16);
    }
}
