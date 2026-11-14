package io.itch.aktomik.console2Discord;

import org.bukkit.plugin.java.JavaPlugin;

public final class Console2Discord extends JavaPlugin {

    WebhookMailer mailer;

    @Override
    public void onEnable() {
        getLogger().info("enabled!");

        // config
        saveDefaultConfig();
        String webhookUrl = getConfig().getString("webhook-url");

        // mail
        mailer = new WebhookMailer(webhookUrl, getLogger());
        int status = mailer.SendMessage("im i ignored?");
        getLogger().info((status == -1) ? "fail to send message" : "sent message status=" + status);
    }

    @Override
    public void onDisable() {
        getLogger().info("disabled!");
    }
}
