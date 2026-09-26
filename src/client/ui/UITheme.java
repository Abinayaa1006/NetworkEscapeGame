package client.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

public class UITheme {

    // Backgrounds
    public static final Color BG_DARK = new Color(11, 15, 25);        // #0B0F19 Deep space
    public static final Color BG_CARD = new Color(20, 27, 44);        // #141B2C Elevated card
    public static final Color BG_CARD_HOVER = new Color(28, 38, 60);  // Card hover
    public static final Color BG_INPUT = new Color(15, 21, 36);       // #0F1524 Input field
    public static final Color BG_DARKER = new Color(8, 11, 19);       // Header/Sidebars

    // Borders & Lines
    public static final Color BORDER_SUBTLE = new Color(37, 51, 77);   // Card borders
    public static final Color BORDER_GLOW = new Color(0, 229, 255);    // Focus border

    // Accent Colors
    public static final Color ACCENT_CYAN = new Color(0, 229, 255);   // #00E5FF Primary neon
    public static final Color ACCENT_CYAN_DIM = new Color(0, 180, 204);
    public static final Color ACCENT_GREEN = new Color(16, 185, 129);  // #10B981 Success / Solved
    public static final Color ACCENT_PURPLE = new Color(139, 92, 246);// #8B5CF6 Clues & Team
    public static final Color ACCENT_AMBER = new Color(245, 158, 11);  // #F59E0B Warnings
    public static final Color ACCENT_RED = new Color(239, 68, 68);     // #EF4444 Errors / Failed
    public static final Color ACCENT_BLUE = new Color(59, 130, 246);   // #3B82F6 Info

    // Text Colors
    public static final Color TEXT_PRIMARY = new Color(243, 244, 246); // #F3F4F6
    public static final Color TEXT_SECONDARY = new Color(156, 163, 175); // #9CA3AF
    public static final Color TEXT_MUTED = new Color(107, 114, 128);    // #6B7280
    public static final Color TEXT_CYAN = new Color(56, 189, 248);     // Sky cyan

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 13);
    public static final Font FONT_MONO_BOLD = new Font("Consolas", Font.BOLD, 14);
    public static final Font FONT_HUGE = new Font("Segoe UI", Font.BOLD, 28);

    // Antialiasing helper
    public static void setupAntiAliasing(Graphics g) {
        if (g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
    }

    public static Border createEmptyPadding(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }
}
