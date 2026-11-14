package io.itch.aktomik.console2Discord;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WebhookMailer {

    URI uri;
    boolean validUri = true;

    public WebhookMailer(String urlString) {
        uri = URI.create(urlString);
    }

    public int sendMessage(String message) {
        if (!validUri) return -1;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request;

        String json = new JsonBuilder()
                .add("content", message)
                .toJson();

        try {
            // build request
            request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

        } catch (IllegalArgumentException e) {
            validUri = false;
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
}
