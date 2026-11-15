package io.itch.aktomik.console2Discord;

public class Settings {
    public static boolean contentAnsiEnable = true;
    public static boolean contentAnsiColorWarning = true;
    public static boolean contentAnsiColorError = true;

    public static boolean contentStackAllow = true;
    public static int contentStackCharLimit = 1200;

    public static boolean groupOnlySameSecond = true;
    public static int groupBodyCharLimit = 1900;

    public static int queueWaitCapacity = 100;
    public static int queuePostTryCount = 5;

    public static int queueWaitTimeResumeMs = 100;
    public static int queueWaitTimeBetweenMinMs = 100;
    public static int queueWaitTimeBetweenMaxMs = 400;
    public static int queueWaitTimeRetryFirstMs = 500;
    public static int queueWaitTimeRetryIterationMs = 1000;
}
