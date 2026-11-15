package io.itch.aktomik.console2Discord;

import org.bukkit.configuration.file.FileConfiguration;

public final class ConfigSettings {
    private ConfigSettings() {}

    public static String webhookUrl = "MISSING";

    public static String messageWelcomeText = ":warning: config not correctly loaded, delete console2discord folder and restart";
    public static String messageShutdownText = ":warning: config not correctly loaded, delete console2discord folder and restart";

    public static boolean contentGroupBySecond = true;
    public static int contentCharLimit = 1900;

    public static boolean contentAnsiEnable = true;
    public static boolean contentAnsiColorWarning = true;
    public static boolean contentAnsiColorError = true;

    public static boolean contentStackTraceAllow = true;
    public static int contentStackTraceCharLimit = 1200;

    public static int queueWaitCapacity = 100;
    public static int queuePostTryCount = 5;

    public static int queueWaitTimeResumeMs = 100;
    public static int queueWaitTimeBetweenMinMs = 100;
    public static int queueWaitTimeBetweenMaxMs = 400;
    public static int queueWaitTimeRetryFirstMs = 500;
    public static int queueWaitTimeRetryIncrementMs = 1000;

    public static void load(FileConfiguration c) {
        webhookUrl = c.getString("webhook-url", webhookUrl);

        messageWelcomeText = c.getString("message.welcome-text", messageWelcomeText);
        messageShutdownText = c.getString("message.shutdown-text", messageShutdownText);

        contentAnsiEnable = c.getBoolean("content.ansi.enable", contentAnsiEnable);
        contentAnsiColorWarning = c.getBoolean("content.ansi.color-warning", contentAnsiColorWarning);
        contentAnsiColorError = c.getBoolean("content.ansi.color-error", contentAnsiColorError);

        contentGroupBySecond = c.getBoolean("message.group-by-second", contentGroupBySecond);
        contentCharLimit = c.getInt("message.body-char-limit", contentCharLimit);

        contentStackTraceAllow = c.getBoolean("content.stack-trace.allow", contentStackTraceAllow);
        contentStackTraceCharLimit = c.getInt("content.stack-trace.char-limit", contentStackTraceCharLimit);

        queueWaitCapacity = c.getInt("queue.wait-capacity", queueWaitCapacity);
        queuePostTryCount = c.getInt("queue.request-try-count", queuePostTryCount);

        queueWaitTimeResumeMs = c.getInt("queue.wait-time.breath-start-ms", queueWaitTimeResumeMs);
        queueWaitTimeBetweenMinMs = c.getInt("queue.wait-time.between-request-min-ms", queueWaitTimeBetweenMinMs);
        queueWaitTimeBetweenMaxMs = c.getInt("queue.wait-time.between-request-max-ms", queueWaitTimeBetweenMaxMs);
        queueWaitTimeRetryFirstMs = c.getInt("queue.wait-time.retry-delay-first-ms", queueWaitTimeRetryFirstMs);
        queueWaitTimeRetryIncrementMs = c.getInt("queue.wait-time.retry-delay-increment-ms", queueWaitTimeRetryIncrementMs);
    }
}