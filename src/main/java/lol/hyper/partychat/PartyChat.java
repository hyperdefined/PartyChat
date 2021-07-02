/*
 * This file is part of PartyChat.
 *
 * PartyChat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PartyChat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PartyChat.  If not, see <https://www.gnu.org/licenses/>.
 */

package lol.hyper.partychat;

import lol.hyper.partychat.commands.CommandParty;
import lol.hyper.partychat.commands.CommandPartyChatMessage;
import lol.hyper.partychat.tools.PartyManagement;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public final class PartyChat extends JavaPlugin {

    public static final String MESSAGE_PREFIX = ChatColor.GREEN + "[Party] " + ChatColor.DARK_AQUA;
    public final Path partyFolder = Paths.get(this.getDataFolder() + File.separator + "parties");
    public final Logger logger = this.getLogger();

    public CommandParty commandParty;
    public CommandPartyChatMessage commandPartyChatMessage;
    public PartyManagement partyManagement;

    @Override
    public void onEnable() {
        partyManagement = new PartyManagement(this);
        commandParty = new CommandParty(this);
        commandPartyChatMessage = new CommandPartyChatMessage(this);
        this.getCommand("party").setExecutor(commandParty);
        this.getCommand("pc").setExecutor(commandPartyChatMessage);

        Metrics metrics = new Metrics(this, 10306);

        if (!partyFolder.toFile().exists()) {
            if (!partyFolder.toFile().mkdirs()) {
                logger.severe("Unable to create the party folder " + partyFolder
                        + "! Please manually create this folder because things will break!");
            } else {
                logger.info("Creating parties folder for data storage.");
            }
        }
    }

    /**
     * @param player player to check if vanished
     * @return returns if player is vanished or not
     */
    public boolean isVanished(String player) {
        if (Bukkit.getPlayerExact(player) == null) {
            return false;
        } else {
            Player player2 = Bukkit.getPlayerExact(player);
            assert player2 != null;
            for (MetadataValue meta : player2.getMetadata("vanished")) {
                if (meta.asBoolean()) return true;
            }
        }
        return false;
    }
}
