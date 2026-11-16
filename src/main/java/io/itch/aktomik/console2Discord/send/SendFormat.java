package io.itch.aktomik.console2Discord.send;

public enum SendFormat {
    ANSIBLOCK, CODEBLOCK, INLINE, TEXT;

    public static SendFormat fromString(String input)
    {
        try {
            return SendFormat.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
