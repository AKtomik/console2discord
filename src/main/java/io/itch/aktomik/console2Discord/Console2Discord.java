package io.itch.aktomik.console2Discord;

import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class Console2Discord extends JavaPlugin {

    WebhookMailer mailer = null;
    LogListen appender = null;
    SendLoop loop = null;
    BlockingQueue<LogUnit> queue;
    Thread shutdownThread;

    org.apache.logging.log4j.core.Logger rootCoreLogger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();

    @Override
    public void onLoad() {
        super.onLoad();

        // queue
        queue = new LinkedBlockingQueue<>(Settings.queueWaitCapacity);

        // appender
        appender = new LogListen(queue);
        appender.start();
        rootCoreLogger.addAppender(appender);

        getLogger().info("Loaded!");
    }

    @Override
    public void onEnable() {
        // cleanup if wrong reload
        if (mailer != null) shutdown();
        if (shutdownThread != null) Runtime.getRuntime().removeShutdownHook(shutdownThread);

        // config
        saveDefaultConfig();
        String webhookUrl = getConfig().getString("webhook-url");
        if (Objects.equals(webhookUrl, "YOUR_URL_HERE")) {
            getLogger().severe("Please set your webhook URL in plugins/console2discord/config.yml");
            kill();
            return;
        }
        Settings.contentAnsiEnable = getConfig().getBoolean("enable-ansi");

        // mail
        mailer = new WebhookMailer(webhookUrl);
        int status = mailer.sendEmbed("Console2Discord", "Hello!", 10437357, null);
        if (200 <= status && status < 300) {
            getLogger().info("Hello message sent");
        } else {
            String logMessage = "Hello message was not sent";
            if (status == -1) logMessage = "Critical failure during web request";
            else if (status == -2) logMessage = "Illegal URL for web request";
            else if (status > 0) logMessage = "Invalid status code after web request CODE=" + status;
            getLogger().severe(logMessage);
            mailer = null;
            kill();
            return;
        }

        // loop
        loop = new SendLoop(queue, mailer);
        loop.enable();
        shutdownThread = new Thread(this::shutdown, "Console2Discord-flush");
        Runtime.getRuntime().addShutdownHook(shutdownThread);

        // log
        getLogger().info("Enabled!");
        // getLogger().severe("Test Error");
        // getLogger().warning("Test Warning");
    }

    boolean killed = false;

    public void kill() {
        killed = true;
        getLogger().warning("Self disabling...");
        getServer().getPluginManager().disablePlugin(this);
    }

    public void shutdown() {
        // mail
        Runnable will = () -> {
            String statLine = SessionStats.sentSuccessCount + " logs sent"
                    + " - " + SessionStats.sentFailureCount + " failed"
                    + " - " + SessionStats.sentIgnoredCount + " ignored";
            mailer.sendEmbed("Console2Discord", "Bye!", 10437357, statLine);
        };

        // appender
        rootCoreLogger.removeAppender(appender);
        appender.stop();

        // loop
        loop.disable(will);
    }

    @Override
    public void onDisable() {
        // if reload
        // else, the shutdown hook handles it
        //if (!Bukkit.isStopping() && !killed) {
        //    if (shutdownThread != null) Runtime.getRuntime().removeShutdownHook(shutdownThread);
        //    shutdown();
        //}

        // log
        getLogger().info("Disabled!");
    }
}
