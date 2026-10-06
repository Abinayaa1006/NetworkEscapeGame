package client.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.Border;

public class ModernComponents {

    // ==========================================
    // MODERN CARD PANEL
    // ==========================================
    public static class CardPanel extends JPanel {
        private int cornerRadius = 16;
        private Color bgColor = UITheme.BG_CARD;
        private Color borderColor = UITheme.BORDER_SUBTLE;
        private boolean drawBorder = true;

        public CardPanel() {
            this(16, UITheme.BG_CARD, UITheme.BORDER_SUBTLE);
        }

        public CardPanel(int cornerRadius, Color bgColor, Color borderColor) {
            this.cornerRadius = cornerRadius;
            this.bgColor = bgColor;
            this.borderColor = borderColor;
            setOpaque(false);
        }

        public void setCardBackground(Color color) {
            this.bgColor = color;
            repaint();
        }

        public void setBorderColor(Color color) {
            this.borderColor = color;
            repaint();
        }

        public void setDrawBorder(boolean draw) {
            this.drawBorder = draw;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            UITheme.setupAntiAliasing(g);
            Graphics2D g2d = (Graphics2D) g.create();

            int w = getWidth();
            int h = getHeight();

            // Background Fill
            g2d.setColor(bgColor);
            g2d.fill(new RoundRectangle2D.Double(0, 0, w, h, cornerRadius, cornerRadius));

            // Border Stroke
            if (drawBorder && borderColor != null) {
                g2d.setColor(borderColor);
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, cornerRadius, cornerRadius));
            }

