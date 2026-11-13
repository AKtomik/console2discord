package io.itch.aktomik.console2Discord;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebhookMailer {

    URL url;
    Logger logger;

    public WebhookMailer(String urlString, Logger pluginLogger) {
        logger = pluginLogger;
        try {
            url = new URL(urlString);
        } catch (IOException e) {
            logger.log(Level.WARNING, "IOException while sending HTTP request", e);
        }
    }

    public int SendMessage(String message) {
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setInstanceFollowRedirects(false);
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            con.setRequestProperty("charset", "utf-8");
            con.connect();
            return con.getResponseCode();// send
        } catch (IOException e) {
            logger.log(Level.WARNING, "IOException while sending HTTP request", e);
            return -1;
        }
    }
}
