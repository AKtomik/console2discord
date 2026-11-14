package io.itch.aktomik.console2Discord;

import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class Console2Discord extends JavaPlugin {

    WebhookMailer mailer = null;
    LogListenAppender appender = null;
    SendLoop loop = null;

    org.apache.logging.log4j.core.Logger rootCoreLogger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();

    @Override
    public void onEnable() {
        // config
        saveDefaultConfig();
        String webhookUrl = getConfig().getString("webhook-url");
        if (Objects.equals(webhookUrl, "YOUR_URL_HERE")) {
            getLogger().severe("set your webhook URL in plugins/console2discord/config.yml");
            kill();
            return;
        }

        // mail
        mailer = new WebhookMailer(webhookUrl);
        int status = mailer.sendMessage("hello!");
        if (200 <= status && status < 300) {
            getLogger().info("hello message sent");
        } else {
            String logMessage = "hello message was not sent";
            if (status == -1) logMessage = "critical failure during web request";
            else if (status == -2) logMessage = "illegal URL for web request";
            else if (status > 0) logMessage = "invalid status code after web request CODE=" + status;
            getLogger().severe(logMessage);
            mailer = null;
            kill();
            return;
        }

        // queue
        BlockingQueue<String> queue = new LinkedBlockingQueue<>(10_000);

        // appender
        appender = new LogListenAppender(queue);
        appender.start();
        rootCoreLogger.addAppender(appender);

        // loop
        loop = new SendLoop(queue, mailer);
        loop.enable();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> loop.disable(), "Console2Discord-flush"));

        // log
        getLogger().info("Enabled!");
    }

    public void kill() {
        getLogger().warning("self disabling...");
        getServer().getPluginManager().disablePlugin(this);
    }

    @Override
    public void onDisable() {
        // if reload
        // else, the shutdown hook handles it
        if (!Bukkit.isStopping()) {
            rootCoreLogger.removeAppender(appender);
            appender.stop();
            loop.disable();
        }

        // mail
        if (mailer != null) {
            mailer.sendMessage("bye!");
        }

        // log
        getLogger().info("Disabled!");
    }
}
