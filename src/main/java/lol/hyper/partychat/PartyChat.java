/*
  PartyChat.java
  Created on 4/15/2020
  - hyperdefined
 */

package lol.hyper.partychat;

import lol.hyper.partychat.tools.PartyManagement;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public final class PartyChat extends JavaPlugin {

    public final Path partyFolder = Paths.get(this.getDataFolder() + File.separator + "parties");

    public static final String MESSAGE_PREFIX = ChatColor.GREEN + "[Party] " + ChatColor.RESET;

    public final Logger logger = this.getLogger();

    public CommandParty commandParty;
    public CommandPartyChatMessage commandPartyChatMessage;
    public PartyManagement partyManagement;

    @Override
    public void onEnable() {
        partyManagement = new PartyManagement(this);
        commandParty = new CommandParty(this, partyManagement);
        commandPartyChatMessage = new CommandPartyChatMessage(this, partyManagement);
        this.getCommand("party").setExecutor(commandParty);
        this.getCommand("pc").setExecutor(commandPartyChatMessage);

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