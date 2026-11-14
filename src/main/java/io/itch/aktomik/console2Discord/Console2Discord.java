package io.itch.aktomik.console2Discord;

import org.apache.logging.log4j.LogManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class Console2Discord extends JavaPlugin {

    WebhookMailer mailer;
    LogListenAppender appender;
    SendLoop loop;

    @Override
    public void onEnable() {
        // log
        getLogger().info("enabled!");

        // config
        saveDefaultConfig();
        String webhookUrl = getConfig().getString("webhook-url");

        // queue
        BlockingQueue<String> queue = new LinkedBlockingQueue<>(10_000);
        mailer = new WebhookMailer(webhookUrl, getLogger());

        // appender
        appender = new LogListenAppender(queue);
        appender.start();
        org.apache.logging.log4j.core.Logger root = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
        root.addAppender(appender);

        // loop
        loop = new SendLoop(queue, mailer);
        loop.enable();

        // mail
        int status = mailer.sendMessage("im enabled letzgoooo");
        getLogger().info((status == -1) ? "fail to send message" : "sent message status=" + status);
    }

    @Override
    public void onDisable() {
        // loop
        loop.disable();

        // mail
        int status = mailer.sendMessage("im disabled nooooooo");
        getLogger().info((status == -1) ? "fail to send message" : "sent message status=" + status);

        // log
        getLogger().info("disabled!");
    }
}
