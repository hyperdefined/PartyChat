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
import java.util.logging.Logger;

public final class PartyChat extends JavaPlugin {

    private static PartyChat instance;

    public final Path partyFolder = Paths.get(this.getDataFolder() + File.separator + "parties");

    public static PartyChat getInstance() {
        return instance;
    }

    public static final String MESSAGE_PREFIX = ChatColor.GREEN + "[Party] " + ChatColor.RESET;

    public Logger logger = this.getLogger();

    @Override
    public void onEnable() {
        instance = this;
        this.getCommand("party").setExecutor(new CommandParty());
        this.getCommand("pc").setExecutor(new CommandPartyChatMessage());

        if (!partyFolder.toFile().exists()) {
            if (!partyFolder.toFile().mkdirs()) {
                logger.severe("Unable to create the party folder " + partyFolder + "! Please manually create this folder because things will break!");
            } else {
                logger.info("Creating parties folder for data storage.");
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}