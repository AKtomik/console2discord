package io.itch.aktomik.console2Discord;

import org.apache.logging.log4j.Level;

import java.time.LocalTime;

public final class LogUnit {

    private final String text;
    private final LocalTime time;
    private final Level level;

    public LogUnit(String text, LocalTime time, Level level) {
        this.text = text;
        this.time = time;
        this.level = level;
    }

    public String text() { return text; }
    public LocalTime time() { return time; }
    public Level level() { return level; }

    public int length() {
        return text().length();
    }
}
