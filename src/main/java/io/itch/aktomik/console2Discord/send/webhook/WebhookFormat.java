package io.itch.aktomik.console2Discord.send.webhook;

import java.util.Objects;
import java.util.regex.Pattern;

public enum WebhookFormat {
    FINE(false, "webhook URL is fine"),
    NULL_URL(false, "webhook URL is null"),
    YOUR_URL_HERE(false, "set your webhook URL in plugins/console2discord/config.yml when server stopped or using [/c2d webhook <url>] in game"),
    MISSING_URL(false, "webhook URL field is missing from config"),
    NO_HTTPS_URL(false, "webhook URL must start with https://"),
    IS_HTTP_URL(false, "webhook URL can't use http protocol, use https"),
    INVALID_WEBHOOK_URL(false, "invalid webhook URL format");

    final boolean ok;
    final String message;
    WebhookFormat(boolean ok, String message) {
        this.ok = ok;
        this.message = message;
    }
    public String message() {
        return message;
    }
    public boolean ok() {
        return ok;
    }

    private static final Pattern WEBHOOK = Pattern.compile("^https://(?:(?:canary\\.|ptb\\.)?discord(?:app)?\\.com)/api/(?:v\\d+/)?webhooks/(\\d{15,25})/([\\w-]{20,100})/?(?:\\?.*)?$");
    static WebhookFormat validate(String url) {

        if (url == null) return NULL_URL;
        if (Objects.equals(url, "YOUR_URL_HERE")) return YOUR_URL_HERE;
        if (Objects.equals(url, "MISSING")) return MISSING_URL;
        if (url.startsWith("http://")) return IS_HTTP_URL;
        if (!url.startsWith("https://")) return NO_HTTPS_URL;
        if (WEBHOOK.matcher(url).matches()) return INVALID_WEBHOOK_URL;

        return FINE;
    }
}
