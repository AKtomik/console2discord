package io.itch.aktomik.console2Discord;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class SendLoop {

    private volatile boolean running = false;
    private final Thread worker = new Thread(this::loop, "Console2Discord");

    BlockingQueue<LogUnit> queue;
    WebhookMailer mailer;

    SendLoop(BlockingQueue<LogUnit> providedQueue, WebhookMailer providedMailer) {
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

        ArrayList<LogUnit> sendingLines = new ArrayList<>();
        int sendingLetters;

        try {
            while (running || !queue.isEmpty()) {
                LogUnit firstLog = queue.poll(5, TimeUnit.SECONDS);
                if (firstLog == null) continue;

                sendingLines.clear();
                sendingLines.add(firstLog);
                sendingLetters = firstLog.length();

                LogUnit nextLine;
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
