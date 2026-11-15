package io.itch.aktomik.console2Discord;

import org.apache.logging.log4j.Level;

import java.util.List;
import java.util.stream.Collectors;

public class AnsiToolbox {

    static String charToAnsi(char c) {
        String d;
        switch (Character.toLowerCase(c)) {
            case '0': case '8': d = "30";
            case '1': case '9': d = "34";
            case '2': case 'a': d = "32";
            case '3': case 'b': d = "36";
            case '4': case 'c': d = "31";
            case '5': case 'd': d = "35";
            case '6': case 'e': d = "33";
            case '7': case 'f': d = "37";
            case 'l': d = "1";
            case 'n': d = "4";
            case 'r': d = "0";
            default: d = null;
        };
        return d;
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
                    if (unit.level() == Level.ERROR && Settings.contentAnsiColorError) text = "\033[31m" + text;
                    if (unit.level() == Level.WARN && Settings.contentAnsiColorWarning) text = "\033[93m" + text;
                    return legacyToAnsi(text);
                })
                .collect(Collectors.joining("\033[0m\n"));
        return "```ansi\n" + body + "\n```";
    }

    public static String makeColorless(List<LogUnit> origin)
    {
        return origin.stream()
                .map(unit -> "`" + unit.text() + "`")
                .collect(Collectors.joining("\n"));
    }
}
