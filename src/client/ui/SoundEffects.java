package client.ui;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class SoundEffects {

    private static boolean soundEnabled = true;

    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
    }

    public static boolean isSoundEnabled() {
        return soundEnabled;
    }

    // Play click sound
    public static void playClick() {
        if (!soundEnabled) return;
        new Thread(() -> playTone(880, 40, 0.25)).start();
    }

    // Play correct answer chime
    public static void playSuccess() {
        if (!soundEnabled) return;
        new Thread(() -> {
            playTone(523, 80, 0.4); // C5
            try { Thread.sleep(70); } catch (Exception ignored) {}
            playTone(659, 80, 0.4); // E5
            try { Thread.sleep(70); } catch (Exception ignored) {}
            playTone(784, 140, 0.5); // G5
        }).start();
    }

    // Play wrong answer buzzer
    public static void playError() {
        if (!soundEnabled) return;
        new Thread(() -> {
            playTone(220, 120, 0.5); // Low A3
            try { Thread.sleep(60); } catch (Exception ignored) {}
            playTone(196, 180, 0.5); // Low G3
        }).start();
    }

    // Play room clear fanfare
    public static void playRoomCleared() {
        if (!soundEnabled) return;
        new Thread(() -> {
            int[] notes = {440, 554, 659, 880};
            for (int note : notes) {
                playTone(note, 90, 0.45);
                try { Thread.sleep(80); } catch (Exception ignored) {}
            }
        }).start();
    }

    // Play game escape victory
    public static void playVictory() {
        if (!soundEnabled) return;
        new Thread(() -> {
            int[] notes = {523, 659, 784, 1046};
            for (int note : notes) {
                playTone(note, 120, 0.5);
                try { Thread.sleep(100); } catch (Exception ignored) {}
            }
            playTone(1046, 300, 0.6);
        }).start();
    }

    private static void playTone(int frequency, int durationMs, double volume) {
        try {
            float sampleRate = 44100f;
            byte[] buf = new byte[(int) (sampleRate * (durationMs / 1000.0))];
            for (int i = 0; i < buf.length; i++) {
                double angle = 2.0 * Math.PI * i / (sampleRate / frequency);
                // Apply soft envelope to prevent clicking
                double envelope = 1.0;
                if (i < 200) envelope = i / 200.0;
                else if (i > buf.length - 300) envelope = (buf.length - i) / 300.0;
                buf[i] = (byte) (Math.sin(angle) * 127.0 * volume * envelope);
            }

            AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, true);
            SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
            sdl.open(af);
            sdl.start();
            sdl.write(buf, 0, buf.length);
            sdl.drain();
            sdl.close();
        } catch (Exception ignored) {
            // Audio line unavailable in environment, fails gracefully
        }
    }
}
