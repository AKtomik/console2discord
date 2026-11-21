package io.itch.aktomik.console2Discord;

import io.itch.aktomik.console2Discord.send.webhook.WebhookMailer;
import io.itch.aktomik.console2Discord.utils.TruncationToolbox;
import io.itch.aktomik.console2Discord.utils.YamlToolbox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class TheCommand implements CommandExecutor {

    Console2Discord plugin;
    boolean manualStop = false;

    public TheCommand(Console2Discord plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) args = new String[] { "help" };

        String actionArgument = args[0].toLowerCase();

        switch (actionArgument) {
            case "help": return getHelp(sender);
            case "webhook": {
                if (args.length > 1)
                    return setWebhook(sender, args[1]);
                else
                    return getWebhook(sender);
            }
            case "status": return getStatus(sender);
            case "reload": return comReload(sender);
            case "start": return comStart(sender);
            case "stop": return comStop(sender);
        }

        // sender.sendMessage("wrong argument");
        return false;
    }

    boolean getHelp(CommandSender sender) {
        sender.sendMessage("""
§8/console2discord §7help
§f get this help message
§8/console2discord §7webhook <url>
§f set the webhook url
§8/console2discord §7status
§f show current communication state
§8/console2discord §7reload
§f reload the config and try establish communication back
§8/console2discord §7stop
§f stop the communication of logs to discord
§8/console2discord §7start
§f start back the communication"""
        );
        return true;
    }

    boolean getStatus(CommandSender sender) {
        String maj = "console2discord communication state: ";
        if (plugin.isCommunicating()) sender.sendMessage(maj + "§l§aenabled");
        else if (manualStop) sender.sendMessage(maj + "§l§estopped");
        else sender.sendMessage(maj + "§l§cfailed");
        return true;
    }

    boolean comReload(CommandSender sender) {
        plugin.readConfig();
        plugin.debugMessages();
        sender.sendMessage("config reloaded from memory");

        if (plugin.isCommunicating() || manualStop) return getStatus(sender);
        return comStart(sender);
    }

    boolean comStart(CommandSender sender) {
        if (plugin.isCommunicating()) {
            sender.sendMessage("§7communication is already enabled");
            return true;
        }

        manualStop = false;

        // validate
        WebhookMailer webhookMailer = new WebhookMailer(ConfigSettings.webhookUrl);
        if (!webhookMailer.isUrlOk()) {
            sender.sendMessage("§c" + webhookMailer.getUrlFormat().message());
            return true;
        }

        // start
        if (plugin.startCommunication(webhookMailer)) {
            if (!plugin.isListening()) plugin.startListen();
            sender.sendMessage("§acommunication started");
        }
        else {
            sender.sendMessage("§ccommunication failed");
        }
        return true;
    }

    boolean comStop(CommandSender sender) {
        if (manualStop && !plugin.isCommunicating()) {
            sender.sendMessage("§7communication is already stopped");
            return true;
        }

        manualStop = true;

        plugin.stopCommunication(false, true);
        if (!ConfigSettings.queueRememberDisconnected && plugin.isListening()) plugin.stopListen();
        sender.sendMessage("§ecommunication stopped");
        return true;
    }

    boolean getWebhook(CommandSender sender) {
        sender.sendMessage("webhook url is: "+ConfigSettings.webhookUrl);
        return getStatus(sender);
    }

    boolean setWebhook(CommandSender sender, String url) {
//        if (Objects.equals(ConfigSettings.webhookUrl, url))
//        {
//            sender.sendMessage("is the same webhook url");
//            return true;
//        }

        // validate
        WebhookMailer webhookMailer = new WebhookMailer(url);
        if (!webhookMailer.isUrlOk()) {
            sender.sendMessage("§c" + webhookMailer.getUrlFormat().message());
            return true;
        }

//        plugin.stopCommunication(false, false);
        sender.sendMessage("changing webhook url...");

        // start
        if (plugin.startCommunication(webhookMailer)) {
            if (!plugin.isListening()) plugin.startListen();
            sender.sendMessage("§acommunication started");
        } else {
            sender.sendMessage("§ccommunication failed");
            return true;
        }

        ConfigSettings.webhookUrl = url;
        plugin.getConfig().set("webhook-url", url);
        try {
            YamlToolbox.replaceTopLevelValue(new File(plugin.getDataFolder(), "config.yml"), "webhook-url", url);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "write of webhook-url in config failed", e);
        }
        plugin.reloadConfig();

        sender.sendMessage("changed webhook url to: "+ TruncationToolbox.truncateLine(url, 60, "*******/****..."));
        return getStatus(sender);
    }
}
