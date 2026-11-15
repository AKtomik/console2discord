package io.itch.aktomik.console2Discord;

public class Settings {
    public static boolean enableAnsi = true;
    public static boolean coloringWarning = true;
    public static boolean coloringError = true;

    public static boolean groupOnlySameSecond = true;
    public static int groupMaxBodyChar = 1900;

    public static int sendTryCount = 5;
    public static boolean sendStackIfThrow = true;

    public static int queueCapacity = 1000;
    public static int queueWaitTimeResumeMs = 100;
    public static int queueWaitTimeBetweenMinMs = 100;
    public static int queueWaitTimeBetweenMaxMs = 400;
    public static int queueWaitTimeRetryFirstMs = 500;
    public static int queueWaitTimeRetryIterationMs = 1000;
}
