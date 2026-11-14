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

    public WebhookMailer(String urlString, Logger pluginLogger) {
        logger = pluginLogger;
        uri = URI.create(urlString);
    }

    public int SendMessage(String message) {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .GET()
            .build();

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
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }
}
