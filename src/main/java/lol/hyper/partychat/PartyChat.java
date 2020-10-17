/*
  PartyChat.java
  Created on 4/15/2020
  - hyperdefined
 */

package lol.hyper.partychat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class PartyChat extends JavaPlugin {

    private static PartyChat instance;

    public final Path partyFolder = Paths.get(this.getDataFolder() + File.separator + "parties");

    public static PartyChat getInstance() {
        return instance;
    }

    public static final String MESSAGE_PREFIX = ChatColor.GREEN + "[Party] " + ChatColor.RESET;

    @Override
    public void onEnable() {
        instance = this;
        this.getCommand("party").setExecutor(new CommandParty());
        this.getCommand("pc").setExecutor(new CommandPartyChatMessage());

        if (!partyFolder.toFile().exists()) {
            if (!partyFolder.toFile().mkdirs()) {
                Bukkit.getLogger().warning("Unable to create parties folder! Please create the folder!");
            } else {
                Bukkit.getLogger().info("Creating parties folder.");
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}