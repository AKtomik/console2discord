package io.itch.aktomik.console2Discord.send;

import java.util.Objects;
import java.util.regex.Pattern;

public enum WebhookUrlStatus {

    OK("valid webhook URL"),
    YOUR_URL_HERE("set your webhook URL in plugins/console2discord/config.yml when server stopped or using [/c2d webhook <url>] in game"),
    MISSING("webhook URL field is missing from config"),
    NULL("webhook URL is null"),
    NO_HTTPS("webhook URL must start with https://"),
    IS_HTTP("webhook URL can't use http protocol, use https"),
    INVALID("invalid webhook URL"),
    ;

    final String message;
    WebhookUrlStatus(String message) {
        this.message = message;
    }
    public String message() {
        return message;
    }

    private static final Pattern WEBHOOK = Pattern.compile("^https://(?:(?:canary\\.|ptb\\.)?discord(?:app)?\\.com)/api/(?:v\\d+/)?webhooks/(\\d{15,25})/([\\w-]{20,100})/?(?:\\?.*)?$");
    public static WebhookUrlStatus validate(String url) {

        if (url == null) return NULL;
        if (Objects.equals(url, "YOUR_URL_HERE")) return YOUR_URL_HERE;
        if (Objects.equals(url, "MISSING")) return MISSING;
        if (url.startsWith("http://")) return IS_HTTP;
        if (!url.startsWith("https://")) return NO_HTTPS;
        if (WEBHOOK.matcher(url).matches()) return INVALID;

        return OK;
    }
}
