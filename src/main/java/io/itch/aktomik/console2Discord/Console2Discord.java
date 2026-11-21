package io.itch.aktomik.console2Discord;

import io.itch.aktomik.console2Discord.listen.LogListen;
import io.itch.aktomik.console2Discord.send.SendLoop;
import io.itch.aktomik.console2Discord.listen.LogQueue;
import io.itch.aktomik.console2Discord.send.webhook.WebhookMailer;
import io.itch.aktomik.console2Discord.send.webhook.WebhookStatus;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;
import java.util.logging.Level;

public final class Console2Discord extends JavaPlugin {

    WebhookMailer mailer = null;
    LogListen appender = null;
    SendLoop loop = null;
    Thread shutdownThread = null;

    volatile LogQueue queue;

    org.apache.logging.log4j.core.Logger rootCoreLogger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();

    // - enable -

    @Override
    public void onLoad() {
        super.onLoad();

        // config preload
        saveDefaultConfig();
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
        if (isCommunicating()) stopCommunication(false, false);
        if (shutdownThread != null) Runtime.getRuntime().removeShutdownHook(shutdownThread);

        // start communication
        WebhookMailer setupMailer = new WebhookMailer(ConfigSettings.webhookUrl);
        if (!setupMailer.isUrlValid()) {
            getLogger().severe(setupMailer.getUrlValidation().message());
            getLogger().warning("invalid webook URL, you can retry with [/c2d webhook <url>]");
        } else if (!startCommunication(setupMailer)) {
            getLogger().warning("communication failed, you can retry with [/c2d reload]");
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
        queue = new LogQueue(ConfigSettings.queueWaitCapacity);

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

    boolean startCommunication(WebhookMailer newMailer) {
        // mail
        String mcVersion = Bukkit.getBukkitVersion().split("-")[0];
        String startDetails = Bukkit.getName() + " " + mcVersion + " - " + getDescription().getName() + " " + getDescription().getVersion();
        WebhookStatus status = newMailer.sendEmbed("Console2Discord", ConfigSettings.messageWelcomeText, 10437357, startDetails);
        if (status.ok()) {
            getLogger().info("Hello message sent");
        } else {
            getLogger().severe(status.message());
            getLogger().info("Communication failed");
            return false;
        }

        // valid
        mailer = newMailer;

        // loop
        loop = new SendLoop(queue, mailer);
        loop.enable();
        getLogger().info("Communication started");

        return true;
    }

    void stopCommunication(boolean workerDelay, boolean goodbyeWill) {
        if (loop == null) return;

        // loop
        if (workerDelay) {
            // will monopolize the thread
            loop.disableStart(ConfigSettings.queueWaitTimeShutdownWorkerMarginMs);
        } else {
            loop.disableNow();
        }
        loop = null;

        // goodbye
        if (goodbyeWill) {
            String statLine = SessionStats.sentSuccessCount + " logs sent"
                    + " - " + SessionStats.sentFailureCount + " failed"
                    + " - " + SessionStats.sentIgnoredCount + " ignored";
            SessionStats.reset();
            mailer.sendEmbed("Console2Discord", ConfigSettings.messageGoodbyeText, 10437357, statLine);
            mailer = null;
        };

        getLogger().info("Communication stopped");
    }

    boolean isCommunicating() {
        return mailer != null;
    }

    // - misc -

    private void applyConfig(FileConfiguration config) {
        ConfigSettings.load(config);

        // config changes
        final int configCapacity = ConfigSettings.queueWaitCapacity;
        if (isListening() && configCapacity != queue.getCapacity()) queue.setCapacity(configCapacity);
    }

    public void loadConfig() {
        try {
            FileConfiguration config;
            config = getConfig();
            applyConfig(config);
        } catch(Exception e) {
            // can be used if coming from onLoad()
            readConfig();
            return;
        }
        getLogger().info("Config loaded");
    }

    public void readConfig() {
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        applyConfig(config);
        getLogger().info("Config read");
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
        if (isCommunicating()) stopCommunication(true, true);
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled!");
    }
}
