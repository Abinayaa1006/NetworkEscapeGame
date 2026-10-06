package client.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

/**
 * Full-screen animated "ROOM UNLOCKED" overlay that plays over the game window.
 * Shows a cinematic expanding-ring + lock-open animation, then fades out.
 *
 * Usage:
 *   RoomUnlockedAnimation.show(parentFrame, roomNumber, onComplete);
 */
public class RoomUnlockedAnimation extends JPanel {

    public interface OnComplete { void done(); }

    // ── Static factory ────────────────────────────────────────────────────
    public static void show(JFrame parent, int roomNumber, OnComplete onComplete) {
        // Create a full-frame glass pane overlay
        RoomUnlockedAnimation anim = new RoomUnlockedAnimation(parent, roomNumber, onComplete);
        parent.setGlassPane(anim);
        anim.setVisible(true);
        anim.startAnimation();
    }

    // ── Config ────────────────────────────────────────────────────────────
    private static final int TOTAL_TICKS = 120; // ~4 seconds at 30 fps

    private final JFrame parent;
    private final int roomNumber;
    private final OnComplete onComplete;

    private Timer animTimer;
    private int tick = 0;

    // Animation state
    private float globalAlpha  = 0f;   // fade in / out
    private float ringScale    = 0f;   // expanding ring
    private float textSlide    = 80f;  // text drops in from above
    private boolean fadingOut  = false;

    // Room-specific colours & text
    private final Color accentColor;
    private final String roomLabel;
    private final String roomDesc;

