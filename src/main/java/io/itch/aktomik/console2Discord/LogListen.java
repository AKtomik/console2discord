package io.itch.aktomik.console2Discord;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.time.LocalTime;
import java.util.concurrent.BlockingQueue;

public class LogListen extends AbstractAppender {

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
        LocalTime time = LocalTime.now().withNano(0);
        Level level = event.getLevel();
        String timeStr = "[" + time + " " + level + "]: ";

        // combine
        String line = timeStr + pluginStr + msgStr;

        queue.offer(new LogUnit(line, time, level)); // can fail and it is ok
        // queue.add(line); // can only fail by throw
    }
}
