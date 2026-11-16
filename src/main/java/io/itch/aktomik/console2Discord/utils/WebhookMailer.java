package io.itch.aktomik.console2Discord.utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class WebhookMailer {

    URI uri;

    public WebhookMailer(String urlString) {
        uri = URI.create(urlString);
    }

    private int sendJson(String json) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request;

        try {
            // build request
            request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

        } catch (IllegalArgumentException e) {
            return -2;
        }

        try {
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
