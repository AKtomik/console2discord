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
            case "help": return false;

            case "reload":
            {
                plugin.reloadConfig();
                ConfigSettings.load(plugin.getConfig());

                sender.sendMessage("config reloaded from memory");
                plugin.getLogger().info("config reloaded");

                if (plugin.isCommunicating()) return true;
                if (plugin.startCommunication()) {
                    if (ConfigSettings.queueRememberDisconnected && !plugin.isListening()) plugin.startListen();
                    sender.sendMessage("communication started");
                } else {
                    sender.sendMessage("communication failed");
                }
                return true;
            }

            case "start":
            {
                if (plugin.isCommunicating()) {
                    sender.sendMessage("communication is already on");
                    return true;
                }

                if (plugin.startCommunication())
                {
                    if (ConfigSettings.queueRememberDisconnected && !plugin.isListening()) plugin.startListen();
                    sender.sendMessage("communication started");
                }
                else {
                    sender.sendMessage("communication failed");
                }
                return true;
            }

            case "stop":
            {
                if (!plugin.isCommunicating()) {
                    sender.sendMessage("communication is already off");
                    return true;
                }

                plugin.stopCommunication();
                if (ConfigSettings.queueRememberDisconnected && plugin.isListening()) plugin.stopListen();
                sender.sendMessage("communication stopped");
                sender.sendMessage("[/console2discord start] to start again");
                return true;
            }
        }

        sender.sendMessage("wrong argument");
        return true;
    }
}
