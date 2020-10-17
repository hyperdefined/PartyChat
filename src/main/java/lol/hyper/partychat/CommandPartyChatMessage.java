/*
  CommandParty.java
  Created on 4/15/2020
  - hyperdefined
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
            if (PartyManagement.lookupParty(player) != null) {
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
                PartyManagement.sendPartyMessage(playerMessage, PartyManagement.lookupParty(player));
                PartyChat.getInstance().logger.info("[" + PartyManagement.lookupParty(player) + "] " + playerMessage);
            } else {
                sender.sendMessage(PartyChat.MESSAGE_PREFIX+ ChatColor.RED + "You are not in a party. Do /party create to make one.");
            }

        }
        return true;
    }
}
