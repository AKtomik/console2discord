package io.itch.aktomik.console2Discord;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class Console2Discord extends JavaPlugin {

    WebhookMailer mailer;

    @Override
    public void onEnable() {
        getLogger().info("enabled!");

        // config
        saveDefaultConfig();
        String url = getConfig().getString("url");

        // mail
        mailer = new WebhookMailer(url, getLogger());
        mailer.SendMessage("im ignored :(");
        getLogger().info("sended message");
    }

    @Override
    public void onDisable() {
        getLogger().info("disabled!");
    }
}
