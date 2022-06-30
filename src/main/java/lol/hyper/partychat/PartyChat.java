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

import lol.hyper.githubreleaseapi.GitHubRelease;
import lol.hyper.githubreleaseapi.GitHubReleaseAPI;
import lol.hyper.partychat.commands.CommandParty;
import lol.hyper.partychat.commands.CommandPartyChatMessage;
import lol.hyper.partychat.events.ChatEvents;
import lol.hyper.partychat.tools.PartyManagement;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public final class PartyChat extends JavaPlugin {

    public final Path partyFolder = Paths.get(this.getDataFolder() + File.separator + "parties");
    public final Logger logger = this.getLogger();
    public final File messagesFile = new File(this.getDataFolder(), "messages.yml");
    public FileConfiguration messages;
    public final int MESSAGES_VERSION = 1;

    public CommandParty commandParty;
    public CommandPartyChatMessage commandPartyChatMessage;
    public PartyManagement partyManagement;
    public ChatEvents chatEvents;

    public final MiniMessage miniMessage = MiniMessage.miniMessage();
    private BukkitAudiences adventure;

    @Override
    public void onEnable() {
        this.adventure = BukkitAudiences.create(this);
        partyManagement = new PartyManagement(this);
        commandParty = new CommandParty(this);
        commandPartyChatMessage = new CommandPartyChatMessage(this);
        chatEvents = new ChatEvents(this);
        this.getCommand("party").setExecutor(commandParty);
        this.getCommand("pc").setExecutor(commandPartyChatMessage);
        Bukkit.getPluginManager().registerEvents(chatEvents, this);

        new Metrics(this, 10306);

        if (!partyFolder.toFile().exists()) {
            if (!partyFolder.toFile().mkdirs()) {
                logger.severe("Unable to create the party folder " + partyFolder
                        + "! Please manually create this folder because things will break!");
            } else {
                logger.info("Creating parties folder for data storage.");
            }
        }

        Bukkit.getScheduler().runTaskAsynchronously(this, this::checkForUpdates);


        if (!messagesFile.exists()) {
            this.saveResource("messages.yml", true);
            logger.info("Copying default messages!");
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        if (messages.getInt("version") != MESSAGES_VERSION) {
            logger.warning("Your messages file is outdated! Please regenerate this file!.");
        }
    }

    public void checkForUpdates() {
        GitHubReleaseAPI api;
        try {
            api = new GitHubReleaseAPI("PartyChat", "hyperdefined");
        } catch (IOException e) {
            logger.warning("Unable to check updates!");
            e.printStackTrace();
            return;
        }
        GitHubRelease current = api.getReleaseByTag(this.getDescription().getVersion());
        GitHubRelease latest = api.getLatestVersion();
        if (current == null) {
            logger.warning("You are running a version that does not exist on GitHub. If you are in a dev environment, you can ignore this. Otherwise, this is a bug!");
            return;
        }
        int buildsBehind = api.getBuildsBehind(current);
        if (buildsBehind == 0) {
            logger.info("You are running the latest version.");
        } else {
            logger.warning("A new version is available (" + latest.getTagVersion() + ")! You are running version " + current.getTagVersion() + ". You are " + buildsBehind + " version(s) behind.");
        }
    }

    public BukkitAudiences getAdventure() {
        if(this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    /**
     * Gets a message from messages.yml.
     * @param path The path to the message.
     * @return Component with formatting applied.
     */
    public String getMessage(String path) {
        String message = messages.getString(path);
        if (message == null) {
            logger.warning(path + " is not a valid message!");
            return "<red>Invalid path! " + path + "</red>";
        }
        return message;
    }
}
