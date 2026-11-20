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
    Thread shutdownThread = null;

    volatile BlockingQueue<LogUnit> queue = null;
    volatile int queueCapacity = -1;

    org.apache.logging.log4j.core.Logger rootCoreLogger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();

    // - enable -

    @Override
    public void onLoad() {
        super.onLoad();

        // config preload
        loadConfig();

        // start listen
        startListen();

        // log
        getLogger().info("Loaded!");
    }

    @Override
    public void onEnable() {
        // command
        this.getCommand("console2discord").setExecutor(new TheCommand(this));

        // cleanup from reload
        if (isCommunicating()) stopCommunication(false);
        if (shutdownThread != null) Runtime.getRuntime().removeShutdownHook(shutdownThread);

        // start communication
        if (!startCommunication()) {
            getLogger().warning("communication failed, you cand reconfigure and retry with [/c2d reload]");
        }

        // shutdown listen
        shutdownThread = new Thread(this::shutdown, "Console2Discord-flush");
        Runtime.getRuntime().addShutdownHook(shutdownThread);

        // log
        getLogger().info("Enabled!");
        debugMessages();
    }

    // - listen -

    void startListen() {
        // queue
        queueCapacity = ConfigSettings.queueWaitCapacity;
        queue = new LinkedBlockingQueue<>(queueCapacity);

        // appender
        appender = new LogListen(queue);
        appender.start();
        rootCoreLogger.addAppender(appender);

        getLogger().info("Listen started");
    }

    void stopListen() {
        // appender
        rootCoreLogger.removeAppender(appender);
        appender.stop();
        appender = null;

        getLogger().info("Listen stopped");
    }

    boolean isListening() {
        return appender != null;
    }

    // - communication -

    boolean startCommunication() {
        // webhook check
        String webhookUrl = ConfigSettings.webhookUrl;
        if (Objects.equals(webhookUrl, "YOUR_URL_HERE")) {
            getLogger().severe("Please set your webhook URL in plugins/console2discord/config.yml");
            getLogger().info("Communication failed");
            return false;
        }
        if (Objects.equals(webhookUrl, "MISSING")) {
            getLogger().severe("webhook URL is not loaded from plugins/console2discord/config.yml");
            getLogger().info("Communication failed");
            return false;
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
            getLogger().info("Communication failed");
            return false;
        }

        // loop
        loop = new SendLoop(queue, mailer);
        loop.enable();
        getLogger().info("Communication started");

        return true;
    }

    void stopCommunication(boolean allowDelay) {
        // mail
        Runnable will = () -> {
            String statLine = SessionStats.sentSuccessCount + " logs sent"
                    + " - " + SessionStats.sentFailureCount + " failed"
                    + " - " + SessionStats.sentIgnoredCount + " ignored";
            SessionStats.reset();
            mailer.sendEmbed("Console2Discord", ConfigSettings.messageGoodbyeText, 10437357, statLine);
            mailer = null;
        };
        // loop
        if (allowDelay) loop.disableNow();
        else loop.disable(will);// will monopolize the thread

        getLogger().info("Communication stopped");
    }

    boolean isCommunicating() {
        return mailer != null;
    }

    // - misc -

    public void loadConfig() {
        FileConfiguration config;
        try {
            saveDefaultConfig();
            config = getConfig();
        } catch(Exception e) {
            // can be used if coming from onLoad()
            config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        }

        ConfigSettings.load(config);

        // config changes
        final int configCapacity = ConfigSettings.queueWaitCapacity;
        getLogger().info("do switch from queue a to queue b???: " + queueCapacity + " to " + configCapacity);// !
        if (isListening() && configCapacity != queueCapacity)
        {
            getLogger().info("Switch from queue a to queue b: " + queueCapacity + " to " + configCapacity);
            BlockingQueue<LogUnit> oldQueue = this.queue;
            BlockingQueue<LogUnit> newQueue = new LinkedBlockingQueue<>(configCapacity);
            this.queue = newQueue;

            while (oldQueue.size() > configCapacity) oldQueue.poll();
            if (newQueue.remainingCapacity() > 0) oldQueue.drainTo(newQueue);
        }
        getLogger().info("Config loaded");
    }

    public void debugMessages() {
        // debug config version
        String configVersion = ConfigSettings.debugGeneratedVersion;
        String pluginVersion = getDescription().getVersion();
        if (!Objects.equals(configVersion, pluginVersion))
        {
            getLogger().warning("config version (" + configVersion + ") and plugin version (" + pluginVersion + ") does not match" +
                    "\nremove your current configuration file to generate a new one");
        }

        // debug errors and others
        if (ConfigSettings.debugTestWarning) getLogger().warning("Test Warning");
        if (ConfigSettings.debugTestError) getLogger().severe("Test Error");
        if (ConfigSettings.debugTestCrash) {
            try {
                throw new IllegalStateException("Test Exception Message", new RuntimeException("Test Exception Cause"));
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Test Exception Caught", e);
            }
        }
    }

    // - disable -

    public void shutdown() {
        if (isListening()) stopListen();
        if (isCommunicating()) stopCommunication(true);
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled!");
    }
}
