package lol.hyper.partychat;

import lol.hyper.partychat.tools.PartyManagement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class ChatEvent implements Listener {
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) throws IOException, ParseException {
        Player player = event.getPlayer();
        if (PartyManagement.partyChatEnabled.contains(player)) {
            if (PartyManagement.lookupParty(player) == null) {
                player.sendMessage(ChatColor.YELLOW + "It looks like you have party chat enabled, but you are not in a party. Please do /pc to talk again.");
            } else {
                String partyID = PartyManagement.lookupParty(player);
                String playerMessage = "[" + ChatColor.BLUE + "P" + ChatColor.RESET + "] " + "<" + player.getDisplayName() + "> " + event.getMessage();
                Bukkit.getLogger().info("[" + partyID + "] " + playerMessage);
                PartyManagement.sendPartyMessage(playerMessage, partyID);
            }
            event.setCancelled(true);
        }
    }
}
