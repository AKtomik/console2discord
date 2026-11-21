package io.itch.aktomik.console2Discord.send;

import io.itch.aktomik.console2Discord.utils.JsonBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class WebhookMailer {

    public WebhookMailer(String urlString) {
        lastStatus = WebhookStatus.validateUrlFormat(urlString);
        uri = URI.create(urlString);
    }

    // members

    final private URI uri;
    final private WebhookStatus lastStatus;

    public String getUrl() {
        return uri.getRawPath();
    }

    public WebhookStatus getStatus() {
        return lastStatus;
    }

    // requests

    private int sendRequest(HttpRequest request) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            // send
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // success
            return response.statusCode();
        } catch (IOException e) {
            return -1;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private int sendJson(String json) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return sendRequest(request);
    }

    private int tryGet() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .GET()
                .build();
        return sendRequest(request);
    }

    public int sendMessage(String message) {
        String json = new JsonBuilder()
                .add("content", message)
                .parse();
        return sendJson(json);
    }

    public int sendEmbed(String title, String description, int color, String footer) {
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
