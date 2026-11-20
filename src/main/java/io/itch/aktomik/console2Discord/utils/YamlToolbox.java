package io.itch.aktomik.console2Discord.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class YamlToolbox {
    public static void replaceTopLevelValue(File file, String key, String value) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        boolean found = false;

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith(key + ":")) {
                lines.set(i, key + ": " + value);
                found = true;
                break;
            }
        }
        if (!found) lines.add(key + ": " + value);

        Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
    }
}
