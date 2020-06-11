/**
 * CommandParty.java
 * Created on 4/15/2020
 * - hyperdefined
 */

package lol.hyper.partychat;

import lol.hyper.partychat.tools.PartyManagement;
import lol.hyper.partychat.tools.UUIDLookup;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class CommandParty implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        UUID commandSender = Bukkit.getPlayer(sender.getName()).getUniqueId();
        if (args.length == 0) {
            sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "Invalid syntax. Do /party help for commands.");
        } else if (args[0].equalsIgnoreCase("create")) {
            if (args.length > 1) {
                sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "Invalid syntax. Do /party create instead.");
            } else {
                try {
                    if (PartyManagement.lookupParty(commandSender) == null) {
                        PartyManagement.createParty(commandSender);
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.DARK_AQUA + "Party has been created.");
                    } else {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You are already in a party.");
                    }

                } catch (IOException | ParseException e) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "There was an issue creating the party. Please contact hyperdefined.");
                    Bukkit.getLogger().warning(e.getMessage());
                }
            }
        } else if (args[0].equalsIgnoreCase("invite")) {
            if (args.length == 1 || args.length > 2) {
                sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "Invalid syntax. Do /party invite <player> instead.");
            } else {
                try {
                    if (PartyManagement.lookupParty(commandSender) == null) {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You are not in a party. Do /party create to make one.");
                    } else if (PartyManagement.isPlayerOwner(commandSender)) {
                        if (Bukkit.getPlayer(args[1]) != null) {
                            UUID inviteReceiver = Bukkit.getPlayerExact(args[1]).getUniqueId();
                            if (PartyManagement.pendingInvites.containsKey(inviteReceiver)) {
                                sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player already has a pending invite.");
                            } else {
                                try {
                                    if (PartyManagement.lookupParty(inviteReceiver) != null) {
                                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player is already in a party.");
                                    } else {
                                        String partyID = PartyManagement.lookupParty(commandSender);
                                        PartyManagement.invitePlayer(inviteReceiver, commandSender, partyID);
                                    }
                                } catch (IOException | ParseException e) {
                                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "There was an issue with the party file. Please contact hyperdefined.");
                                    Bukkit.getLogger().warning(e.getMessage());
                                }
                            }
                        } else {
                            sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player was not found.");
                        }
                    } else {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You cannot invite members to the party. Only the party owner can.");
                    }
                } catch (IOException | ParseException e) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "There was an issue with the party file. Please contact hyperdefined.");
                    Bukkit.getLogger().warning(e.getMessage());
                }
            }
        } else if (args[0].equalsIgnoreCase("accept")) {
            if (args.length > 1) {
                sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "Invalid syntax. Do /party accept instead.");
            } else {
                if (PartyManagement.pendingInvites.containsKey(commandSender)) {
                    String partyID = PartyManagement.pendingInvites.get(commandSender);
                    try {
                        PartyManagement.removeInvite(commandSender, partyID, true);
                    } catch (IOException | ParseException e) {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "There was an issue with the party file. Please contact hyperdefined.");
                        Bukkit.getLogger().warning(e.getMessage());
                    }
                } else {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You do not have a pending invite right now.");
                }
            }
        } else if (args[0].equalsIgnoreCase("deny")) {
            if (args.length > 1) {
                sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "Invalid syntax. Do /party deny instead.");
            } else {
                if (PartyManagement.pendingInvites.containsKey(commandSender)) {
                    String partyID = PartyManagement.pendingInvites.get(commandSender);
                    try {
                        PartyManagement.removeInvite(commandSender, partyID, false);
                    } catch (IOException | ParseException e) {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "There was an issue with the party file. Please contact hyperdefined.");
                        Bukkit.getLogger().warning(e.getMessage());
                    }
                } else {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You do not have a pending invite right now.");
                }
            }

        } else if (args[0].equalsIgnoreCase("leave")) {
            if (args.length > 1) {
                sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "Invalid syntax. Do /party leave instead.");
            } else {
                try {
                    if (PartyManagement.lookupParty(commandSender) == null) {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You are not in a party. Do /party create to make one.");
                    } else if (PartyManagement.isPlayerOwner(commandSender)) {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You cannot leave as the owner. To delete the party, do /party disband. You can transfer the ownership with /party <transfer> <player>.");
                    } else {
                        PartyManagement.sendPartyMessage(PartyChat.MESSAGE_PREFIX + ChatColor.DARK_AQUA + sender.getName() + " has left the party.", PartyManagement.lookupParty(commandSender));
                        PartyManagement.removePlayerFromParty(commandSender, PartyManagement.lookupParty(commandSender));
                    }
                } catch (IOException | ParseException e) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "There was an issue with the party file. Please contact hyperdefined.");
                    Bukkit.getLogger().warning(e.getMessage());
                }
            }
        } else if (args[0].equalsIgnoreCase("disband")) {
            if (args.length > 1) {
                sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "Invalid syntax. Do /party disband instead.");
            } else {
                try {
                    if (PartyManagement.lookupParty(commandSender) == null) {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You are not in a party. Do /party create to make one.");
                    } else if (PartyManagement.isPlayerOwner(commandSender)) {
                        PartyManagement.sendPartyMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "Party has been deleted.", PartyManagement.lookupParty(commandSender));
                        PartyManagement.deleteParty(PartyManagement.lookupParty(commandSender));
                    } else {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You aren't the owner of a party. Do /party leave instead.");
                    }
                } catch (IOException | ParseException e) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "There was an issue with the party file. Please contact hyperdefined.");
                    Bukkit.getLogger().warning(e.getMessage());
                }
            }
        } else if (args[0].equalsIgnoreCase("kick")) {
            if (args.length == 1 || args.length > 2) {
                sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "Invalid syntax. Do /party kick <player> instead.");
            } else {
                try {
                    if (PartyManagement.lookupParty(commandSender) == null) {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You are not in a party. Do /party create to make one.");
                    } else if (PartyManagement.isPlayerOwner(commandSender)) {
                        if (Bukkit.getPlayer(args[1]) != null) {
                            UUID kickedPlayer = Bukkit.getPlayerExact(args[1]).getUniqueId();
                            String partyPlayerKicked = PartyManagement.lookupParty(kickedPlayer);
                            String partyPlayerSender = PartyManagement.lookupOwner(PartyManagement.lookupParty(commandSender)).toString();
                            if (partyPlayerSender.equals(partyPlayerKicked)) {
                                sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player is not in your party.");
                            } else {
                                PartyManagement.sendPartyMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + Bukkit.getPlayer(kickedPlayer).getName() + " has been kicked from the party.", PartyManagement.lookupParty(kickedPlayer));
                                PartyManagement.removePlayerFromParty(kickedPlayer, PartyManagement.lookupParty(kickedPlayer));
                            }
                        } else {
                            sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player was not found.");
                        }
                    } else {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You cannot kick members from the party. Only the party owner can.");
                    }
                } catch (IOException | ParseException e) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "There was an issue with the party file. Please contact hyperdefined.");
                    Bukkit.getLogger().warning(e.getMessage());
                }
            }
        } else if (args[0].equalsIgnoreCase("transfer")) {
            if (args.length == 1 || args.length > 2) {
                sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "Invalid syntax. Do /party transfer <player> instead.");
            } else {
                try {
                    if (PartyManagement.lookupParty(commandSender) == null) {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You are not in a party. Do /party create to make one.");
                    } else if (PartyManagement.isPlayerOwner(commandSender)) {
                        if (Bukkit.getPlayer(args[1]) != null) {
                            UUID newOwner = Bukkit.getPlayerExact(args[1]).getUniqueId();
                            String partyPlayerKicked = PartyManagement.lookupParty(newOwner);
                            String partyPlayerSender = PartyManagement.lookupOwner(PartyManagement.lookupParty(commandSender)).toString();
                            if (partyPlayerSender.equals(partyPlayerKicked)) {
                                sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player is not in your party.");
                            } else {
                                PartyManagement.sendPartyMessage(PartyChat.MESSAGE_PREFIX + ChatColor.DARK_AQUA + Bukkit.getPlayer(newOwner).getName() + " is now the owner of the party.", PartyManagement.lookupParty(newOwner));
                                PartyManagement.updatePartyOwner(newOwner, PartyManagement.lookupParty(newOwner));
                            }
                        } else {
                            sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "That player was not found.");
                        }
                    } else {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You cannot transfer ownership. Only the party owner can.");
                    }
                } catch (IOException | ParseException e) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "There was an issue with the party file. Please contact hyperdefined.");
                    Bukkit.getLogger().warning(e.getMessage());
                }
            }
        } else if (args[0].equalsIgnoreCase("info")) {
            if (args.length > 1) {
                sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.DARK_AQUA + "Invalid syntax. Do /party info instead.");
            } else {
                try {
                    if (PartyManagement.lookupParty(commandSender) == null) {
                        sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You are not in a party. Do /party create to make one.");
                    } else {
                        Bukkit.getPlayer(commandSender).sendMessage(ChatColor.GOLD + "--------------------------------------------");
                        Bukkit.getPlayer(commandSender).sendMessage(ChatColor.DARK_AQUA + "Members: " + ChatColor.DARK_AQUA + PartyManagement.listPartyMembers(PartyManagement.lookupParty(commandSender)).size() + " - ID: " + PartyManagement.lookupParty(commandSender));
                        ArrayList<UUID> players = PartyManagement.listPartyMembers(PartyManagement.lookupParty(commandSender));
                        ArrayList<String> convertedPlayerNames = new ArrayList<>();
                        UUID partyOwner = PartyManagement.lookupOwner(PartyManagement.lookupParty(commandSender));
                        Bukkit.getScheduler().runTaskAsynchronously(PartyChat.getInstance(), () -> {
                            for (UUID tempPlayer : players) {
                                if (!tempPlayer.equals(partyOwner)) {
                                    convertedPlayerNames.add(UUIDLookup.getName(tempPlayer));
                                } else {
                                    convertedPlayerNames.add(UUIDLookup.getName(tempPlayer) + " (Owner)");
                                }
                            }
                            for (String tempPlayer : convertedPlayerNames) {
                                Bukkit.getPlayer(commandSender).sendMessage(ChatColor.DARK_AQUA + tempPlayer);
                            }
                            Bukkit.getPlayer(commandSender).sendMessage(ChatColor.GOLD + "--------------------------------------------");
                        });
                    }
                } catch (IOException | ParseException e) {
                    sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "There was an issue with the party file. Please contact hyperdefined.");
                    Bukkit.getLogger().warning(e.getMessage());
                }
            }
        } else if (args[0].equalsIgnoreCase("help")) {
            if (args.length > 1) {
                sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "Invalid syntax. Do /party help instead.");
            } else {
                sender.sendMessage(ChatColor.GOLD + "--------------------------------------------");
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
            }
        } else {
            sender.sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "Invalid command. Do /party help instead.");
        }
        return true;
    }
}