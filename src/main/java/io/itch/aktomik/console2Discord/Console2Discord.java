package io.itch.aktomik.console2Discord;

import io.itch.aktomik.console2Discord.listen.LogListen;
import io.itch.aktomik.console2Discord.listen.LogUnit;
import io.itch.aktomik.console2Discord.send.SendLoop;
import io.itch.aktomik.console2Discord.utils.WebhookMailer;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

public final class Console2Discord extends JavaPlugin {

    WebhookMailer mailer = null;
    LogListen appender = null;
    SendLoop loop = null;
    BlockingQueue<LogUnit> queue;
    Thread shutdownThread;

    boolean killed = false;
    boolean configResolved = false;

    org.apache.logging.log4j.core.Logger rootCoreLogger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();

    @Override
    public void onLoad() {
        super.onLoad();

        // config preload
        FileConfiguration config;
        try {
            saveDefaultConfig();
            config = getConfig();
            configResolved = true;
        } catch(Exception e) {
            config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        }
        ConfigSettings.load(config);

        // queue
        queue = new LinkedBlockingQueue<>(ConfigSettings.queueWaitCapacity);

        // appender
        appender = new LogListen(queue);
        appender.start();
        rootCoreLogger.addAppender(appender);

        // command
        this.getCommand("console2discord").setExecutor(new TheCommand(this));

        getLogger().info("Loaded!");
    }

    @Override
    public void onEnable() {
        // cleanup if wrong reload
        if (mailer != null) shutdown();
        if (shutdownThread != null) Runtime.getRuntime().removeShutdownHook(shutdownThread);

        // config
        if (!configResolved) {
            saveDefaultConfig();
            ConfigSettings.load(getConfig());
            configResolved = true;
        }

        // webhook check
        String webhookUrl = ConfigSettings.webhookUrl;
        if (Objects.equals(webhookUrl, "YOUR_URL_HERE")) {
            getLogger().severe("Please set your webhook URL in plugins/console2discord/config.yml");
            kill();
            return;
        }
        if (Objects.equals(webhookUrl, "MISSING")) {
            getLogger().severe("webhook URL is not loaded from plugins/console2discord/config.yml");
            kill();
            return;
        }

        // mail
        mailer = new WebhookMailer(webhookUrl);
        String mcVersion = Bukkit.getBukkitVersion().split("-")[0];
        String startDetails = Bukkit.getName() + " " + mcVersion + " - " + getDescription().getName() + " " + getDescription().getVersion();
        int status = mailer.sendEmbed("Console2Discord", ConfigSettings.messageWelcomeText, 10437357, startDetails);
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
        if (ConfigSettings.debugTestError) getLogger().severe("Test Error");
        if (ConfigSettings.debugTestWarning) getLogger().warning("Test Warning");
        if (ConfigSettings.debugTestCrash) {
            try {
                throw new IllegalStateException("Test Catch Crash", new RuntimeException("Exception Cause"));
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Caught Test Exception", e);
            }
//            new Thread(
//                    () -> getLogger().log(Level.SEVERE, "Test Async Crash", new RuntimeException("Exception Cause"))
//                    , "Test-Thread"
//            ).start();
//            Consumer<Integer> recurse = (Integer n) -> {
//                if (n == 0) throw new RuntimeException("deep");
//                recurse.accept(n - 1);
//            };
//            try { recurse.accept(60); } catch (Exception e) { getLogger().log(Level.SEVERE, "Deep trace", e); }
        }
    }

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
            mailer.sendEmbed("Console2Discord", ConfigSettings.messageGoodbyeText, 10437357, statLine);
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
