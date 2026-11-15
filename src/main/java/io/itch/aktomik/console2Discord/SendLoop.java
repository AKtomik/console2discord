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

    private Runnable lastWill = null;

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

    public void disable(Runnable inputWill) {
        lastWill = inputWill;
        disable();
    }

    private void loop() {

        ArrayList<LogUnit> sendingLogs;
        int sendingLetters;
        LogUnit nextLog = null;

        try {
            while (running || !queue.isEmpty() || nextLog != null) {
                if (nextLog == null)
                {
                    nextLog = queue.poll(5, TimeUnit.SECONDS);
                    //noinspection BusyWait
                    Thread.sleep(ConfigSettings.queueWaitTimeResumeMs);
                    if (nextLog == null) continue;
                }

                // if one message is already too long
                if (nextLog.length() > ConfigSettings.contentCharLimit)
                    nextLog = new LogUnit(nextLog.text().substring(0, ConfigSettings.contentCharLimit - 3) + "...", nextLog.time(), nextLog.level());

                // first setup
                int firstSecond = nextLog.time().getSecond();// good simplification
                sendingLogs = new ArrayList<>(List.of(nextLog));
                sendingLetters = nextLog.length();

                // stack log unit in one message
                while ((nextLog = queue.poll()) != null) {
                    if (
                        sendingLetters + nextLog.length() > ConfigSettings.contentCharLimit
                        || (ConfigSettings.contentGroupBySecond && nextLog.time().getSecond() != firstSecond)
                    ) break;
                    sendingLogs.add(nextLog);
                    sendingLetters += nextLog.length();
                    nextLog = null;// necessary
                }

                // message send
                String message = ConfigSettings.contentAnsiEnable
                        ? AnsiToolbox.makeAnsiBlock(sendingLogs)
                        : AnsiToolbox.makeColorless(sendingLogs);
                int sendingSize = sendingLogs.size();
                sendingLogs.clear();

                long timeBefore = System.currentTimeMillis();
                for (int i = 0; i < ConfigSettings.queuePostTryCount; i++)
                {
                    int status = mailer.sendMessage(message);// waiting response

                    // fine
                    if (200 <= status && status < 300)
                    {
                        SessionStats.sentSuccessCount += sendingSize;
                        break;
                    }

                    // something wrong
                    boolean retryable = status == 429 || status < 0 || (status >= 500 && status <= 504);
                    if (!retryable) break;

                    if (i + 1 == ConfigSettings.queuePostTryCount)
                    {
                        SessionStats.sentFailureCount += sendingSize;
                        break;
                    } else {
                        //noinspection BusyWait
                        Thread.sleep(ConfigSettings.queueWaitTimeRetryFirstMs + (long) ConfigSettings.queueWaitTimeRetryIncrementMs * i);
                    }
                }
                long operationDuration = System.currentTimeMillis() - timeBefore;

                //noinspection BusyWait
                Thread.sleep(Math.max(ConfigSettings.queueWaitTimeBetweenMinMs, ConfigSettings.queueWaitTimeBetweenMaxMs - operationDuration));
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("InterruptedException in send loop", e);
        }

        if (lastWill != null) lastWill.run();
    }
}
