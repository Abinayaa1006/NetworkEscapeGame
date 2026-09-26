package client;

import client.ui.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class GameClientGUI extends JFrame implements NetworkClient.GameEventListener {

    public static final String CARD_LOGIN = "LOGIN";
    public static final String CARD_LOBBY = "LOBBY";
    public static final String CARD_GAME = "GAME";

    public static final String SUB_PUZZLE = "SUB_PUZZLE";
    public static final String SUB_TEAM = "SUB_TEAM";
    public static final String SUB_FINAL = "SUB_FINAL";

    private NetworkClient networkClient;
    private JPanel rootPanel;
    private CardLayout rootCardLayout;

    // View Panels
    private LoginPanel loginPanel;
    private LobbyPanel lobbyPanel;
    private GameHUDPanel hudPanel;
    private PuzzlePanel puzzlePanel;
    private TeamChallengePanel teamPanel;
    private FinalRoomPanel finalPanel;
    private ChatPanel chatPanel;

    private JPanel gameCenterPanel;
    private CardLayout gameCenterCardLayout;

    public GameClientGUI() {
        super("Network Escape Room — Multiplayer Cyber Terminal");
        this.networkClient = new NetworkClient();
        this.networkClient.addListener(this);

        initWindow();
        initViews();
        showCard(CARD_LOGIN);
    }

    private void initWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_DARK);

        try {
            // Set modern look if available
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    private void initViews() {
        rootCardLayout = new CardLayout();
        rootPanel = new JPanel(rootCardLayout);
        rootPanel.setBackground(UITheme.BG_DARK);

        // 1. Login View
        loginPanel = new LoginPanel(networkClient);

        // 2. Lobby View
        lobbyPanel = new LobbyPanel(networkClient);

        // 3. Main In-Game Dashboard
        JPanel gameContainer = new JPanel(new BorderLayout());
        gameContainer.setBackground(UITheme.BG_DARK);

        hudPanel = new GameHUDPanel();
        chatPanel = new ChatPanel(networkClient);

        // Sub-Cards for Center Gameplay Area
        gameCenterCardLayout = new CardLayout();
        gameCenterPanel = new JPanel(gameCenterCardLayout);
        gameCenterPanel.setOpaque(false);

        puzzlePanel = new PuzzlePanel(networkClient);
        teamPanel = new TeamChallengePanel(networkClient);
        finalPanel = new FinalRoomPanel(networkClient);

        gameCenterPanel.add(puzzlePanel, SUB_PUZZLE);
        gameCenterPanel.add(teamPanel, SUB_TEAM);
        gameCenterPanel.add(finalPanel, SUB_FINAL);

        gameContainer.add(hudPanel, BorderLayout.NORTH);
        gameContainer.add(gameCenterPanel, BorderLayout.CENTER);
        gameContainer.add(chatPanel, BorderLayout.EAST);

        // Assemble Cards
        rootPanel.add(loginPanel, CARD_LOGIN);
        rootPanel.add(lobbyPanel, CARD_LOBBY);
        rootPanel.add(gameContainer, CARD_GAME);

        setContentPane(rootPanel);
    }

    public void showCard(String cardName) {
        rootCardLayout.show(rootPanel, cardName);
    }

    public void showGameSubCard(String subCardName) {
        gameCenterCardLayout.show(gameCenterPanel, subCardName);
    }

    // ==========================================
    // NETWORK EVENT LISTENER IMPLEMENTATION
    // ==========================================

    @Override
    public void onConnected() {
        hudPanel.setPlayerName(networkClient.getPlayerName());
        hudPanel.setScore(0);
        hudPanel.setAttempts(2, 2);
        showCard(CARD_LOBBY);
        SoundEffects.playClick();
    }

    @Override
    public void onDisconnected(String reason) {
        loginPanel.reset();
        loginPanel.setStatus(reason, UITheme.ACCENT_AMBER);
        showCard(CARD_LOGIN);
    }

    @Override
    public void onError(String errorMessage) {
        loginPanel.setStatus(errorMessage, UITheme.ACCENT_RED);
        SoundEffects.playError();
    }

    @Override
    public void onLobbyUpdate(List<String> playerList, int count, int max) {
        lobbyPanel.updateLobbyState(count, max);
    }

    @Override
    public void onGameStarted() {
        showCard(CARD_GAME);
        SoundEffects.playRoomCleared();
    }

    @Override
    public void onRoomChanged(int roomNumber, String roomTitle) {
        hudPanel.setRoom(roomNumber, roomTitle);
        puzzlePanel.setRoom(roomNumber);
    }

    @Override
    public void onIndividualPuzzleReceived(int puzzleId, String question, List<String> options) {
        hudPanel.setAttempts(2, 2);
        puzzlePanel.displayPuzzle(puzzleId, question, options);
        showGameSubCard(SUB_PUZZLE);
    }

    @Override
    public void onPuzzleResult(boolean correct, int remainingAttempts, String feedback) {
        hudPanel.setAttempts(remainingAttempts, 2);
        puzzlePanel.handlePuzzleResult(correct, remainingAttempts, feedback);
    }

    @Override
    public void onScoreUpdated(int newScore) {
        hudPanel.setScore(newScore);
    }

    @Override
    public void onClueDiscovered(String clue) {
        teamPanel.updateCluesList(networkClient.getDiscoveredClues());
    }

    @Override
    public void onTeamChallengeStarted(int room, String title, String format, String example) {
        teamPanel.setupTeamChallenge(room, title, format, example, networkClient.getDiscoveredClues());
        showGameSubCard(SUB_TEAM);
    }

    @Override
    public void onTeamChallengeResult(boolean correct, String message) {
        teamPanel.handleTeamResult(correct, message);
    }

    @Override
    public void onFinalRoomChallengeStarted(String question, List<String> options, int remainingAttempts) {
        hudPanel.setRoom(3, "Final Room: Sequence Escape");
        hudPanel.setAttempts(remainingAttempts, 3);
        finalPanel.setupFinalChallenge(question, options, remainingAttempts);
        showGameSubCard(SUB_FINAL);
    }

    @Override
    public void onFinalRoomResult(boolean correct, int attemptUsed, int remainingAttempts, String message) {
        hudPanel.setAttempts(remainingAttempts, 3);
        finalPanel.handleFinalResult(correct, attemptUsed, remainingAttempts, message);
    }

    @Override
    public void onGameEnded(boolean escaped, Map<String, Integer> finalScores) {
        VictoryDialog dialog = new VictoryDialog(this, escaped, finalScores, new VictoryDialog.DebriefActionListener() {
            @Override
            public void onPlayAgain() {
                networkClient.disconnect();
                loginPanel.reset();
                showCard(CARD_LOGIN);
            }

            @Override
            public void onExit() {
                networkClient.disconnect();
                System.exit(0);
            }
        });
        dialog.setVisible(true);
    }

    @Override
    public void onChatMessageReceived(String sender, String message) {
        chatPanel.appendChatMessage(sender, message);
    }

    @Override
    public void onSystemLogReceived(String log, String category) {
        chatPanel.appendSystemLog(log, category);
    }

    // ==========================================
    // MAIN ENTRY POINT
    // ==========================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameClientGUI gui = new GameClientGUI();
            gui.setVisible(true);
        });
    }
}
