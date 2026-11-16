package io.itch.aktomik.console2Discord;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TheCommand implements CommandExecutor {

    Console2Discord plugin;
    boolean communicationPaused = false;

    public TheCommand(Console2Discord plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) args = new String[] { "help" };

        String actionArgument = args[0].toLowerCase();

        switch (actionArgument) {
            case "help": return false;

            case "reload":
            {
                sender.sendMessage("reloading config from memory...");
                plugin.reloadConfig();
                ConfigSettings.load(plugin.getConfig());
                sender.sendMessage("config reloaded");
                if (!plugin.isCommunicating() && !communicationPaused)
                    if (plugin.startCommunication())
                        sender.sendMessage("communication started");
                    else
                        sender.sendMessage("communication failed");
                return true;
            }

            case "start":
            {
                if (plugin.isCommunicating()) {
                    sender.sendMessage("communication is already on");
                    return true;
                }

                communicationPaused = false;
                if (plugin.startCommunication())
                    sender.sendMessage("communication started");
                else
                    sender.sendMessage("communication failed");
                return true;
            }

            case "stop":
            {
                if (!plugin.isCommunicating()) {
                    sender.sendMessage("communication is already off");
                    return true;
                }

                plugin.stopCommunication();
                sender.sendMessage("communication stopped");
                sender.sendMessage("[/console2discord start] to start again");
                return true;
            }

            case "pause":
            {
                if (communicationPaused) {
                    sender.sendMessage("communication is already paused");
                    sender.sendMessage("[/console2discord start] to start again");
                    return true;
                }
                communicationPaused = true;

                if (plugin.isCommunicating()) plugin.stopCommunication();
                sender.sendMessage("communication paused");
                sender.sendMessage("[/console2discord start] to start again");
                return true;
            }
        }

        sender.sendMessage("wrong argument");
        return true;
    }
}
