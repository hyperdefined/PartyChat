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

import lol.hyper.partychat.tools.PartyManagement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandPartyChatMessage implements CommandExecutor {

    private final PartyChat partyChat;

    public CommandPartyChatMessage(PartyChat partyChat) {
        this.partyChat = partyChat;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(ChatColor.RED + "You must be a player for this command.");
            return true;
        }
        UUID player = Bukkit.getPlayerExact(sender.getName()).getUniqueId();
        if (args.length < 1) {
            sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "Invalid syntax. Do /pc <message> instead.");
        } else {
            if (partyChat.partyManagement.lookupParty(player) != null) {
                StringBuilder str = new StringBuilder();
                for (String x : args) {
                    str.append(x).append(" ");
                }

                Pattern greenTextPattern = Pattern.compile("^>(\\S*).*");
                Matcher greenTextMatcher = greenTextPattern.matcher(str.toString());
                if (greenTextMatcher.find()) {
                    str.insert(0, ChatColor.GREEN);
                }

                String playerMessage = PartyChat.MESSAGE_PREFIX + "<" + Bukkit.getPlayer(player).getName() + "> " + str.toString();
                partyChat.partyManagement.sendPartyMessage(playerMessage, partyChat.partyManagement.lookupParty(player));
                partyChat.logger.info("[" + partyChat.partyManagement.lookupParty(player) + "] " + playerMessage);
            } else {
                sender.sendMessage(PartyChat.MESSAGE_PREFIX+ ChatColor.RED + "You are not in a party. Do /party create to make one.");
            }
        }
        return true;
    }
}
