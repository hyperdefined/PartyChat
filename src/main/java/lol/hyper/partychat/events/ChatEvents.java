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

package lol.hyper.partychat.events;

import lol.hyper.partychat.PartyChat;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatEvents implements Listener {

    private final PartyChat partyChat;

    public ChatEvents(PartyChat partyChat) {
        this.partyChat = partyChat;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (partyChat.partyManagement.lookupParty(player.getUniqueId()) == null) {
            return;
        }
        // player has party chat enabled
        if (partyChat.commandPartyChatMessage.partyChatEnabled.contains(player.getUniqueId())) {
            // cancel the chat message
            event.setCancelled(true);
            String playerMessage = String.join(" ", event.getMessage());
            Pattern greenTextPattern = Pattern.compile("^>(\\S*).*");
            Matcher greenTextMatcher = greenTextPattern.matcher(playerMessage);
            if (greenTextMatcher.find()) {
                playerMessage = ChatColor.GREEN + playerMessage;
            }

            String finalMessage = "<" + player.getName() + "> " + playerMessage;
            partyChat.partyManagement.sendPartyMessage(finalMessage, partyChat.partyManagement.lookupParty(player.getUniqueId()));
            partyChat.logger.info("[" + partyChat.partyManagement.lookupParty(player.getUniqueId()) + "] " + finalMessage);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // remove player if they leave
        partyChat.commandPartyChatMessage.partyChatEnabled.remove(player.getUniqueId());
    }
}
