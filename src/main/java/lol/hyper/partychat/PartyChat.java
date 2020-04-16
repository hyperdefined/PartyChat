/**
 * PartyChat.java
 * Created on 4/15/2020
 * - hyperdefined
 */

package lol.hyper.partychat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class PartyChat extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getCommand("party").setExecutor(new CommandParty());
        this.getCommand("pc").setExecutor(new CommandPartyChatToggle());
        Bukkit.getServer().getPluginManager().registerEvents(new ChatEvent(), this);

        File partyFolder = new File("parties");
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