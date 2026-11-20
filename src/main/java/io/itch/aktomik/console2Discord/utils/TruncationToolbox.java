package io.itch.aktomik.console2Discord.utils;

public class TruncationToolbox {
    public static String truncateStack(String stack, int limit, boolean prioritizeCause, String truncationMessage) {
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

    public static String truncateLine(String line, int limit, String truncationMessage) {
        if (line.length() <= limit) return line;

        return line.substring(0, Math.max(0, limit - truncationMessage.length())) + truncationMessage;
    }
}
