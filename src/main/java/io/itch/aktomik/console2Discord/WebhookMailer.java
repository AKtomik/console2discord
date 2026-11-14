package io.itch.aktomik.console2Discord;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebhookMailer {

    URI uri;
    Logger logger;
    boolean validUri = true;

    public WebhookMailer(String urlString, Logger pluginLogger) {
        logger = pluginLogger;
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
            logger.log(Level.SEVERE, "illegal URL");
            logger.warning("URL=" + uri.getRawPath());
            validUri = false;
            return -1;
        }

        try {
            // send
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // success
            return response.statusCode();

        } catch (IOException e) {
            logger.log(Level.SEVERE, "HTTP request failed");
            logger.warning("URL=" + uri.getRawPath());
            return -1;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
