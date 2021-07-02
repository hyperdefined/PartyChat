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
import lol.hyper.partychat.tools.UUIDLookup;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CommandParty implements TabExecutor {

    private final PartyChat partyChat;

    public CommandParty(PartyChat partyChat) {
        this.partyChat = partyChat;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || sender instanceof ConsoleCommandSender) {
            sender.sendMessage(ChatColor.GREEN + "PartyChat version "
                    + partyChat.getDescription().getVersion() + ". Created by hyperdefined.");
            sender.sendMessage(ChatColor.GREEN + "Use /party help for command help.");
            return true;
        }

        UUID commandSender = Bukkit.getPlayerExact(sender.getName()).getUniqueId();

        switch (args[0]) {
            case "help":
                sender.sendMessage(ChatColor.GOLD + "--------------------------------------------");
                sender.sendMessage(ChatColor.DARK_AQUA + "/party help - Shows this menu.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/party create - Make a new party.");
                sender.sendMessage(ChatColor.DARK_AQUA
                        + "/party invite <player> - Invite a player to the party. Party owner only.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/party accept/deny - Accept or deny an invite.");
                sender.sendMessage(
                        ChatColor.DARK_AQUA + "/party kick <player> - Kick a player from the party. Party owner only.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/party leave - Leave the party.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/party disband - Delete the party. Party owner only.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/party info - Information about the party.");
                sender.sendMessage(ChatColor.DARK_AQUA
                        + "/party transfer <player> - Transfer ownership of party. Party owner only.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/pc <message> - Send a message to the party.");
                sender.sendMessage(ChatColor.GOLD + "--------------------------------------------");
                break;
            case "invite": {
                if (args.length == 1 || args.length > 2) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED
                            + "Invalid syntax. Do /party invite <player> instead.");
                    return true;
                }
                if (partyChat.partyManagement.lookupParty(commandSender) == null) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED
                            + "You are not in a party. Do /party create to make one.");
                    return true;
                }
                if (partyChat.partyManagement.isPlayerOwner(commandSender)
                        || partyChat.partyManagement.checkTrusted(commandSender)) {
                    if (Bukkit.getPlayerExact(args[1]) == null) {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player was not found.");
                        return true;
                    }
                    Player playerToInvite = Bukkit.getPlayerExact(args[1]);
                    if (partyChat.partyManagement.pendingInvites.containsKey(playerToInvite.getUniqueId())) {
                        sender.sendMessage(
                                PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player already has a pending invite.");
                        return true;
                    }
                    if (partyChat.partyManagement.lookupParty(playerToInvite.getUniqueId()) != null) {
                        sender.sendMessage(
                                PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player is already in a party.");
                        return true;
                    }
                    String partyID = partyChat.partyManagement.lookupParty(commandSender);
                    partyChat.partyManagement.invitePlayer(playerToInvite.getUniqueId(), commandSender, partyID);
                    return true;
                }
                sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED
                        + "You cannot invite members to the party. The owner or any trusted members can.");
                return true;
            }
            case "create": {
                if (partyChat.partyManagement.lookupParty(commandSender) == null) {
                    partyChat.partyManagement.createParty(commandSender);
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.DARK_AQUA + "Party has been created.");
                } else {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You are already in a party.");
                }
                return true;
            }
            case "accept": {
                if (partyChat.partyManagement.pendingInvites.containsKey(commandSender)) {
                    String partyID = partyChat.partyManagement.pendingInvites.get(commandSender);
                    partyChat.partyManagement.removeInvite(commandSender, partyID, true);
                } else {
                    sender.sendMessage(
                            PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You do not have a pending invite right now.");
                }
                return true;
            }
            case "deny": {
                if (partyChat.partyManagement.pendingInvites.containsKey(commandSender)) {
                    String partyID = partyChat.partyManagement.pendingInvites.get(commandSender);
                    partyChat.partyManagement.removeInvite(commandSender, partyID, false);
                } else {
                    sender.sendMessage(
                            PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You do not have a pending invite right now.");
                }
                return true;
            }
            case "leave": {
                if (partyChat.partyManagement.lookupParty(commandSender) == null) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED
                            + "You are not in a party. Do /party create to make one.");
                    return true;
                }
                if (partyChat.partyManagement.isPlayerOwner(commandSender)) {
                    sender.sendMessage(
                            PartyChat.MESSAGE_PREFIX + ChatColor.RED
                                    + "You cannot leave as the owner. To delete the party, do /party disband. You can transfer the ownership with /party <transfer> <player>.");
                    return true;
                }
                Player playerLeaving = (Player) sender;
                String partyID = partyChat.partyManagement.lookupParty(playerLeaving.getUniqueId());
                partyChat.partyManagement.sendPartyMessage(
                        PartyChat.MESSAGE_PREFIX + ChatColor.DARK_AQUA + playerLeaving.getName()
                                + " has left the party.",
                        partyID);
                partyChat.partyManagement.removePlayerFromParty(commandSender, partyID);
                return true;
            }
            case "disband": {
                if (partyChat.partyManagement.lookupParty(commandSender) == null) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED
                            + "You are not in a party. Do /party create to make one.");
                    return true;
                }
                if (!partyChat.partyManagement.isPlayerOwner(commandSender)) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED
                            + "You aren't the owner of a party. Do /party leave instead.");
                    return true;
                }
                Player playerLeaving = (Player) sender;
                String partyID = partyChat.partyManagement.lookupParty(playerLeaving.getUniqueId());
                partyChat.partyManagement.sendPartyMessage(
                        PartyChat.MESSAGE_PREFIX + ChatColor.RED + "Party has been deleted.", partyID);
                partyChat.partyManagement.deleteParty(partyID);
                return true;
            }
            case "kick": {
                if (args.length == 1 || args.length > 2) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED
                            + "Invalid syntax. Do /party kick <player> instead.");
                    return true;
                }
                if (partyChat.partyManagement.lookupParty(commandSender) == null) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED
                            + "You are not in a party. Do /party create to make one.");
                    return true;
                }
                if (partyChat.partyManagement.isPlayerOwner(commandSender)
                        || partyChat.partyManagement.checkTrusted(commandSender)) {
                    if (Bukkit.getPlayerExact(args[1]) == null) {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player was not found.");
                        return true;
                    }
                    if (Bukkit.getPlayerExact(args[1]) == null) {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player was not found.");
                        return true;
                    }
                    Player playerToKick = Bukkit.getPlayerExact(args[1]);
                    String partyIDKickingPlayer = partyChat.partyManagement.lookupParty(playerToKick.getUniqueId());
                    String partyID = partyChat.partyManagement.lookupParty(commandSender);
                    if (!partyID.equals(partyIDKickingPlayer)) {
                        sender.sendMessage(
                                PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player is not in your party.");
                        return true;
                    }
                    partyChat.partyManagement.sendPartyMessage(
                            PartyChat.MESSAGE_PREFIX + ChatColor.RED + playerToKick.getName()
                                    + " has been kicked from the party.",
                            partyID);
                    partyChat.partyManagement.removePlayerFromParty(playerToKick.getUniqueId(), partyID);
                    return true;
                }
                sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED
                        + "You cannot kick members from the party. The owner or any trusted members can.");
                return true;
            }
            case "transfer": {
                if (args.length == 1 || args.length > 2) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED
                            + "Invalid syntax. Do /party transfer <player> instead.");
                    return true;
                }
                if (partyChat.partyManagement.lookupParty(commandSender) == null) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED
                            + "You are not in a party. Do /party create to make one.");
                    return true;
                }
                if (partyChat.partyManagement.isPlayerOwner(commandSender)) {
                    if (Bukkit.getPlayerExact(args[1]) == null) {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player was not found.");
                        return true;
                    }
                    Player newOwner = Bukkit.getPlayerExact(args[1]);
                    String partyID = partyChat.partyManagement.lookupParty(commandSender);
                    partyChat.partyManagement.sendPartyMessage(
                            PartyChat.MESSAGE_PREFIX + ChatColor.DARK_AQUA + newOwner.getName()
                                    + " is now the owner of the party.",
                            partyID);
                    partyChat.partyManagement.updatePartyOwner(newOwner.getUniqueId(), partyID);
                    return true;
                }
                sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED
                        + "You cannot transfer ownership. Only the party owner can.");
                return true;
            }
            case "info": {
                if (partyChat.partyManagement.lookupParty(commandSender) == null) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED
                            + "You are not in a party. Do /party create to make one.");
                    return true;
                }
                Bukkit.getPlayer(commandSender)
                        .sendMessage(ChatColor.GOLD + "--------------------------------------------");
                Bukkit.getPlayer(commandSender)
                        .sendMessage(ChatColor.DARK_AQUA + "Members: " + ChatColor.DARK_AQUA
                                + partyChat
                                        .partyManagement
                                        .listPartyMembers(partyChat.partyManagement.lookupParty(commandSender))
                                        .size()
                                + " - ID: " + partyChat.partyManagement.lookupParty(commandSender));
                ArrayList<UUID> players = partyChat.partyManagement.listPartyMembers(
                        partyChat.partyManagement.lookupParty(commandSender));
                ArrayList<String> convertedPlayerNames = new ArrayList<>();
                UUID partyOwner =
                        partyChat.partyManagement.lookupOwner(partyChat.partyManagement.lookupParty(commandSender));
                Bukkit.getScheduler().runTaskAsynchronously(partyChat, () -> {
                    for (UUID tempPlayer : players) {
                        if (tempPlayer.equals(partyOwner)) {
                            convertedPlayerNames.add(UUIDLookup.getName(tempPlayer) + " (Owner)");
                        } else if (partyChat.partyManagement.checkTrusted(tempPlayer)) {
                            convertedPlayerNames.add(UUIDLookup.getName(tempPlayer) + " (Trusted)");
                        } else {
                            convertedPlayerNames.add(UUIDLookup.getName(tempPlayer));
                        }
                    }
                    for (String tempPlayer : convertedPlayerNames) {
                        Bukkit.getPlayer(commandSender).sendMessage(ChatColor.DARK_AQUA + tempPlayer);
                    }
                    Bukkit.getPlayer(commandSender)
                            .sendMessage(ChatColor.GOLD + "--------------------------------------------");
                });
            }
            case "trust": {
                if (args.length == 1 || args.length > 2) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED
                            + "Invalid syntax. Do /party transfer <player> instead.");
                    return true;
                }
                if (partyChat.partyManagement.lookupParty(commandSender) == null) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED
                            + "You are not in a party. Do /party create to make one.");
                    return true;
                }
                if (partyChat.partyManagement.isPlayerOwner(commandSender)) {
                    if (Bukkit.getPlayerExact(args[1]) == null) {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player was not found.");
                        return true;
                    }
                    Player memberToTrust = Bukkit.getPlayerExact(args[1]);
                    if (commandSender.equals(memberToTrust.getUniqueId())) {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED
                                + "You cannot add yourself as a trusted member, you are the party owner.");
                        return true;
                    }
                    if (partyChat.partyManagement.checkTrusted(memberToTrust.getUniqueId())) {
                        sender.sendMessage(
                                PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player is already trusted.");
                        return true;
                    }
                    partyChat.partyManagement.trustPlayer(memberToTrust.getUniqueId());
                    sender.sendMessage(
                            PartyChat.MESSAGE_PREFIX + ChatColor.GREEN + "That player was added as a trusted member.");
                    return true;
                }
                sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED
                        + "You are not the owner of the party, Only the owner can trust members.");
                return true;
            }
            case "untrust": {
                if (args.length == 1 || args.length > 2) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED
                            + "Invalid syntax. Do /party transfer <player> instead.");
                    return true;
                }
                if (partyChat.partyManagement.lookupParty(commandSender) == null) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED
                            + "You are not in a party. Do /party create to make one.");
                    return true;
                }
                if (partyChat.partyManagement.isPlayerOwner(commandSender)) {
                    if (Bukkit.getPlayerExact(args[1]) == null) {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player was not found.");
                        return true;
                    }
                    Player memberToTrust = Bukkit.getPlayerExact(args[1]);
                    if (commandSender.equals(memberToTrust.getUniqueId())) {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED
                                + "You cannot remove yourself as a trusted player, you are the party owner.");
                        return true;
                    }
                    if (!partyChat.partyManagement.checkTrusted(memberToTrust.getUniqueId())) {
                        sender.sendMessage(
                                PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player is already not trusted.");
                        return true;
                    }
                    partyChat.partyManagement.removeTrustedPlayer(memberToTrust.getUniqueId());
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.GREEN
                            + "That player was removed as a trusted member.");
                    return true;
                }
                sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED
                        + "You are not the owner of the party, Only the owner can trust members.");
                return true;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            return Arrays.asList(
                    "create",
                    "invite",
                    "accept",
                    "deny",
                    "kick",
                    "leave",
                    "disband",
                    "info",
                    "transfer",
                    "help",
                    "trust",
                    "untrust");
        } else {
            return null;
        }
    }
}
