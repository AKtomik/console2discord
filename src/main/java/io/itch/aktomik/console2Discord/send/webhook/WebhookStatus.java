package io.itch.aktomik.console2Discord.send.webhook;

public enum WebhookStatus {
    SUCCESS(true, "webhook request sent"),
    INVALID_TOKEN(false, "webhook token is invalid"),
    UNKNOW_WEBHOOK(false, "unknow (or deleted) webhook"),
    GENERIC_SERVER_ERROR(false, "server error"),
    GENERIC_CLIENT_ERROR(false, "client error"),
    UNKNOW_STATUS(false, "unknow error code"),
    RATE_LIMIT(false, "rate limit"),
    OFFLINE(false, "can't connect to server, is the server connected to the internet?"),
    ;

    private final boolean ok;
    private final String message;
    WebhookStatus(boolean ok, String message) {
        this.ok = ok;
        this.message = message;
    }
    public String message() {
        return message;
    }
    public boolean OK() {
        return ok;
    }
    public boolean KO() {
        return !ok;
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
