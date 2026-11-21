package io.itch.aktomik.console2Discord.utils;

import java.util.Objects;

public enum WebhookUrlStatus {

    OK("valid webhook url"),
    DEFAULT("set your webhook URL in plugins/console2discord/config.yml when server stopped or using [/c2d webhook <url>] in game"),
    MISSING("webhook URL field is missing from config"),
    NO_HTTP("URL must start with http:// or https://"),
    INVALID_URL("invalid URL"),
    ;

    final String message;
    WebhookUrlStatus(String message) {
        this.message = message;
    }
    public String message() {
        return message;
    }

    public static WebhookUrlStatus validate(String url) {

        if (Objects.equals(url, "YOUR_URL_HERE")) return DEFAULT;
        if (Objects.equals(url, "MISSING")) return MISSING;
        if (!url.startsWith("http://") && !url.startsWith("https://")) return NO_HTTP;

        return OK;
    }
}
