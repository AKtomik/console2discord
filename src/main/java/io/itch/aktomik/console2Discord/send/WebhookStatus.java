package io.itch.aktomik.console2Discord.send;

import java.util.Objects;
import java.util.regex.Pattern;

public enum WebhookStatus {


    OK_FORMAT(false, "webhook URL is fine"),
    NULL_URL(false, "webhook URL is null"),
    YOUR_URL_HERE(false, "set your webhook URL in plugins/console2discord/config.yml when server stopped or using [/c2d webhook <url>] in game"),
    MISSING_URL(false, "webhook URL field is missing from config"),
    NO_HTTPS_URL(false, "webhook URL must start with https://"),
    IS_HTTP_URL(false, "webhook URL can't use http protocol, use https"),
    INVALID_WEBHOOK_URL(false, "invalid webhook URL format"),

    SUCCESS(true, "webhook request sent"),
    INVALID_TOKEN(true, "webhook token is invalid"),
    UNKNOW_WEBHOOK(true, "unknow (or deleted) webhook"),
    GENERIC_SERVER_ERROR(true, "server error"),
    GENERIC_CLIENT_ERROR(true, "client error"),
    UNKNOW_STATUS(true, "unknow error code"),
    RATE_LIMIT(true, "rate limit"),
    OFFLINE(true, "can't connect to server, is the server connected to the internet?"),
    ;

    final boolean afterRequest;
    final String message;
    WebhookStatus(boolean afterRequest, String message) {
        this.afterRequest = afterRequest;
        this.message = message;
    }
    public String message() {
        return message;
    }

    private static final Pattern WEBHOOK = Pattern.compile("^https://(?:(?:canary\\.|ptb\\.)?discord(?:app)?\\.com)/api/(?:v\\d+/)?webhooks/(\\d{15,25})/([\\w-]{20,100})/?(?:\\?.*)?$");
    static WebhookStatus validateFormat(String url) {

        if (url == null) return NULL_URL;
        if (Objects.equals(url, "YOUR_URL_HERE")) return YOUR_URL_HERE;
        if (Objects.equals(url, "MISSING")) return MISSING_URL;
        if (url.startsWith("http://")) return IS_HTTP_URL;
        if (!url.startsWith("https://")) return NO_HTTPS_URL;
        if (WEBHOOK.matcher(url).matches()) return INVALID_WEBHOOK_URL;

        return OK_FORMAT;
    }

    static WebhookStatus fromStatus(int status) {
        if (200 <= status && status < 300) return SUCCESS;

        switch (status) {
            case 401: return INVALID_TOKEN;
            case 404: return UNKNOW_WEBHOOK;
            case 429: return RATE_LIMIT;
        }

        if (400 <= status && status < 500) return GENERIC_CLIENT_ERROR;
        if (500 <= status && status < 600) return GENERIC_SERVER_ERROR;

        return UNKNOW_STATUS;
    }
}
