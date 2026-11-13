package io.itch.aktomik.console2Discord;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebhookMailer {

    URL url;

    public WebhookMailer(String urlString) {
        try {
            url = new URL(urlString);
        } catch (IOException ignored) {}
    }

    public void SendMessage(String message) {
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestMethod("GET");
            con.connect();
        } catch (IOException ignored) {}
    }
}
