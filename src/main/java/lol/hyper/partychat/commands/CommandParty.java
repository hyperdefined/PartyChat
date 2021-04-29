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
            sender.sendMessage(ChatColor.GREEN + "PartyChat version " + partyChat.getDescription().getVersion() + ". Created by hyperdefined.");
            sender.sendMessage(ChatColor.GREEN + "Use /party help for command help.");
            return true;
        }

        UUID commandSender = Bukkit.getPlayerExact(sender.getName()).getUniqueId();

        switch (args[0]) {
            case "help":
                sender.sendMessage(ChatColor.GOLD + "--------------------------------------------");
                sender.sendMessage(ChatColor.DARK_AQUA + "/party help - Shows this menu.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/party create - Make a new party.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/party invite <player> - Invite a player to the party. Party owner only.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/party accept/deny - Accept or deny an invite.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/party kick <player> - Kick a player from the party. Party owner only.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/party leave - Leave the party.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/party disband - Delete the party. Party owner only.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/party info - Information about the party.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/party transfer <player> - Transfer ownership of party. Party owner only.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/pc <message> - Send a message to the party.");
                sender.sendMessage(ChatColor.GOLD + "--------------------------------------------");
                break;
            case "invite":
                if (args.length == 1 || args.length > 2) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "Invalid syntax. Do /party invite <player> instead.");
                } else {
                    if (partyChat.partyManagement.lookupParty(commandSender) == null) {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You are not in a party. Do /party create to make one.");
                    } else if (partyChat.partyManagement.isPlayerOwner(commandSender)) {
                        if (Bukkit.getPlayerExact(args[1]) != null && !partyChat.isVanished(args[1])) {
                            UUID inviteReceiver = Bukkit.getPlayerExact(args[1]).getUniqueId();
                            if (partyChat.partyManagement.pendingInvites.containsKey(inviteReceiver)) {
                                sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player already has a pending invite.");
                            } else {
                                if (partyChat.partyManagement.lookupParty(inviteReceiver) != null) {
                                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player is already in a party.");
                                } else {
                                    String partyID = partyChat.partyManagement.lookupParty(commandSender);
                                    partyChat.partyManagement.invitePlayer(inviteReceiver, commandSender, partyID);
                                }
                            }
                        } else {
                            sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player was not found.");
                        }
                    } else {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You cannot invite members to the party. Only the party owner can.");
                    }
                }
                break;
            case "create":
                if (partyChat.partyManagement.lookupParty(commandSender) == null) {
                    partyChat.partyManagement.createParty(commandSender);
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.DARK_AQUA + "Party has been created.");
                } else {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You are already in a party.");
                }
                break;
            case "accept":
                if (partyChat.partyManagement.pendingInvites.containsKey(commandSender)) {
                    String partyID = partyChat.partyManagement.pendingInvites.get(commandSender);
                    partyChat.partyManagement.removeInvite(commandSender, partyID, true);
                } else {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You do not have a pending invite right now.");
                }
                break;
            case "deny":
                if (partyChat.partyManagement.pendingInvites.containsKey(commandSender)) {
                    String partyID = partyChat.partyManagement.pendingInvites.get(commandSender);
                    partyChat.partyManagement.removeInvite(commandSender, partyID, false);
                } else {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You do not have a pending invite right now.");
                }
                break;
            case "leave":
                if (partyChat.partyManagement.lookupParty(commandSender) == null) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You are not in a party. Do /party create to make one.");
                } else if (partyChat.partyManagement.isPlayerOwner(commandSender)) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You cannot leave as the owner. To delete the party, do /party disband. You can transfer the ownership with /party <transfer> <player>.");
                } else {
                    partyChat.partyManagement.sendPartyMessage(PartyChat.MESSAGE_PREFIX + ChatColor.DARK_AQUA + sender.getName() + " has left the party.", partyChat.partyManagement.lookupParty(commandSender));
                    partyChat.partyManagement.removePlayerFromParty(commandSender, partyChat.partyManagement.lookupParty(commandSender));
                }
                break;
            case "disband":
                if (partyChat.partyManagement.lookupParty(commandSender) == null) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You are not in a party. Do /party create to make one.");
                } else if (partyChat.partyManagement.isPlayerOwner(commandSender)) {
                    partyChat.partyManagement.sendPartyMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "Party has been deleted.", partyChat.partyManagement.lookupParty(commandSender));
                    partyChat.partyManagement.deleteParty(partyChat.partyManagement.lookupParty(commandSender));
                } else {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You aren't the owner of a party. Do /party leave instead.");
                }
                break;
            case "kick":
                if (args.length == 1 || args.length > 2) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "Invalid syntax. Do /party kick <player> instead.");
                } else {
                    if (partyChat.partyManagement.lookupParty(commandSender) == null) {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You are not in a party. Do /party create to make one.");
                    } else if (partyChat.partyManagement.isPlayerOwner(commandSender)) {
                        if (Bukkit.getPlayerExact(args[1]) != null && !partyChat.isVanished(args[1])) {
                            UUID kickedPlayer = Bukkit.getPlayerExact(args[1]).getUniqueId();
                            String partyPlayerKicked = partyChat.partyManagement.lookupParty(kickedPlayer);
                            String partyPlayerSender = partyChat.partyManagement.lookupOwner(partyChat.partyManagement.lookupParty(commandSender)).toString();
                            if (partyPlayerSender.equals(partyPlayerKicked)) {
                                sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player is not in your party.");
                            } else if (!commandSender.equals(partyChat.partyManagement.lookupOwner(partyPlayerSender))) {
                                partyChat.partyManagement.sendPartyMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + Bukkit.getPlayer(kickedPlayer).getName() + " has been kicked from the party.", partyChat.partyManagement.lookupParty(kickedPlayer));
                                partyChat.partyManagement.removePlayerFromParty(kickedPlayer, partyChat.partyManagement.lookupParty(kickedPlayer));
                            } else {
                                sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You cannot kick yourself from the party.");
                            }
                        } else {
                            sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player was not found.");
                        }
                    } else {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You cannot kick members from the party. Only the party owner can.");
                    }
                }
                break;
            case "transfer":
                if (args.length == 1 || args.length > 2) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "Invalid syntax. Do /party transfer <player> instead.");
                } else {
                    if (partyChat.partyManagement.lookupParty(commandSender) == null) {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You are not in a party. Do /party create to make one.");
                    } else if (partyChat.partyManagement.isPlayerOwner(commandSender)) {
                        if (Bukkit.getPlayerExact(args[1]) != null) {
                            UUID newOwner = Bukkit.getPlayerExact(args[1]).getUniqueId();
                            String partyPlayerKicked = partyChat.partyManagement.lookupParty(newOwner);
                            String partyPlayerSender = partyChat.partyManagement.lookupOwner(partyChat.partyManagement.lookupParty(commandSender)).toString();
                            if (partyPlayerSender.equals(partyPlayerKicked)) {
                                sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player is not in your party.");
                            } else {
                                partyChat.partyManagement.sendPartyMessage(PartyChat.MESSAGE_PREFIX + ChatColor.DARK_AQUA + Bukkit.getPlayer(newOwner).getName() + " is now the owner of the party.", partyChat.partyManagement.lookupParty(newOwner));
                                partyChat.partyManagement.updatePartyOwner(newOwner, partyChat.partyManagement.lookupParty(newOwner));
                            }
                        } else {
                            sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player was not found.");
                        }
                    } else {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You cannot transfer ownership. Only the party owner can.");
                    }
                }
                break;
            case "info":
                if (partyChat.partyManagement.lookupParty(commandSender) == null) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You are not in a party. Do /party create to make one.");
                } else {
                    Bukkit.getPlayer(commandSender).sendMessage(ChatColor.GOLD + "--------------------------------------------");
                    Bukkit.getPlayer(commandSender).sendMessage(ChatColor.DARK_AQUA + "Members: " + ChatColor.DARK_AQUA + partyChat.partyManagement.listPartyMembers(partyChat.partyManagement.lookupParty(commandSender)).size() + " - ID: " + partyChat.partyManagement.lookupParty(commandSender));
                    ArrayList < UUID > players = partyChat.partyManagement.listPartyMembers(partyChat.partyManagement.lookupParty(commandSender));
                    ArrayList < String > convertedPlayerNames = new ArrayList < > ();
                    UUID partyOwner = partyChat.partyManagement.lookupOwner(partyChat.partyManagement.lookupParty(commandSender));
                    Bukkit.getScheduler().runTaskAsynchronously(partyChat, () -> {
                    for (UUID tempPlayer: players) {
                        if (!tempPlayer.equals(partyOwner)) {
                            convertedPlayerNames.add(UUIDLookup.getName(tempPlayer));
                        } else {
                            convertedPlayerNames.add(UUIDLookup.getName(tempPlayer) + " (Owner)");
                        }
                    }
                    for (String tempPlayer: convertedPlayerNames) {
                        Bukkit.getPlayer(commandSender).sendMessage(ChatColor.DARK_AQUA + tempPlayer);
                    }
                    Bukkit.getPlayer(commandSender).sendMessage(ChatColor.GOLD + "--------------------------------------------");
                    });
                }
                break;
        }
        return true;
    }

    @Override
    public List < String > onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            return Arrays.asList("create", "invite", "accept", "deny", "kick", "leave", "disband", "info", "transfer", "help");
        } else {
            return null;
        }
    }
}