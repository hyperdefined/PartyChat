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
import org.bukkit.entity.Player;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;

public class CommandParty implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Invalid syntax. Do /party help for commands.");
        } else if (args[0].equalsIgnoreCase("create")) {
            if (args.length > 1) {
                sender.sendMessage(ChatColor.RED + "Invalid syntax. Do /party create instead.");
            } else {
                try {
                    if (PartyManagement.lookupParty(Bukkit.getPlayer(sender.getName())) == null) {
                        PartyManagement.createParty(Bukkit.getPlayer(sender.getName()));
                        sender.sendMessage(ChatColor.GREEN + "Party has been created!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "You are already in a party!");
                    }

                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED + "There was an issue creating the party.");
                }
            }
        } else if (args[0].equalsIgnoreCase("invite")) {
            if (args.length == 1 || args.length > 2) {
                sender.sendMessage(ChatColor.RED + "Invalid syntax. Do /party invite <player> instead.");
            } else {
                Player player = Bukkit.getPlayer(sender.getName());
                try {
                    if (PartyManagement.lookupParty(player) == null) {
                        sender.sendMessage(ChatColor.RED + "You are not in a party! Do /party create to make one!");
                    } else if (PartyManagement.isPlayerOwner(player)) {
                        if (Bukkit.getPlayer(args[1]) != null) {
                            Player inviteReceiver = Bukkit.getPlayerExact(args[1]);
                            Player inviteSender = Bukkit.getPlayerExact(sender.getName());
                            if (PartyManagement.pendingInvites.containsKey(inviteReceiver)) {
                                sender.sendMessage(ChatColor.RED + "That player already has a pending invite.");
                            } else {
                                try {
                                    if (PartyManagement.lookupParty(inviteReceiver) != null) {
                                        sender.sendMessage(ChatColor.RED + "That player is already in a party!");
                                    } else {
                                        String partyID = PartyManagement.lookupParty(inviteSender);
                                        PartyManagement.invitePlayer(inviteReceiver, inviteSender, partyID);
                                    }
                                } catch (IOException | ParseException e) {
                                    e.printStackTrace();
                                    sender.sendMessage(ChatColor.RED + "There was an issue with the party file. Please contact hyperdefined.");
                                }
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Invalid player.");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You cannot invite members to the party. Only owners can.");
                    }
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED + "There was an issue with the party file. Please contact hyperdefined.");
                }
            }
        } else if (args[0].equalsIgnoreCase("accept")) {
            if (args.length > 1) {
                sender.sendMessage(ChatColor.RED + "Invalid syntax! Do /party accept instead.");
            } else {
                Player player = Bukkit.getPlayerExact(sender.getName());
                if (PartyManagement.pendingInvites.containsKey(player)) {
                    String partyID = PartyManagement.pendingInvites.get(player);
                    try {
                        PartyManagement.removeInvite(player, partyID, true);
                    } catch (IOException | ParseException e) {
                        e.printStackTrace();
                        sender.sendMessage(ChatColor.RED + "There was an issue with the party file. Please contact hyperdefined.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have any invites right now.");
                }
            }
        } else if (args[0].equalsIgnoreCase("deny")) {
            if (args.length > 1) {
                sender.sendMessage(ChatColor.RED + "Invalid syntax! Do /party deny instead.");
            } else {
                Player player = Bukkit.getPlayerExact(sender.getName());
                if (PartyManagement.pendingInvites.containsKey(player)) {
                    String partyID = PartyManagement.pendingInvites.get(player);
                    try {
                        PartyManagement.removeInvite(player, partyID, true);
                    } catch (IOException | ParseException e) {
                        e.printStackTrace();
                        sender.sendMessage(ChatColor.RED + "There was an issue with the party file. Please contact hyperdefined.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have any invites right now.");
                }
            }

        } else if (args[0].equalsIgnoreCase("leave")) {
            if (args.length > 1) {
                sender.sendMessage(ChatColor.RED + "Invalid syntax! Do /party leave instead.");
            } else {
                Player player = Bukkit.getPlayerExact(sender.getName());
                try {
                    if (PartyManagement.lookupParty(player) == null) {
                        sender.sendMessage(ChatColor.RED + "You are not in a party! Do /party create to make one!");
                    } else if (PartyManagement.isPlayerOwner(player)) {
                        sender.sendMessage(ChatColor.RED + "You cannot leave as the owner. To delete a party, do /party disband.");
                    } else {
                        PartyManagement.sendPartyMessage(ChatColor.RED + sender.getName() + " has left the party.", PartyManagement.lookupParty(player));
                        PartyManagement.removePlayerFromParty(player, PartyManagement.lookupParty(player));
                    }
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED + "There was an issue with the party file. Please contact hyperdefined.");
                }
            }
        } else if (args[0].equalsIgnoreCase("disband")) {
            if (args.length > 1) {
                sender.sendMessage(ChatColor.RED + "Invalid syntax! Do /party disband instead.");
            } else {
                Player player = Bukkit.getPlayerExact(sender.getName());
                try {
                    if (PartyManagement.lookupParty(player) == null) {
                        sender.sendMessage(ChatColor.RED + "You are not in a party! Do /party create to make one!");
                    } else if (PartyManagement.isPlayerOwner(player)) {
                        PartyManagement.sendPartyMessage(ChatColor.RED + "Party has been deleted.", PartyManagement.lookupParty(player));
                        PartyManagement.deleteParty(PartyManagement.lookupParty(player));
                    } else {
                        sender.sendMessage(ChatColor.RED + "You aren't the owner of a party. Do /party leave instead.");
                    }
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED + "There was an issue with the party file. Please contact hyperdefined.");
                }
            }
        } else if (args[0].equalsIgnoreCase("kick")) {
            if (args.length == 1 || args.length > 2) {
                sender.sendMessage(ChatColor.RED + "Invalid syntax. Do /party kick <player> instead.");
            } else {
                Player player = Bukkit.getPlayer(sender.getName());
                try {
                    if (PartyManagement.lookupParty(player) == null) {
                        sender.sendMessage(ChatColor.RED + "You are not in a party! Do /party create to make one!");
                    } else if (PartyManagement.isPlayerOwner(player)) {
                        if (Bukkit.getPlayer(args[1]) != null) {
                            Player kickedPlayed = Bukkit.getPlayerExact(args[1]);
                            String partyPlayerKicked = PartyManagement.lookupParty(kickedPlayed);
                            String partyPlayerSender = PartyManagement.lookupOwner(PartyManagement.lookupParty(player)).toString();
                            if (partyPlayerSender.equals(partyPlayerKicked)) {
                                sender.sendMessage(ChatColor.RED + "That player is not in your party.");
                            } else {
                                PartyManagement.sendPartyMessage(ChatColor.RED + kickedPlayed.getName() + " has been kicked from the party.", PartyManagement.lookupParty(kickedPlayed));
                                PartyManagement.removePlayerFromParty(kickedPlayed, PartyManagement.lookupParty(kickedPlayed));
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Invalid player.");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You cannot kick members from the party. Only owners can.");
                    }
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED + "There was an issue with the party file. Please contact hyperdefined.");
                }
            }
        } else if (args[0].equalsIgnoreCase("transfer")) {
            if (args.length == 1 || args.length > 2) {
                sender.sendMessage(ChatColor.RED + "Invalid syntax. Do /party transfer <player> instead.");
            } else {
                Player player = Bukkit.getPlayer(sender.getName());
                try {
                    if (PartyManagement.lookupParty(player) == null) {
                        sender.sendMessage(ChatColor.RED + "You are not in a party! Do /party create to make one!");
                    } else if (PartyManagement.isPlayerOwner(player)) {
                        if (Bukkit.getPlayer(args[1]) != null) {
                            Player newOwner = Bukkit.getPlayerExact(args[1]);
                            String partyPlayerKicked = PartyManagement.lookupParty(newOwner);
                            String partyPlayerSender = PartyManagement.lookupOwner(PartyManagement.lookupParty(player)).toString();
                            if (partyPlayerSender.equals(partyPlayerKicked)) {
                                sender.sendMessage(ChatColor.RED + "That player is not in your party.");
                            } else {
                                PartyManagement.sendPartyMessage(ChatColor.RED + newOwner.getName() + " is now the owner of the party.", PartyManagement.lookupParty(newOwner));
                                PartyManagement.updatePartyOwner(newOwner, PartyManagement.lookupParty(newOwner));
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Invalid player.");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You cannot transfer ownership. Only owners can.");
                    }
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED + "There was an issue with the party file. Please contact hyperdefined.");
                }
            }
        } else if (args[0].equalsIgnoreCase("info")) {
            if (args.length > 1) {
                sender.sendMessage(ChatColor.RED + "Invalid syntax! Do /party list instead.");
            } else {
                Player player = Bukkit.getPlayerExact(sender.getName());
                try {
                    if (PartyManagement.lookupParty(player) == null) {
                        sender.sendMessage(ChatColor.RED + "You are not in a party! Do /party create to make one!");
                    } else {
                        player.sendMessage(ChatColor.GOLD + "--------------------------------------------");
                        player.sendMessage(ChatColor.GOLD + "Members: " + ChatColor.YELLOW + PartyManagement.listPartyMembers(PartyManagement.lookupParty(player)).size() + ". " + ChatColor.GOLD + "ID: " + ChatColor.YELLOW + PartyManagement.lookupParty(player));
                        ArrayList<String> playerNames = PartyManagement.listPartyMembers(PartyManagement.lookupParty(player));
                        ArrayList<String> convertedPlayerNames = new ArrayList<>();
                        Bukkit.getScheduler().runTaskAsynchronously(PartyChat.getInstance(), () -> {
                            for (String player1 : playerNames) {
                                convertedPlayerNames.add(UUIDLookup.getName(player1));
                            }
                            for (String partyMember : convertedPlayerNames) {
                                if (Bukkit.getPlayerExact(partyMember) != null) {
                                    player.sendMessage(ChatColor.GREEN + partyMember);
                                } else {
                                    player.sendMessage(ChatColor.RED + partyMember);
                                }
                            }
                            player.sendMessage(ChatColor.GOLD + "--------------------------------------------");
                        });
                    }
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED + "There was an issue with the party file. Please contact hyperdefined.");
                }
            }
        } else if (args[0].equalsIgnoreCase("help")) {
            if (args.length > 1) {
                sender.sendMessage(ChatColor.RED + "Invalid syntax! Do /party help instead.");
            } else {
                sender.sendMessage(ChatColor.GOLD + "--------------------------------------------");
                sender.sendMessage(ChatColor.YELLOW + "/party create - Make a new party.");
                sender.sendMessage(ChatColor.YELLOW + "/party invite <player> - Invite a player to the party.");
                sender.sendMessage(ChatColor.YELLOW + "/party accept/deny - Accept or deny an invite.");
                sender.sendMessage(ChatColor.YELLOW + "/party kick <player> - Kick a player from the party.");
                sender.sendMessage(ChatColor.YELLOW + "/party leave - Leave the party.");
                sender.sendMessage(ChatColor.YELLOW + "/party disband - Delete the party.");
                sender.sendMessage(ChatColor.YELLOW + "/party info - Information about the party.");
                sender.sendMessage(ChatColor.YELLOW + "/party transfer <player> - Transfer ownership of party.");
                sender.sendMessage(ChatColor.YELLOW + "/pc <message> - Send a message to the party.");
                sender.sendMessage(ChatColor.GOLD + "--------------------------------------------");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid command! Do /party help instead.");
        }
        return true;
    }
}