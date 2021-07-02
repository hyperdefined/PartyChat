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

package lol.hyper.partychat.commands;

import lol.hyper.partychat.PartyChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.Arrays;
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
            sender.sendMessage(PartyChat.MESSAGE_PREFIX + "You must be a player for this command.");
            return true;
        }
        UUID player = Bukkit.getPlayerExact(sender.getName()).getUniqueId();
        if (args.length < 1) {
            sender.sendMessage(PartyChat.MESSAGE_PREFIX + "Invalid syntax. Do /pc <message> instead.");
            return true;
        }
        if (partyChat.partyManagement.lookupParty(player) == null) {
            sender.sendMessage(PartyChat.MESSAGE_PREFIX + "You are not in a party. Do /party create to make one.");
        }

        String playerMessage = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Pattern greenTextPattern = Pattern.compile("^>(\\S*).*");
        Matcher greenTextMatcher = greenTextPattern.matcher(playerMessage);
        if (greenTextMatcher.find()) {
            playerMessage = ChatColor.GREEN + playerMessage;
        }

        String finaMlessage = "<" + Bukkit.getPlayer(player).getName() + "> " + playerMessage;
        partyChat.partyManagement.sendPartyMessage(finaMlessage, partyChat.partyManagement.lookupParty(player));
        partyChat.logger.info("[" + partyChat.partyManagement.lookupParty(player) + "] " + finaMlessage);
        return true;
    }
}
