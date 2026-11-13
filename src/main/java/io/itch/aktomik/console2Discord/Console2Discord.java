package io.itch.aktomik.console2Discord;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class Console2Discord extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().log(Level.FINE, "enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.FINE, "disabled!");
    }
}
