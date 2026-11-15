package io.itch.aktomik.console2Discord;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;

public class LogListen extends AbstractAppender {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    BlockingQueue<LogUnit> queue;

    public LogListen(BlockingQueue<LogUnit> providedQueue) {
        super("LogListen", null, null, false);
        queue = providedQueue;
    }

    @Override
    public void append(LogEvent event) {
        // message
        String msgStr = event.getMessage().getFormattedMessage();

        // plugin
        String name = event.getLoggerName();
        boolean looksLikePlugin = name != null && !name.isBlank() && !name.contains(".");
        String pluginStr = "";
        if (looksLikePlugin && !msgStr.startsWith("[" + name + "]")) pluginStr = "[" + name + "] ";

        // time
        LocalTime time = Instant.ofEpochMilli(event.getTimeMillis())
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
                .withNano(0);
        Level level = event.getLevel();
        String timeStr = "[" + time.format(TIME_FORMATTER) + " " + level + "]: ";

        String stackStr = "";
        if (ConfigSettings.contentStackTraceAllow) {
            Throwable thrown = event.getThrown();
            if (thrown != null) {
                StringWriter sw = new StringWriter();
                thrown.printStackTrace(new PrintWriter(sw));
                stackStr = "\n" + sw.toString().stripTrailing();
                if (stackStr.length() > ConfigSettings.contentStackTraceCharLimit)
                    stackStr = stackStr.substring(0, ConfigSettings.contentStackTraceCharLimit - 3) + "...";
            }
        }

        // combine
        String line = timeStr + pluginStr + msgStr + stackStr;

        // send
        if (!queue.offer(new LogUnit(line, time, level))) SessionStats.sentIgnoredCount += 1;
    }
}
