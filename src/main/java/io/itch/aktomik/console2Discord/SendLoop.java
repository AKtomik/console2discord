package io.itch.aktomik.console2Discord;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class SendLoop {

    private volatile boolean running = false;
    private final Thread worker = new Thread(this::loop, "Console2Discord");

    BlockingQueue<String> queue;
    WebhookMailer mailer;

    SendLoop(BlockingQueue<String> providedQueue, WebhookMailer providedMailer) {
        queue = providedQueue;
        mailer = providedMailer;
    }

    public void enable() {
        if (running) return;
        running = true;

        worker.setDaemon(true);
        worker.start();
    }

    public void disable() {
        if (!running) return;
        running = false;

        try {
            worker.join(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void loop() {

        ArrayList<String> sendingLines = new ArrayList<>();
        int sendingLetters;

        try {
            while (running || !queue.isEmpty()) {
                String firstLine = queue.poll(5, TimeUnit.SECONDS);
                if (firstLine == null) continue;

                sendingLines.clear();
                sendingLines.add(firstLine);
                sendingLetters = firstLine.length();

                String nextLine;
                while (sendingLetters < 1800 && (nextLine = queue.poll()) != null) {
                    sendingLines.add(nextLine);
                    sendingLetters += nextLine.length();
                }

                String message = Settings.enableAnsi
                        ? AnsiToolbox.makeAnsiBlock(sendingLines)
                        : AnsiToolbox.makeColorless(sendingLines);

                mailer.sendMessage(message);// waiting response
                //noinspection BusyWait
                Thread.sleep(500);
            }
        } catch (InterruptedException ignored) { }
    }
}
