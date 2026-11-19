package io.itch.aktomik.console2Discord;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

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
        plugin.reloadConfig();
        ConfigSettings.load(plugin.getConfig());

        sender.sendMessage("config reloaded from memory");
        plugin.getLogger().info("config reloaded");

        if (plugin.isCommunicating() || manualStop) return getStatus(sender);
        return comStart(sender);
    }

    boolean comStart(CommandSender sender) {
        if (plugin.isCommunicating()) {
            sender.sendMessage("§7communication is already enabled");
            return true;
        }

        manualStop = false;

        if (plugin.startCommunication()) {
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

        plugin.stopCommunication(false);
        if (!ConfigSettings.queueRememberDisconnected && plugin.isListening()) plugin.stopListen();
        sender.sendMessage("§ecommunication stopped");
        return true;
    }
}
