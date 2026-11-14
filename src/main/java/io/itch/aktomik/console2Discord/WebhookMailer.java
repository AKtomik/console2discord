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

    public int SendMessage(String message) {
        if (!validUri) return -1;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request;

        try {

            // build
            request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        } catch (IllegalArgumentException e) {
            String errorMsg = "wrong url: " + uri.getPath();
            logger.log(Level.WARNING, errorMsg);
            validUri = false;
            return -1;
        }

        try {

            // send
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            // success
            return response.statusCode();

        } catch (IOException e) {
            logger.log(Level.WARNING, "IOException while sending HTTP request", e);
            return -1;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
