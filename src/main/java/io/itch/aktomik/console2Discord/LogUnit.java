package io.itch.aktomik.console2Discord;

import org.apache.logging.log4j.Level;

import java.time.LocalTime;

public record LogUnit(String text, LocalTime time, Level level) {
    public int length() {
        return text().length();
    }
}
