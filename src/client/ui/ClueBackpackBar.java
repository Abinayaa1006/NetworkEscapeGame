package client.ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class ClueBackpackBar extends JPanel {

    private JPanel chipsPanel;
    private JLabel countLabel;
    private List<String> currentClues = new ArrayList<>();

    public ClueBackpackBar() {
        setLayout(new BorderLayout(12, 0));
        setOpaque(false);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        initComponents();
    }

    private void initComponents() {
        // Left Badge
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel icon = new JLabel("[VAULT]");
        icon.setFont(UITheme.FONT_SMALL_BOLD);
        icon.setForeground(UITheme.ACCENT_PURPLE);

        countLabel = new JLabel("0 UNLOCKED");
        countLabel.setFont(UITheme.FONT_SMALL);
        countLabel.setForeground(UITheme.TEXT_MUTED);

        left.add(icon);
        left.add(countLabel);

        // Center / Right: Dynamic Clue Chips
        chipsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chipsPanel.setOpaque(false);

        JLabel emptyPlaceholder = new JLabel("Solve room challenges to decode network artifacts and tokens.");
        emptyPlaceholder.setFont(UITheme.FONT_SMALL);
        emptyPlaceholder.setForeground(UITheme.TEXT_MUTED);
        chipsPanel.add(emptyPlaceholder);

        add(left, BorderLayout.WEST);
        add(chipsPanel, BorderLayout.CENTER);
    }

    public void updateClues(List<String> clues) {
        this.currentClues = new ArrayList<>(clues);
        chipsPanel.removeAll();

        countLabel.setText(clues.size() + " UNLOCKED");
        countLabel.setForeground(clues.isEmpty() ? UITheme.TEXT_MUTED : UITheme.ACCENT_GREEN);

        if (clues.isEmpty()) {
            JLabel emptyPlaceholder = new JLabel("Solve room challenges to decode network artifacts and tokens.");
            emptyPlaceholder.setFont(UITheme.FONT_SMALL);
            emptyPlaceholder.setForeground(UITheme.TEXT_MUTED);
            chipsPanel.add(emptyPlaceholder);
        } else {
            for (String clue : clues) {
                // Card Chip with icon
                String tag = "[KEY]";
                if (clue.contains("IP") || clue.contains("DNS")) tag = "[IP/DNS]";
                else if (clue.contains("Port")) tag = "[PORT]";
                else if (clue.contains("Username") || clue.contains("Password")) tag = "[AUTH]";
                else if (clue.contains("ROUTE") || clue.contains("HOPS") || clue.contains("TTL")) tag = "[ROUTE]";

                ModernComponents.CardPanel chip = new ModernComponents.CardPanel(8, UITheme.BG_CARD_HOVER, UITheme.ACCENT_PURPLE);
                chip.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 3));

                JLabel chipLbl = new JLabel(tag + " " + clue);
                chipLbl.setFont(UITheme.FONT_SMALL_BOLD);
                chipLbl.setForeground(UITheme.TEXT_PRIMARY);

                chip.add(chipLbl);
                chipsPanel.add(chip);
            }
        }

        chipsPanel.revalidate();
        chipsPanel.repaint();
    }
}
