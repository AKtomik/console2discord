package io.itch.aktomik.console2Discord;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.time.LocalTime;
import java.util.concurrent.BlockingQueue;

public class LogListenAppender extends AbstractAppender {

    BlockingQueue<String> queue;

    public LogListenAppender(BlockingQueue<String> providedQueue) {
        super("LogListenAppender", null, null, false);
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
        String timeStr = "[" + LocalTime.now().withNano(0) + " " + event.getLevel() + "]: ";

        // timeStr
        String line = timeStr + pluginStr + msgStr;

        queue.offer("`" + line + "`"); // can fail and it is ok
        // queue.add(line); // can only fail by throw
    }
}
