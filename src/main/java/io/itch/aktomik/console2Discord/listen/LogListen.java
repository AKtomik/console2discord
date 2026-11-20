package io.itch.aktomik.console2Discord.listen;

import io.itch.aktomik.console2Discord.ConfigSettings;
import io.itch.aktomik.console2Discord.SessionStats;
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

    private static String truncateStack(String stack, int limit, boolean prioritizeCause, String truncationMessage) {
        if (stack.length() <= limit) return stack;

        if (prioritizeCause) {
            int causeIdx = stack.lastIndexOf("\nCaused by:");
            if (causeIdx > 0) {
                int headEnd = stack.indexOf('\n');
                String head = stack.substring(0, headEnd);
                String content = stack.substring(headEnd, causeIdx);
                String cause = stack.substring(causeIdx);

                int room = limit;
                if (room <= head.length()) {
                    if (room < head.length()) head = head.substring(0, room) + truncationMessage;
                    return head;
                }
                room -= head.length();

                if (room <= cause.length()) {
                    if (room < cause.length()) cause = cause.substring(0, room) + truncationMessage;
                    return head + cause;
                }
                room -= cause.length();

                if (room < content.length()) content = content.substring(0, room) + "\n\t" + truncationMessage;
                return head + content + cause;
            }
        }

        return stack.substring(0, limit) + truncationMessage;
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
                stackStr = truncateStack(
                        stackStr,
                        ConfigSettings.contentStackTraceCharLimit,
                        ConfigSettings.contentStackTracePrioritizeFirstCause,
                        ConfigSettings.messageTruncationText
                );
            }
        }

        // combine
        String line = timeStr + pluginStr + msgStr + stackStr;
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
