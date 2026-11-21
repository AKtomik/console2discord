package io.itch.aktomik.console2Discord.send;

import io.itch.aktomik.console2Discord.ConfigSettings;
import io.itch.aktomik.console2Discord.listen.LogUnit;
import io.itch.aktomik.console2Discord.SessionStats;
import io.itch.aktomik.console2Discord.listen.LogQueue;
import io.itch.aktomik.console2Discord.send.webhook.WebhookMailer;
import io.itch.aktomik.console2Discord.send.webhook.WebhookStatus;
import io.itch.aktomik.console2Discord.utils.TruncationToolbox;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SendLoop {

    private volatile boolean running = false;
    private final Thread worker = new Thread(this::loop, "Console2Discord");

    LogQueue queue;
    WebhookMailer mailer;

    public SendLoop(LogQueue buffer, WebhookMailer providedMailer) {
        queue = buffer;
        mailer = providedMailer;
    }

    public void enable() {
        if (running) return;
        running = true;

        worker.setDaemon(true);
        worker.start();
    }

    public void disableStart(int joinDelay) {
        if (!running) return;
        running = false;

        try {
            worker.join(joinDelay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void disableNow() {
        if (!running) return;
        running = false;
        worker.interrupt();
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
                if (nextLog.length() > ConfigSettings.contentBodyCharLimit)
                {
                    String logText = TruncationToolbox.truncateLine(nextLog.text(), ConfigSettings.contentBodyCharLimit, ConfigSettings.messageTruncationText);
                    nextLog = new LogUnit(logText, nextLog.time(), nextLog.level());
                }

                // first setup
                int firstSecond = nextLog.time().getSecond();// good simplification
                sendingLogs = new ArrayList<>(List.of(nextLog));
                sendingLetters = nextLog.length();

                // stack log unit in one message
                while ((nextLog = queue.poll()) != null) {
                    if (
                        sendingLetters + nextLog.length() > ConfigSettings.contentBodyCharLimit
                        || (ConfigSettings.queueGroupBySecond && nextLog.time().getSecond() != firstSecond)
                    ) break;
                    sendingLogs.add(nextLog);
                    sendingLetters += nextLog.length();
                    nextLog = null;// necessary
                }

                // message send
                String message = switch (ConfigSettings.contentFormat)
                {
                    case CODEBLOCK -> AnsiToolbox.makeCodeBlock(sendingLogs);
                    case ANSIBLOCK -> AnsiToolbox.makeAnsiBlock(sendingLogs);
                    case INLINE -> AnsiToolbox.makeCodeLine(sendingLogs);
                    case TEXT -> AnsiToolbox.makeText(sendingLogs);
                };
                int sendingSize = sendingLogs.size();
                sendingLogs.clear();

                long timeBefore = System.currentTimeMillis();
                for (int i = 0; i < ConfigSettings.queuePostTryCount; i++)
                {
                    WebhookStatus status = mailer.sendMessage(message);// waiting response

                    // fine
                    if (status.OK())
                    {
                        SessionStats.sentSuccessCount += sendingSize;
                        break;
                    }

                    // something wrong
                    boolean retryable = List.of(WebhookStatus.RATE_LIMIT, WebhookStatus.OFFLINE, WebhookStatus.GENERIC_SERVER_ERROR).contains(status);
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
    }
}
