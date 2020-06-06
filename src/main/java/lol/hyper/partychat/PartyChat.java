/**
 * PartyChat.java
 * Created on 4/15/2020
 * - hyperdefined
 */

package lol.hyper.partychat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class PartyChat extends JavaPlugin {

    private static PartyChat instance;

    public static final File partyFolder = new File("DMC/parties");

    public static PartyChat getInstance() {
        return instance;
    }

    public static final String MESSAGE_PREFIX = ChatColor.DARK_AQUA + "[Party] " + ChatColor.RESET;

    @Override
    public void onEnable() {
        instance = this;
        this.getCommand("party").setExecutor(new CommandParty());
        this.getCommand("pc").setExecutor(new CommandPartyChatMessage());

        if (!partyFolder.exists()) {
            if (!partyFolder.mkdir()) {
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