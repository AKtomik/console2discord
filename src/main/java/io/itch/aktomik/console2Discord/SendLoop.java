package io.itch.aktomik.console2Discord;

import java.util.ArrayList;
import java.util.List;
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

        ArrayList<LogUnit> sendingLogs;
        int sendingLetters;
        LogUnit nextLog = null;

        try {
            while (running || !queue.isEmpty()) {
                if (nextLog == null)
                {
                    nextLog = queue.poll(5, TimeUnit.SECONDS);
                    if (nextLog == null) continue;
                }

                sendingLogs = new ArrayList<>(List.of(nextLog));
                sendingLetters = nextLog.length();

                while ((nextLog = queue.poll()) != null) {
                    if (sendingLetters + nextLog.length() > 2000) break;
                    sendingLogs.add(nextLog);
                    sendingLetters += nextLog.length();
                    nextLog = null;
                }

                String message = Settings.enableAnsi
                        ? AnsiToolbox.makeAnsiBlock(sendingLogs)
                        : AnsiToolbox.makeColorless(sendingLogs);

                mailer.sendMessage(message);// waiting response
                sendingLogs.clear();

                //noinspection BusyWait
                Thread.sleep(500);
            }
        } catch (InterruptedException ignored) { }
    }
}
