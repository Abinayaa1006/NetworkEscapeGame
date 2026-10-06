package client;

import server.Server;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestGameFlow {

    public static void main(String[] args) throws Exception {
        System.out.println(">>> Starting Server in background thread...");
        Thread serverThread = new Thread(() -> Server.main(new String[0]));
        serverThread.setDaemon(true);
        serverThread.start();

        Thread.sleep(1000);

        CountDownLatch latch = new CountDownLatch(2);

        System.out.println(">>> Connecting Client 1 (Alice)...");
        NetworkClient c1 = new NetworkClient();
        c1.addListener(new NetworkClient.GameEventListener() {
            @Override
            public void onConnected() {
                System.out.println("[TEST-C1] Connected!");
            }
            @Override
            public void onGameStarted() {
                System.out.println("[TEST-C1] Game Started!");
            }
            @Override
            public void onIndividualPuzzleReceived(int id, String q, List<String> opts) {
                System.out.println("[TEST-C1] Received Puzzle #" + id + ": " + q.substring(0, Math.min(30, q.length())));
                String ans = (id == 1 ? "A" : (id == 5 ? "B" : "A"));
                c1.sendAnswer(ans);
            }
            @Override
            public void onPuzzleResult(boolean correct, int rem, String fb) {
                System.out.println("[TEST-C1] Puzzle Result: " + correct + " (" + fb + ")");
            }
            @Override
            public void onClueDiscovered(String clue) {
                System.out.println("[TEST-C1] Discovered clue: " + clue);
            }
            @Override
            public void onTeamChallengeStarted(int room, String title, String fmt, String ex) {
                System.out.println("[TEST-C1] Team challenge started for room " + room + ": " + fmt);
                new Thread(() -> {
                    try { Thread.sleep(300); } catch (Exception ignored) {}
                    if (room == 1) {
                        c1.sendTeamAnswer("CONNECT 192.168.1.10 80");
                    } else if (room == 2) {
                        c1.sendTeamAnswer("ROUTE A-C-E HOPS 2");
                    }
                }).start();
            }
            @Override
            public void onFinalRoomChallengeStarted(String q, List<String> opts, int rem) {
                System.out.println("[TEST-C1] Final Room Challenge received! Answering FINAL A...");
                new Thread(() -> {
                    try { Thread.sleep(300); } catch (Exception ignored) {}
                    c1.sendFinalAnswer("A");
                }).start();
            }
            @Override
            public void onGameEnded(boolean escaped, Map<String, Integer> scores) {
                System.out.println("[TEST-C1] GAME COMPLETED! Escaped=" + escaped + " Scores=" + scores);
                latch.countDown();
            }
        });
        c1.connect("localhost", 5000, "Alice");

        Thread.sleep(500);

        System.out.println(">>> Connecting Client 2 (Bob)...");
        NetworkClient c2 = new NetworkClient();
        c2.addListener(new NetworkClient.GameEventListener() {
            @Override
            public void onConnected() {
                System.out.println("[TEST-C2] Connected!");
            }
            @Override
            public void onIndividualPuzzleReceived(int id, String q, List<String> opts) {
                System.out.println("[TEST-C2] Received Puzzle #" + id + ": " + q.substring(0, Math.min(30, q.length())));
                String ans = (id == 2 ? "C" : (id == 6 ? "B" : "A"));
                c2.sendAnswer(ans);
            }
            @Override
            public void onGameEnded(boolean escaped, Map<String, Integer> scores) {
                System.out.println("[TEST-C2] GAME COMPLETED! Escaped=" + escaped + " Scores=" + scores);
                latch.countDown();
            }
        });
        c2.connect("localhost", 5000, "Bob");

        Thread.sleep(1000);

        System.out.println(">>> Alice starting game...");
        c1.startGame();

        boolean finished = latch.await(10, TimeUnit.SECONDS);
        System.out.println(">>> Test Flow Finished. Success = " + finished);

        c1.disconnect();
        c2.disconnect();
        System.exit(finished ? 0 : 1);
    }
}
