package io.itch.aktomik.console2Discord.send;

import java.util.Objects;
import java.util.regex.Pattern;

public enum WebhookStatus {

    SUCCESS(false, "webhook request fine"),

    YOUR_URL_HERE(true, "set your webhook URL in plugins/console2discord/config.yml when server stopped or using [/c2d webhook <url>] in game"),
    MISSING_URL(true, "webhook URL field is missing from config"),
    NULL_URL(true, "webhook URL is null"),
    NO_HTTPS_URL(true, "webhook URL must start with https://"),
    IS_HTTP_URL(true, "webhook URL can't use http protocol, use https"),
    INVALID_WEBHOOK_URL(true, "invalid webhook URL format"),
    OK_FORMAT(true, "webhook request fine"),

    ;

    final boolean beforeRequest;
    final String message;
    WebhookStatus(boolean beforeRequest, String message) {
        this.beforeRequest = beforeRequest;
        this.message = message;
    }
    public String message() {
        return message;
    }

    private static final Pattern WEBHOOK = Pattern.compile("^https://(?:(?:canary\\.|ptb\\.)?discord(?:app)?\\.com)/api/(?:v\\d+/)?webhooks/(\\d{15,25})/([\\w-]{20,100})/?(?:\\?.*)?$");
    static WebhookStatus validateUrlFormat(String url) {

        if (url == null) return NULL_URL;
        if (Objects.equals(url, "YOUR_URL_HERE")) return YOUR_URL_HERE;
        if (Objects.equals(url, "MISSING")) return MISSING_URL;
        if (url.startsWith("http://")) return IS_HTTP_URL;
        if (!url.startsWith("https://")) return NO_HTTPS_URL;
        if (WEBHOOK.matcher(url).matches()) return INVALID_WEBHOOK_URL;

        return OK_FORMAT;
    }
}
