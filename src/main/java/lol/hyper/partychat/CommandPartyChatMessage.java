package lol.hyper.partychat;

import lol.hyper.partychat.tools.PartyManagement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class CommandPartyChatMessage implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = Bukkit.getPlayerExact(sender.getName());
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Invalid syntax. Do /pc <message> instead.");
        } else {
            try {
                if (PartyManagement.lookupParty(player) != null) {
                    StringBuilder str = new StringBuilder();
                    for (String x : args) {
                        str.append(x + " ");
                    }
                    String playerMessage = "[" + ChatColor.BLUE + "P" + ChatColor.RESET + "] " + "<" + player.getDisplayName() + "> " + str.toString();
                    PartyManagement.sendPartyMessage(playerMessage, PartyManagement.lookupParty(player));
                    Bukkit.getLogger().info("[" + PartyManagement.lookupParty(player) + "] " + playerMessage);
                } else {
                    sender.sendMessage(ChatColor.RED + "You are not in a party! Do /party create to make one!");
                }
            } catch (IOException | ParseException e) {
                e.printStackTrace();
                sender.sendMessage(ChatColor.RED + "There was an issue with the party file. Please contact hyperdefined.");
            }

        }
        return true;
    }
}