            g2d.dispose();
            super.paintComponent(g);
        }
    }

    // ==========================================
    // MODERN BUTTON
    // ==========================================
    public enum ButtonStyle {
        PRIMARY(UITheme.ACCENT_CYAN, UITheme.BG_DARK, new Color(100, 240, 255)),
        SUCCESS(UITheme.ACCENT_GREEN, Color.WHITE, new Color(52, 211, 153)),
        WARNING(UITheme.ACCENT_AMBER, UITheme.BG_DARK, new Color(251, 191, 36)),
        DANGER(UITheme.ACCENT_RED, Color.WHITE, new Color(248, 113, 113)),
        PURPLE(UITheme.ACCENT_PURPLE, Color.WHITE, new Color(167, 139, 250)),
        SECONDARY(UITheme.BG_CARD_HOVER, UITheme.TEXT_PRIMARY, UITheme.BORDER_SUBTLE),
        OUTLINE(new Color(0, 0, 0, 0), UITheme.ACCENT_CYAN, UITheme.ACCENT_CYAN);

        final Color baseColor;
        final Color textColor;
        final Color hoverColor;

        ButtonStyle(Color base, Color text, Color hover) {
            this.baseColor = base;
            this.textColor = text;
            this.hoverColor = hover;
        }
    }

    public static class ModernButton extends JButton {
        private ButtonStyle style = ButtonStyle.PRIMARY;
        private boolean isHovered = false;
        private boolean isPressed = false;
        private int cornerRadius = 12;

        public ModernButton(String text) {
            this(text, ButtonStyle.PRIMARY);
        }

        public ModernButton(String text, ButtonStyle style) {
            super(text);
            this.style = style;
            setFont(UITheme.FONT_BODY_BOLD);
            setForeground(style.textColor);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(10, 20, 10, 20));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) {
                        isHovered = true;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    isPressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (isEnabled()) {
                        isPressed = true;
                        SoundEffects.playClick();
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        public void setButtonStyle(ButtonStyle style) {
            this.style = style;
            setForeground(style.textColor);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            UITheme.setupAntiAliasing(g);
            Graphics2D g2d = (Graphics2D) g.create();

            int w = getWidth();
            int h = getHeight();

            Color fill;
            if (!isEnabled()) {
                fill = new Color(50, 60, 80);
                setForeground(UITheme.TEXT_MUTED);
            } else if (isPressed) {
                fill = style.hoverColor.darker();
                setForeground(style.textColor);
            } else if (isHovered) {
                fill = style.hoverColor;
                setForeground(style == ButtonStyle.OUTLINE ? Color.WHITE : style.textColor);
            } else {
                fill = style.baseColor;
                setForeground(style.textColor);
            }

            if (style == ButtonStyle.OUTLINE) {
                if (isHovered) {
                    g2d.setColor(new Color(UITheme.ACCENT_CYAN.getRed(), UITheme.ACCENT_CYAN.getGreen(), UITheme.ACCENT_CYAN.getBlue(), 40));
                    g2d.fill(new RoundRectangle2D.Double(0, 0, w, h, cornerRadius, cornerRadius));
                }
                g2d.setColor(UITheme.ACCENT_CYAN);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.draw(new RoundRectangle2D.Double(1, 1, w - 2, h - 2, cornerRadius, cornerRadius));
            } else {
                g2d.setColor(fill);
                g2d.fill(new RoundRectangle2D.Double(0, 0, w, h, cornerRadius, cornerRadius));

                // Subtle inner top highlight
                if (isEnabled() && !isPressed) {
                    g2d.setColor(new Color(255, 255, 255, 30));
                    g2d.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h / 2.0, cornerRadius, cornerRadius));
                }
            }

            g2d.dispose();
            super.paintComponent(g);
        }
    }

    // ==========================================
    // MODERN TEXT FIELD
    // ==========================================
    public static class ModernTextField extends JTextField {
        private String placeholder = "";
        private boolean isFocused = false;
        private int cornerRadius = 10;

        public ModernTextField(String placeholder) {
            this(placeholder, 15);
        }

        public ModernTextField(String placeholder, int columns) {
            super(columns);
            this.placeholder = placeholder;
            setFont(UITheme.FONT_BODY);
            setForeground(UITheme.TEXT_PRIMARY);
            setCaretColor(UITheme.ACCENT_CYAN);
            setBackground(UITheme.BG_INPUT);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    isFocused = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    isFocused = false;
                    repaint();
                }
            });
        }

        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            UITheme.setupAntiAliasing(g);
            Graphics2D g2d = (Graphics2D) g.create();

            int w = getWidth();
            int h = getHeight();

            // Background
            g2d.setColor(UITheme.BG_INPUT);
            g2d.fill(new RoundRectangle2D.Double(0, 0, w, h, cornerRadius, cornerRadius));

            // Border
            if (isFocused) {
                g2d.setColor(UITheme.BORDER_GLOW);
                g2d.setStroke(new BasicStroke(1.8f));
            } else {
                g2d.setColor(UITheme.BORDER_SUBTLE);
                g2d.setStroke(new BasicStroke(1.0f));
            }
            g2d.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, cornerRadius, cornerRadius));

            g2d.dispose();
            super.paintComponent(g);

            // Placeholder Text
            if (getText().isEmpty() && !isFocused && placeholder != null && !placeholder.isEmpty()) {
                Graphics2D gText = (Graphics2D) g.create();
                UITheme.setupAntiAliasing(gText);
                gText.setColor(UITheme.TEXT_MUTED);
                gText.setFont(getFont());
                FontMetrics fm = gText.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                gText.drawString(placeholder, getInsets().left, y);
                gText.dispose();
            }
        }
    }

    // ==========================================
    // STATUS BADGE
    // ==========================================
    public static class StatusBadge extends JPanel {
        private String text;
        private Color bgColor;
        private Color textColor;
        private JLabel label;

        public StatusBadge(String text, Color bgColor, Color textColor) {
            this.text = text;
            this.bgColor = bgColor;
            this.textColor = textColor;
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.CENTER, 8, 4));

            label = new JLabel(text);
            label.setFont(UITheme.FONT_SMALL_BOLD);
            label.setForeground(textColor);
            add(label);
        }

        public void setBadge(String text, Color bgColor, Color textColor) {
            this.text = text;
            this.bgColor = bgColor;
            this.textColor = textColor;
            label.setText(text);
            label.setForeground(textColor);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            UITheme.setupAntiAliasing(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(bgColor);
            g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
            g2d.dispose();
            super.paintComponent(g);
        }
    }

    // ==========================================
    // SECTION HEADER
    // ==========================================
    public static class SectionHeader extends JPanel {
        public SectionHeader(String title, String subtitle, Color accentColor) {
            setOpaque(false);
            setLayout(new BorderLayout(10, 0));

            // Accent bar
            JPanel bar = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    UITheme.setupAntiAliasing(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setColor(accentColor != null ? accentColor : UITheme.ACCENT_CYAN);
                    g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 4, 4));
                    g2d.dispose();
                }
            };
            bar.setPreferredSize(new Dimension(4, 32));
            bar.setOpaque(false);

            JPanel textPanel = new JPanel();
            textPanel.setOpaque(false);
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(UITheme.FONT_SUBTITLE);
            titleLabel.setForeground(UITheme.TEXT_PRIMARY);

            textPanel.add(titleLabel);

            if (subtitle != null && !subtitle.isEmpty()) {
                JLabel subLabel = new JLabel(subtitle);
                subLabel.setFont(UITheme.FONT_SMALL);
                subLabel.setForeground(UITheme.TEXT_SECONDARY);
                textPanel.add(subLabel);
            }

            add(bar, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
        }
    }

    // ==========================================
    // INTERACTIVE PUZZLE OPTION TILE
    // ==========================================
    public static class OptionTile extends JPanel {
        private String optionKey; // "A", "B", "C", "D"
        private String optionText;
        private boolean isSelected = false;
        private boolean isHovered = false;
        private ActionListener onSelectListener;

        public OptionTile(String optionKey, String optionText) {
            this.optionKey = optionKey;
            this.optionText = optionText;
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setLayout(new BorderLayout(14, 0));
            setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

            // Option Key Badge
            JLabel keyLabel = new JLabel(optionKey, SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    UITheme.setupAntiAliasing(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setColor(isSelected ? UITheme.ACCENT_CYAN : (isHovered ? UITheme.BG_CARD_HOVER : UITheme.BG_INPUT));
                    g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
                    g2d.setColor(isSelected ? UITheme.BG_DARK : UITheme.TEXT_PRIMARY);
                    g2d.dispose();
                    super.paintComponent(g);
                }
            };
            keyLabel.setPreferredSize(new Dimension(32, 32));
            keyLabel.setFont(UITheme.FONT_BODY_BOLD);
            keyLabel.setForeground(isSelected ? UITheme.BG_DARK : UITheme.TEXT_CYAN);

            // Option Content
            JLabel textLabel = new JLabel("<html>" + optionText + "</html>");
            textLabel.setFont(UITheme.FONT_BODY);
            textLabel.setForeground(UITheme.TEXT_PRIMARY);

            add(keyLabel, BorderLayout.WEST);
            add(textLabel, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) {
                        isHovered = true;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (isEnabled()) {
                        SoundEffects.playClick();
                        if (onSelectListener != null) {
                            onSelectListener.actionPerformed(new ActionEvent(OptionTile.this, ActionEvent.ACTION_PERFORMED, optionKey));
                        }
                    }
                }
            });
        }

        public String getOptionKey() {
            return optionKey;
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
            repaint();
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setOnSelectListener(ActionListener l) {
            this.onSelectListener = l;
        }

        @Override
        protected void paintComponent(Graphics g) {
            UITheme.setupAntiAliasing(g);
            Graphics2D g2d = (Graphics2D) g.create();

            int w = getWidth();
            int h = getHeight();

            if (isSelected) {
                g2d.setColor(new Color(0, 229, 255, 35));
                g2d.fill(new RoundRectangle2D.Double(0, 0, w, h, 12, 12));
                g2d.setColor(UITheme.ACCENT_CYAN);
                g2d.setStroke(new BasicStroke(1.8f));
                g2d.draw(new RoundRectangle2D.Double(1, 1, w - 2, h - 2, 12, 12));
            } else if (isHovered) {
                g2d.setColor(UITheme.BG_CARD_HOVER);
                g2d.fill(new RoundRectangle2D.Double(0, 0, w, h, 12, 12));
                g2d.setColor(UITheme.BORDER_GLOW.darker());
                g2d.setStroke(new BasicStroke(1.0f));
                g2d.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 12, 12));
            } else {
                g2d.setColor(UITheme.BG_INPUT);
                g2d.fill(new RoundRectangle2D.Double(0, 0, w, h, 12, 12));
                g2d.setColor(UITheme.BORDER_SUBTLE);
                g2d.setStroke(new BasicStroke(1.0f));
                g2d.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 12, 12));
            }

            g2d.dispose();
            super.paintComponent(g);
        }
    }
}
