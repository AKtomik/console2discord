package io.itch.aktomik.console2Discord;

import io.itch.aktomik.console2Discord.send.SendFormat;
import org.bukkit.configuration.file.FileConfiguration;

public final class ConfigSettings {
    private ConfigSettings() {}

    public static String webhookUrl = "MISSING";

    public static String messageWelcomeText = ":warning: config not correctly loaded, delete `plugins/console2discord` folder and restart";
    public static String messageGoodbyeText = ":warning: config not correctly loaded, delete `plugins/console2discord` folder and restart";
    public static String messageTruncationText = "...";

    public static boolean contentGroupBySecond = true;
    public static int contentCharLimit = 1900;

    public static SendFormat contentFormat = SendFormat.INLINE;

    public static boolean contentAnsiConvertLegacy = true;
    public static boolean contentAnsiColorWarning = true;
    public static boolean contentAnsiColorError = true;

    public static boolean contentStackTraceAllow = true;
    public static int contentStackTraceCharLimit = 1200;
    public static boolean contentStackTracePrioritizeFirstCause = true;

    public static int queueWaitCapacity = 100;
    public static int queuePostTryCount = 5;

    public static int queueWaitTimeResumeMs = 100;
    public static int queueWaitTimeBetweenMinMs = 100;
    public static int queueWaitTimeBetweenMaxMs = 400;
    public static int queueWaitTimeRetryFirstMs = 500;
    public static int queueWaitTimeRetryIncrementMs = 1000;

    public static boolean debugTestError = false;
    public static boolean debugTestWarning = false;
    public static boolean debugTestCrash = false;

    public static void load(FileConfiguration c) {
        webhookUrl = c.getString("webhook-url", webhookUrl);

        messageWelcomeText = c.getString("message.welcome-text", messageWelcomeText);
        messageGoodbyeText = c.getString("message.goodbye-text", messageGoodbyeText);
        messageTruncationText = c.getString("message.truncation-text", messageTruncationText);

        contentCharLimit = c.getInt("content.body-char-limit", contentCharLimit);
        contentFormat = SendFormat.fromString(c.getString("content.format", String.valueOf(contentFormat)));
        if (contentFormat == null) contentFormat = SendFormat.TEXT;

        contentAnsiConvertLegacy = c.getBoolean("content.ansi.convert-legacy", contentAnsiConvertLegacy);
        contentAnsiColorWarning = c.getBoolean("content.ansi.color-warning", contentAnsiColorWarning);
        contentAnsiColorError = c.getBoolean("content.ansi.color-error", contentAnsiColorError);

        contentStackTraceAllow = c.getBoolean("content.stack-trace.allow", contentStackTraceAllow);
        contentStackTraceCharLimit = c.getInt("content.stack-trace.char-limit", contentStackTraceCharLimit);
        contentStackTracePrioritizeFirstCause = c.getBoolean("content.stack-trace.prioritize-first-cause", contentStackTracePrioritizeFirstCause);

        queueWaitCapacity = c.getInt("queue.wait-capacity", queueWaitCapacity);
        contentGroupBySecond = c.getBoolean("queue.group-by-second", contentGroupBySecond);
        queuePostTryCount = c.getInt("queue.request-try-count", queuePostTryCount);

        queueWaitTimeResumeMs = c.getInt("queue.wait-time.breath-start-ms", queueWaitTimeResumeMs);
        queueWaitTimeBetweenMinMs = c.getInt("queue.wait-time.between-request-min-ms", queueWaitTimeBetweenMinMs);
        queueWaitTimeBetweenMaxMs = c.getInt("queue.wait-time.between-request-max-ms", queueWaitTimeBetweenMaxMs);
        queueWaitTimeRetryFirstMs = c.getInt("queue.wait-time.retry-delay-first-ms", queueWaitTimeRetryFirstMs);
        queueWaitTimeRetryIncrementMs = c.getInt("queue.wait-time.retry-delay-increment-ms", queueWaitTimeRetryIncrementMs);

        debugTestError = c.getBoolean("debug.test-error", debugTestError);
        debugTestWarning = c.getBoolean("debug.test-warning", debugTestWarning);
        debugTestCrash = c.getBoolean("debug.test-crash", debugTestCrash);
    }
}