package client.ui;

import client.NetworkClient;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.text.*;

public class ChatPanel extends JPanel {

    private NetworkClient networkClient;
    private JTextPane chatArea;
    private JTextPane terminalArea;
    private JTabbedPane tabbedPane;
    private ModernComponents.ModernTextField chatInput;
    private ModernComponents.ModernButton sendBtn;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public ChatPanel(NetworkClient networkClient) {
        this.networkClient = networkClient;
        setLayout(new BorderLayout(0, 10));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 16));
        setPreferredSize(new Dimension(340, 600));

        initComponents();
    }

    private void initComponents() {
        ModernComponents.CardPanel mainCard = new ModernComponents.CardPanel(16, UITheme.BG_CARD, UITheme.BORDER_SUBTLE);
        mainCard.setLayout(new BorderLayout(0, 10));
        mainCard.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // Tabbed Panel: TEAM COMM / SYSTEM TERMINAL
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_SMALL_BOLD);
        tabbedPane.setBackground(UITheme.BG_INPUT);
        tabbedPane.setForeground(UITheme.TEXT_SECONDARY);

        // 1. Team Chat Area
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setBackground(UITheme.BG_INPUT);
        chatArea.setFont(UITheme.FONT_BODY);
        chatArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(BorderFactory.createEmptyBorder());
        chatScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // 2. Terminal Raw Area
        terminalArea = new JTextPane();
        terminalArea.setEditable(false);
        terminalArea.setBackground(new Color(6, 9, 15));
        terminalArea.setFont(UITheme.FONT_MONO);
        terminalArea.setForeground(new Color(34, 197, 94)); // Matrix green text
        terminalArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane termScroll = new JScrollPane(terminalArea);
        termScroll.setBorder(BorderFactory.createEmptyBorder());
        termScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        tabbedPane.addTab("TEAM CHAT", chatScroll);
        tabbedPane.addTab("SYSTEM LOG", termScroll);

        // Quick Macro Chips
        JPanel quickChips = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        quickChips.setOpaque(false);

        String[] macros = {"Got my clue!", "What's the IP?", "Check chat", "Ready!"};
        for (String m : macros) {
            JButton btn = new JButton(m);
            btn.setFont(UITheme.FONT_SMALL);
            btn.setForeground(UITheme.TEXT_CYAN);
            btn.setBackground(UITheme.BG_INPUT);
            btn.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_SUBTLE, 1));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                chatInput.setText(m);
                sendMessage();
            });
            quickChips.add(btn);
        }

        // Bottom Input Row
        JPanel inputPanel = new JPanel(new BorderLayout(6, 0));
        inputPanel.setOpaque(false);

        chatInput = new ModernComponents.ModernTextField("Message operatives...", 10);
        chatInput.addActionListener(e -> sendMessage());

        sendBtn = new ModernComponents.ModernButton("SEND", ModernComponents.ButtonStyle.PRIMARY);
        sendBtn.setPreferredSize(new Dimension(85, 38));
        sendBtn.setMargin(new Insets(4, 8, 4, 8));
        sendBtn.setFont(UITheme.FONT_SMALL_BOLD);
        sendBtn.addActionListener(e -> sendMessage());

        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);

        JPanel bottomWrapper = new JPanel(new BorderLayout(0, 6));
        bottomWrapper.setOpaque(false);
        bottomWrapper.add(quickChips, BorderLayout.NORTH);
        bottomWrapper.add(inputPanel, BorderLayout.CENTER);

        mainCard.add(tabbedPane, BorderLayout.CENTER);
        mainCard.add(bottomWrapper, BorderLayout.SOUTH);

        add(mainCard, BorderLayout.CENTER);
    }

    private void sendMessage() {
        String msg = chatInput.getText().trim();
        if (msg.isEmpty()) return;
        networkClient.sendChatMessage(msg);
        chatInput.setText("");
    }

    public void appendChatMessage(String sender, String message) {
        StyledDocument doc = chatArea.getStyledDocument();
        boolean isMe = sender.equalsIgnoreCase(networkClient.getPlayerName());

        SimpleAttributeSet timeStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(timeStyle, UITheme.TEXT_MUTED);
        StyleConstants.setFontSize(timeStyle, 11);

        SimpleAttributeSet nameStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(nameStyle, isMe ? UITheme.ACCENT_CYAN : UITheme.ACCENT_AMBER);
        StyleConstants.setBold(nameStyle, true);

        SimpleAttributeSet msgStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(msgStyle, UITheme.TEXT_PRIMARY);

        try {
            String time = "[" + timeFormat.format(new Date()) + "] ";
            doc.insertString(doc.getLength(), time, timeStyle);
            doc.insertString(doc.getLength(), sender + ": ", nameStyle);
            doc.insertString(doc.getLength(), message + "\n", msgStyle);
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException ignored) {}
    }

    public void appendSystemLog(String line, String category) {
        StyledDocument doc = terminalArea.getStyledDocument();
        SimpleAttributeSet style = new SimpleAttributeSet();

        if ("ERROR".equals(category)) {
            StyleConstants.setForeground(style, UITheme.ACCENT_RED);
        } else if ("GAME".equals(category) || "ROOM".equals(category)) {
            StyleConstants.setForeground(style, UITheme.ACCENT_CYAN);
        } else if ("CLUE".equals(category) || "PUZZLE".equals(category)) {
            StyleConstants.setForeground(style, UITheme.ACCENT_GREEN);
        } else if ("TEAM".equals(category) || "FINAL".equals(category)) {
            StyleConstants.setForeground(style, UITheme.ACCENT_PURPLE);
        } else {
            StyleConstants.setForeground(style, new Color(156, 163, 175));
        }

        try {
            doc.insertString(doc.getLength(), line + "\n", style);
            terminalArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException ignored) {}
    }
}