    // ── Constructor ───────────────────────────────────────────────────────
    private RoomUnlockedAnimation(JFrame parent, int roomNumber, OnComplete onComplete) {
        this.parent      = parent;
        this.roomNumber  = roomNumber;
        this.onComplete  = onComplete;
        setOpaque(false);
        setLayout(null);

        switch (roomNumber) {
            case 1  -> { accentColor = UITheme.ACCENT_CYAN;
                         roomLabel  = "ROOM 1 COMPLETE";
                         roomDesc   = "Initial Access & Configuration — Cleared!"; }
            case 2  -> { accentColor = UITheme.ACCENT_GREEN;
                         roomLabel  = "ROOM 2 UNLOCKED";
                         roomDesc   = "Network Topology & Routing — Engage!"; }
            default -> { accentColor = UITheme.ACCENT_PURPLE;
                         roomLabel  = "FINAL ROOM UNLOCKED";
                         roomDesc   = "Network Escape Sequence — This is it!"; }
        }

        // Click to skip
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { finishEarly(); }
        });
    }

    // ── Animation control ─────────────────────────────────────────────────
    public void startAnimation() {
        tick = 0;
        animTimer = new Timer(33, e -> {
            tick++;
            updateState();
            repaint();
            if (tick >= TOTAL_TICKS) finishEarly();
        });
        animTimer.start();
    }

    private void updateState() {
        float progress = (float) tick / TOTAL_TICKS;

        // Phase 1 (0–15%): fade in
        if (progress < 0.15f) {
            globalAlpha = progress / 0.15f;
            ringScale   = easeOut(progress / 0.15f) * 0.6f;
            textSlide   = 80f * (1f - easeOut(progress / 0.15f));
            fadingOut   = false;

        // Phase 2 (15–75%): hold
        } else if (progress < 0.75f) {
            globalAlpha = 1f;
            float p2    = (progress - 0.15f) / 0.60f;
            ringScale   = 0.6f + easeOut(p2) * 0.4f;
            textSlide   = 0f;

        // Phase 3 (75–100%): fade out
        } else {
            float p3    = (progress - 0.75f) / 0.25f;
            globalAlpha = 1f - easeIn(p3);
            fadingOut   = true;
        }
    }

    private void finishEarly() {
        if (animTimer != null) { animTimer.stop(); animTimer = null; }
        setVisible(false);
        parent.setGlassPane(new JPanel()); // restore neutral glass pane
        if (onComplete != null) onComplete.done();
    }

    // ── Paint ─────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        UITheme.setupAntiAliasing(g2);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, Math.max(0f, globalAlpha))));

        int w = getWidth(), h = getHeight();
        int cx = w / 2, cy = h / 2;

        // 1. Full dark overlay
        g2.setColor(new Color(3, 6, 15, 200));
        g2.fillRect(0, 0, w, h);

        // 2. Expanding concentric rings
        drawRings(g2, cx, cy, w);

        // 3. Central icon + label
        drawCentralContent(g2, cx, cy);

        // 4. Scan-line sweep (moves downward)
        drawScanSweep(g2, w, h);

        // 5. Corner bracket markers
        drawCornerMarkers(g2, w, h);

        g2.dispose();
    }

    private void drawRings(Graphics2D g, int cx, int cy, int w) {
        int maxR = (int) (Math.min(getWidth(), getHeight()) * 0.42 * ringScale);
        for (int ring = 4; ring >= 1; ring--) {
            int r = (int) (maxR * (ring / 4.0));
            float alpha = (4f - ring + 1) / 4f * 0.35f * globalAlpha;
            g.setColor(new Color(
                accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(),
                (int) (alpha * 255)));
            g.setStroke(new BasicStroke(2.5f - ring * 0.4f, BasicStroke.CAP_ROUND,
                                         BasicStroke.JOIN_ROUND));
            g.drawOval(cx - r, cy - r, r * 2, r * 2);
        }

        // Solid glow dot at centre
        int dotR = (int) (18 * ringScale);
        RadialGradientPaint dot = new RadialGradientPaint(
            new Point2D.Float(cx, cy), dotR,
            new float[]{0f, 1f},
            new Color[]{accentColor, new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 0)}
        );
        g.setPaint(dot);
        g.fillOval(cx - dotR, cy - dotR, dotR * 2, dotR * 2);
    }

    private void drawCentralContent(Graphics2D g, int cx, int cy) {
        int slideY = (int) textSlide;

        // Lock-open emoji / icon
        g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 54));
        FontMetrics efm = g.getFontMetrics();
        String lockIcon = "🔓";
        g.setColor(Color.WHITE);
        g.drawString(lockIcon, cx - efm.stringWidth(lockIcon) / 2, cy - 50 + slideY);

        // Room label (big)
        String label = roomLabel;
        Font labelFont = new Font("Segoe UI", Font.BOLD, 36);
        g.setFont(labelFont);
        FontMetrics lfm = g.getFontMetrics();
        int lx = cx - lfm.stringWidth(label) / 2;

        // Glow
        for (int glow = 6; glow >= 1; glow--) {
            float a = (6f - glow) / 6f * .25f;
            g.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), (int) (a * 255)));
            g.drawString(label, lx + glow, cy + 10 + slideY + glow);
            g.drawString(label, lx - glow, cy + 10 + slideY + glow);
        }
        g.setColor(accentColor);
        g.drawString(label, lx, cy + 10 + slideY);

        // Description sub-text
        String desc = roomDesc;
        Font descFont = new Font("Segoe UI", Font.PLAIN, 15);
        g.setFont(descFont);
        FontMetrics dfm = g.getFontMetrics();
        g.setColor(new Color(200, 220, 255, 220));
        g.drawString(desc, cx - dfm.stringWidth(desc) / 2, cy + 52 + slideY);

        // "Click anywhere to continue" hint
        if (!fadingOut) {
            float blink = (float) (Math.abs(Math.sin(tick * .08)) * .7 + .3);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            FontMetrics hfm = g.getFontMetrics();
            String hint = "Click anywhere to continue";
            g.setColor(new Color(150, 170, 200, (int) (blink * 200)));
            g.drawString(hint, cx - hfm.stringWidth(hint) / 2, cy + 90 + slideY);
        }
    }

    private void drawScanSweep(Graphics2D g, int w, int h) {
        float progress = (float) tick / TOTAL_TICKS;
        if (progress < 0.6f) {
            int sy = (int) (h * (progress / 0.6f));
            int r = accentColor.getRed(), gr = accentColor.getGreen(), b = accentColor.getBlue();
            // Fade in (top half of the sweep band)
            GradientPaint fadeIn = new GradientPaint(0, sy - 4, new Color(r, gr, b, 0),
                                                      0, sy,     new Color(r, gr, b, 80));
            g.setPaint(fadeIn);
            g.fillRect(0, sy - 4, w, 4);
            // Fade out (bottom half of the sweep band)
            GradientPaint fadeOut = new GradientPaint(0, sy,     new Color(r, gr, b, 80),
                                                       0, sy + 4, new Color(r, gr, b, 0));
            g.setPaint(fadeOut);
            g.fillRect(0, sy, w, 4);
        }
    }

    private void drawCornerMarkers(Graphics2D g, int w, int h) {
        g.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 140));
        g.setStroke(new BasicStroke(2f));
        int m = 20, len = 22;
        // Top-left
        g.drawLine(m, m, m + len, m); g.drawLine(m, m, m, m + len);
        // Top-right
        g.drawLine(w - m, m, w - m - len, m); g.drawLine(w - m, m, w - m, m + len);
        // Bottom-left
        g.drawLine(m, h - m, m + len, h - m); g.drawLine(m, h - m, m, h - m - len);
        // Bottom-right
        g.drawLine(w - m, h - m, w - m - len, h - m); g.drawLine(w - m, h - m, w - m, h - m - len);
    }

    // ── Easing ────────────────────────────────────────────────────────────
    private static float easeOut(float t) { return 1f - (1f - t) * (1f - t); }
    private static float easeIn(float t)  { return t * t; }
}
