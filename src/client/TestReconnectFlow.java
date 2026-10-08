package client;

import server.Server;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestReconnectFlow {

    public static void main(String[] args) throws Exception {
        System.out.println(">>> Starting Server in background thread...");
        Thread serverThread = new Thread(() -> Server.main(new String[0]));
        serverThread.setDaemon(true);
        serverThread.start();

        Thread.sleep(800);

        // --- ROUND 1 ---
        CountDownLatch latch1 = new CountDownLatch(2);

        NetworkClient c1 = new NetworkClient();
        c1.addListener(new NetworkClient.GameEventListener() {
            @Override public void onIndividualPuzzleReceived(int id, String q, List<String> opts) {
                c1.sendAnswer(id == 1 ? "A" : (id == 5 ? "B" : "A"));
            }
            @Override public void onTeamChallengeStarted(int r, String t, String f, String ex) {
                new Thread(() -> {
                    try { Thread.sleep(200); } catch (Exception ignored) {}
                    if (r == 1) c1.sendTeamAnswer("CONNECT 192.168.1.10 80");
                    else if (r == 2) c1.sendTeamAnswer("ROUTE A-C-E HOPS 2");
                }).start();
            }
            @Override public void onFinalRoomChallengeStarted(String q, List<String> opts, int rem) {
                new Thread(() -> {
                    try { Thread.sleep(200); } catch (Exception ignored) {}
                    c1.sendFinalAnswer("A");
                }).start();
            }
            @Override public void onGameEnded(boolean escaped, Map<String, Integer> scores) {
                System.out.println("[ROUND 1] C1 Game Ended! Escaped=" + escaped);
                latch1.countDown();
            }
        });

        NetworkClient c2 = new NetworkClient();
        c2.addListener(new NetworkClient.GameEventListener() {
            @Override public void onIndividualPuzzleReceived(int id, String q, List<String> opts) {
                c2.sendAnswer(id == 2 ? "C" : (id == 6 ? "B" : "A"));
            }
            @Override public void onGameEnded(boolean escaped, Map<String, Integer> scores) {
                System.out.println("[ROUND 1] C2 Game Ended! Escaped=" + escaped);
                latch1.countDown();
            }
        });

        c1.connect("localhost", 5000, "Agent1");
        Thread.sleep(300);
        c2.connect("localhost", 5000, "Agent2");
        Thread.sleep(600);

        System.out.println(">>> Round 1: Starting Game...");
        c1.startGame();

        boolean r1Finished = latch1.await(10, TimeUnit.SECONDS);
        System.out.println(">>> Round 1 Finished: " + r1Finished);
        if (!r1Finished) System.exit(1);

        // --- RECONNECT ---
        System.out.println(">>> Now testing reconnect for both clients...");
        CountDownLatch reconnectLatch = new CountDownLatch(2);

        c1.addListener(new NetworkClient.GameEventListener() {
            @Override public void onConnected() {
                System.out.println("[RECONNECT] Agent1 successfully reconnected to server!");
                reconnectLatch.countDown();
            }
        });
        c2.addListener(new NetworkClient.GameEventListener() {
            @Override public void onConnected() {
                System.out.println("[RECONNECT] Agent2 successfully reconnected to server!");
                reconnectLatch.countDown();
            }
        });

        c1.reconnect();
        Thread.sleep(400);
        c2.reconnect();

        boolean reconnected = reconnectLatch.await(5, TimeUnit.SECONDS);
        System.out.println(">>> Reconnect successful for all agents: " + reconnected);

        c1.disconnect();
        c2.disconnect();

        System.exit(reconnected ? 0 : 1);
    }
}
