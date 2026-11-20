package io.itch.aktomik.console2Discord.listen;

import io.itch.aktomik.console2Discord.ConfigSettings;
import io.itch.aktomik.console2Discord.SessionStats;
import io.itch.aktomik.console2Discord.utils.LogQueue;
import io.itch.aktomik.console2Discord.utils.TruncationToolbox;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LogListen extends AbstractAppender {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    LogQueue queue;

    public LogListen(LogQueue buffer) {
        super("LogListen", null, null, false);
        queue = buffer;
    }

    @Override
    public void append(LogEvent event) {
        // message
        String msgStr = event.getMessage().getFormattedMessage();

        // plugin
        String originStr = "";
        String name = event.getLoggerName();
        // name.contains(".")) -> if class
        if (name != null && !name.isBlank() && !msgStr.startsWith("[" + name + "]") && ConfigSettings.contentShowOrigin) {
            originStr = "[" + name + "] ";
        }

        // filter
        if (!ConfigSettings.filterAllowedLevels.isEmpty() && !ConfigSettings.filterAllowedLevels.contains(event.getLevel().name())) return;

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
                stackStr = TruncationToolbox.truncateStack(
                        stackStr,
                        ConfigSettings.contentStackTraceCharLimit,
                        ConfigSettings.contentStackTracePrioritizeFirstCause,
                        ConfigSettings.messageTruncationText
                );
            }
        }

        // combine
        String line = timeStr + originStr + msgStr;
        line = TruncationToolbox.truncateLine(line, ConfigSettings.contentLineCharLimit, ConfigSettings.messageTruncationText);
        line += stackStr;
        LogUnit unit = new LogUnit(line, time, level);

        // send
        if (ConfigSettings.queueFullIgnoreOlder)
            while (!queue.offer(unit)) {
                queue.poll();
                SessionStats.sentIgnoredCount += 1;
            }
        else
            if (!queue.offer(unit)) SessionStats.sentIgnoredCount += 1;
    }
}
