package io.itch.aktomik.console2Discord;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class SendLoop {

    private volatile boolean running = true;
    private final Thread worker = new Thread(this::loop, "Console2Discord");

    BlockingQueue<String> queue;
    WebhookMailer mailer;

    SendLoop(BlockingQueue<String> providedQueue, WebhookMailer providedMailer) {
        queue = providedQueue;
        mailer = providedMailer;
    }

    public void enable() {
        worker.setDaemon(true);
        worker.start();
    }

    public void disable() {
        running = false;
        try {
            worker.join(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void loop() {
        StringBuilder sb = new StringBuilder();
        try {
            while (running || !queue.isEmpty()) {
                String first = queue.poll(2, TimeUnit.SECONDS);
                if (first == null) continue;
                sb.setLength(0);
                sb.append(first);
                String next;
                while (sb.length() < 1800 && (next = queue.poll()) != null) {
                    sb.append('\n').append(next);
                }
                mailer.sendMessage(sb.toString());// pause here
                Thread.sleep(1000);
            }
        } catch (InterruptedException ignored) { }
    }
}
