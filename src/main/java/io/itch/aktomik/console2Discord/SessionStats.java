package io.itch.aktomik.console2Discord;

public class SessionStats {
    public static int sentSuccessCount = 0;
    public static int sentFailureCount = 0;
    public static int sentIgnoredCount = 0;

    public static void reset() {
        sentSuccessCount = 0;
        sentFailureCount = 0;
        sentIgnoredCount = 0;
    }
}
