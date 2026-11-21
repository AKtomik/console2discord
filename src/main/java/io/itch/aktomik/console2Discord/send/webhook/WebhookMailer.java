package io.itch.aktomik.console2Discord.send.webhook;

import io.itch.aktomik.console2Discord.utils.JsonBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class WebhookMailer {

    public WebhookMailer(String urlString) {
        urlFormat = WebhookFormat.validate(urlString);
        if (urlFormat.ok) uri = URI.create(urlString);
        else uri = null;
    }

    // atrs

    private final URI uri;
    private final WebhookFormat urlFormat;

    public String getUrlString() {
        return uri.getRawPath();
    }

    public WebhookFormat getUrlValidation() {
        return urlFormat;
    }

    public boolean isUrlValid() {
        return urlFormat.ok();
    }

    // requests

    private WebhookStatus sendRequest(HttpRequest request) {
        if (uri == null) return WebhookStatus.INVALID_URI;
        try {
            HttpClient client = HttpClient.newHttpClient();
            // send
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // success
            return WebhookStatus.fromStatus(response.statusCode());
        } catch (IOException e) {
            return WebhookStatus.OFFLINE;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private WebhookStatus sendJson(String json) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return sendRequest(request);
    }

    public WebhookStatus check() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .GET()
                .build();
        return sendRequest(request);
    }

    public WebhookStatus sendMessage(String message) {
        String json = new JsonBuilder()
                .add("content", message)
                .parse();
        return sendJson(json);
    }

    public WebhookStatus sendEmbed(String title, String description, int color, String footer) {
        JsonBuilder embed = new JsonBuilder()
                .add("title", title)
                .add("description", description)
                .add("color", color)
                .add("footer", new JsonBuilder().add("text", footer));
        String json = new JsonBuilder()
                .add("embeds", List.of(embed))
                .parse();
        return sendJson(json);
    }
}
