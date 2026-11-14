package io.itch.aktomik.console2Discord;

import org.apache.logging.log4j.LogManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class Console2Discord extends JavaPlugin {

    WebhookMailer mailer = null;
    LogListenAppender appender = null;
    SendLoop loop = null;

    @Override
    public void onEnable() {
        // log
        getLogger().info("Enabled!");

        // config
        saveDefaultConfig();
        String webhookUrl = getConfig().getString("webhook-url");
        if (Objects.equals(webhookUrl, "YOUR_URL_HERE")) {
            getLogger().severe("set your webhook URL in plugins/console2discord/config.yml");
            kill();
            return;
        }

        // mail
        mailer = new WebhookMailer(webhookUrl, getLogger());
        int status = mailer.sendMessage("hello!");
        if (200 <= status && status < 300) {
            getLogger().info("hello message sent");
        } else if (status == -1) {
            getLogger().severe("failure sending message");
            mailer = null;
            kill();
            return;
        } else {
            getLogger().severe("invalid status code = " + status);
            mailer = null;
            kill();
            return;
        }

        // queue
        BlockingQueue<String> queue = new LinkedBlockingQueue<>(10_000);

        // appender
        appender = new LogListenAppender(queue);
        appender.start();
        org.apache.logging.log4j.core.Logger root = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
        root.addAppender(appender);

        // loop
        loop = new SendLoop(queue, mailer);
        loop.enable();

    }

    public void kill() {
        getLogger().warning("self disabling...");
        getServer().getPluginManager().disablePlugin(this);
    }

    @Override
    public void onDisable() {
        // loop
        if (loop != null) {
            loop.disable();
        }

        // mail
        if (mailer != null) {
            int status = mailer.sendMessage("im disabled nooooooo");
            getLogger().info((status == -1) ? "fail to send message" : "sent message status=" + status);
        }

        // log
        getLogger().info("Disabled!");
    }
}
