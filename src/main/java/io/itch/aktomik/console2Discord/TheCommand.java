package io.itch.aktomik.console2Discord;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TheCommand implements CommandExecutor {

    Console2Discord plugin;

    public TheCommand(Console2Discord plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0)
        {
            sender.sendMessage("yo!");
            return false;
        }

        String actionArgument = args[0].toLowerCase();

        if (actionArgument.equals("reload"))
        {
            sender.sendMessage("reloading config from memory...");
            plugin.reloadConfig();
            ConfigSettings.load(plugin.getConfig());
            sender.sendMessage("config reloaded!");
            return true;
        }

        sender.sendMessage("wrong argument");
        return true;
    }
}
