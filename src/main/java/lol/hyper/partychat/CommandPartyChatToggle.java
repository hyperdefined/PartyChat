package lol.hyper.partychat;

import lol.hyper.partychat.tools.PartyManagement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandPartyChatToggle implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = Bukkit.getPlayerExact(sender.getName());
        if (args.length > 0) {
            sender.sendMessage(ChatColor.RED + "Invalid syntax. Do /pc instead.");
        } else {
            if (PartyManagement.partyChatEnabled.contains(player)) {
                PartyManagement.partyChatEnabled.remove(player);
                sender.sendMessage(ChatColor.GREEN + "Party chat is now off.");
            } else {
                PartyManagement.partyChatEnabled.add(player);
                sender.sendMessage(ChatColor.GREEN + "Party chat is now on.");
            }
        }
        return true;
    }
}
