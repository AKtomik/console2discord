package io.itch.aktomik.console2Discord;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class Console2Discord extends JavaPlugin {

    WebhookMailer mailer;

    @Override
    public void onEnable() {
        getLogger().log(Level.FINE, "enabled!");
        mailer = new WebhookMailer("http://webhook.site/68d56892-1046-41b5-aab4-e41a6a736085");
        mailer.SendMessage("im ignored :(");
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.FINE, "disabled!");
    }
}
