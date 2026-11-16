package io.itch.aktomik.console2Discord;

public enum SendFormat {
    ANSIBLOCK, CODEBLOCK, INLINE, TEXT;

    static SendFormat fromString(String input)
    {
        try {
            return SendFormat.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
