package io.itch.aktomik.console2Discord;

import org.apache.logging.log4j.Level;

import java.util.List;
import java.util.stream.Collectors;

public class AnsiToolbox {

    static String charToAnsi(char c) {
        return switch (Character.toLowerCase(c)) {
            case '0', '8' -> "30";
            case '1', '9' -> "34";
            case '2', 'a' -> "32";
            case '3', 'b' -> "36";
            case '4', 'c' -> "31";
            case '5', 'd' -> "35";
            case '6', 'e' -> "33";
            case '7', 'f' -> "37";
            case 'l' -> "1";
            case 'n' -> "4";
            case 'r' -> "0";
            default -> null;
        };
    }

    static String legacyToAnsi(String s) {
        StringBuilder out = new StringBuilder();
        boolean colored = false;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '§' && i + 1 < s.length()) {
                String code = charToAnsi(s.charAt(++i));
                if (code != null) {
                    out.append("\u001b[").append(code).append('m');
                    colored = !code.equals("0");
                }
            } else out.append(ch);
        }
        if (colored) out.append("\u001b[0m");
        return out.toString();
    }

    public static String makeAnsiBlock(List<LogUnit> origin)
    {
        String body = origin.stream()
                .map(unit -> {
                    String text = unit.text();
                    if (ConfigSettings.contentAnsiConvertLegacy) text = legacyToAnsi(text);
                    if (ConfigSettings.contentAnsiColorError && unit.level() == Level.ERROR) text = "\033[31m" + text;
                    else if (ConfigSettings.contentAnsiColorWarning && unit.level() == Level.WARN) text = "\033[93m" + text;
                    return text;
                })
                .collect(Collectors.joining("\033[0m\n"));
        return "```ansi\n" + body + "\n```";
    }

    public static String makeCodeBlock(List<LogUnit> origin)
    {
        String body = makeText(origin);
        return "```\n" + body + "\n```";
    }

    public static String makeCodeLine(List<LogUnit> origin)
    {
        return origin.stream()
                .map(unit -> "`" + unit.text() + "`")
                .collect(Collectors.joining("\n"));
    }

    public static String makeText(List<LogUnit> origin)
    {
        return origin.stream()
                .map(LogUnit::text)
                .collect(Collectors.joining("\n"));
    }
}
