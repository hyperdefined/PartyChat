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
import lol.hyper.partychat.tools.Party;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CommandParty implements TabExecutor {

    private final PartyChat partyChat;
    private final BukkitAudiences audiences;
    private final MiniMessage miniMessage;

    public CommandParty(PartyChat partyChat) {
        this.partyChat = partyChat;
        this.audiences = partyChat.getAdventure();
        this.miniMessage = partyChat.miniMessage;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0 || sender instanceof ConsoleCommandSender) {
            audiences.sender(sender).sendMessage(Component.text("PartyChat version " + partyChat.getDescription().getVersion() + ". Created by hyperdefined.").color(NamedTextColor.GREEN));
            audiences.sender(sender).sendMessage(Component.text("Use /party help for command help.").color(NamedTextColor.GREEN));
            return true;
        }

        UUID commandSender = Bukkit.getPlayerExact(sender.getName()).getUniqueId();

        switch (args[0]) {
            case "help":
                List<String> helpCommandLines = partyChat.messages.getStringList("commands.help-command");
                Component helpCommand = Component.empty();
                for (int i = 0; i < helpCommandLines.size(); i++) {
                    String line = partyChat.messages.getStringList("commands.help-command").get(i);
                    if (i == 0) {
                        helpCommand = miniMessage.deserialize(line);
                    } else {
                        helpCommand = helpCommand.append(Component.newline()).append(miniMessage.deserialize(line));
                    }
                }
                audiences.sender(sender).sendMessage(helpCommand);
                break;
            case "invite": {
                if (args.length == 1 || args.length > 2) {
                    audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.invite.invalid-syntax")));
                    return true;
                }
                if (partyChat.partyManagement.loadParty(commandSender) == null) {
                    audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("errors.not-in-a-party")));
                    return true;
                }
                if (partyChat.partyManagement.isPlayerOwner(commandSender)
                        || partyChat.partyManagement.checkTrusted(commandSender)) {
                    if (Bukkit.getPlayerExact(args[1]) == null) {
                        audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("errors.player-not-found")));
                        return true;
                    }
                    Player playerToInvite = Bukkit.getPlayerExact(args[1]);
                    if (partyChat.partyManagement.pendingInvites.containsKey(playerToInvite.getUniqueId())) {
                        audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.invite.pending-invite")));
                        return true;
                    }
                    if (partyChat.partyManagement.loadParty(playerToInvite.getUniqueId()) != null) {
                        audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("errors.player-in-party")));
                        return true;
                    }
                    String partyID = partyChat.partyManagement.loadParty(commandSender).getPartyID();
                    partyChat.partyManagement.invitePlayer(playerToInvite.getUniqueId(), commandSender, partyID);
                    return true;
                }
                audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.invite.cant-invite")));
                return true;
            }
            case "create": {
                if (partyChat.partyManagement.loadParty(commandSender) == null) {
                    partyChat.partyManagement.createParty(commandSender);
                    audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.create.party-created")));
                } else {
                    audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("errors.player-in-party")));
                }
                return true;
            }
            case "accept": {
                if (partyChat.partyManagement.pendingInvites.containsKey(commandSender)) {
                    partyChat.partyManagement.removeInvite(commandSender, true);
                } else {
                    audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.accept.no-invites")));
                }
                return true;
            }
            case "deny": {
                if (partyChat.partyManagement.pendingInvites.containsKey(commandSender)) {
                    partyChat.partyManagement.removeInvite(commandSender, false);
                } else {
                    audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.deny.no-invites")));
                }
                return true;
            }
            case "leave": {
                if (partyChat.partyManagement.loadParty(commandSender) == null) {
                    audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("errors.not-in-a-party")));
                    return true;
                }
                if (partyChat.partyManagement.isPlayerOwner(commandSender)) {
                    audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.leave.owner-leave")));
                    return true;
                }
                Player playerLeaving = (Player) sender;
                String partyID = partyChat.partyManagement.loadParty(playerLeaving.getUniqueId()).getPartyID();
                partyChat.partyManagement.sendPartyMessage(miniMessage.deserialize(partyChat.getMessage("commands.leave.has-left").replace("%player%", playerLeaving.getName())), partyID);
                partyChat.partyManagement.removePlayerFromParty(commandSender, partyID);
                return true;
            }
            case "disband": {
                if (partyChat.partyManagement.loadParty(commandSender) == null) {
                    audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("errors.not-in-a-party")));
                    return true;
                }
                if (!partyChat.partyManagement.isPlayerOwner(commandSender)) {
                    audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.disband.not-party-owner")));
                    return true;
                }
                Player playerLeaving = (Player) sender;
                String partyID = partyChat.partyManagement.loadParty(playerLeaving.getUniqueId()).getPartyID();
                partyChat.partyManagement.sendPartyMessage(miniMessage.deserialize(partyChat.getMessage("commands.disband.disbanded")), partyID);
                partyChat.partyManagement.deleteParty(partyID);
                return true;
            }
            case "kick": {
                if (args.length == 1 || args.length > 2) {
                    audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.kick.invalid-syntax")));
                    return true;
                }
                if (partyChat.partyManagement.loadParty(commandSender) == null) {
                    audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("errors.not-in-a-party")));
                    return true;
                }
                if (partyChat.partyManagement.isPlayerOwner(commandSender)
                        || partyChat.partyManagement.checkTrusted(commandSender)) {
                    if (Bukkit.getPlayerExact(args[1]) == null) {
                        audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("errors.player-not-found")));
                        return true;
                    }
                    Player playerToKick = Bukkit.getPlayerExact(args[1]);
                    String partyIDKickingPlayer = partyChat.partyManagement.loadParty(playerToKick.getUniqueId()).getPartyID();
                    String partyID = partyChat.partyManagement.loadParty(commandSender).getPartyID();
                    if (!partyID.equals(partyIDKickingPlayer)) {
                        audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.kick.not-in-party")));
                        return true;
                    }
                    if (partyChat.partyManagement.isPlayerOwner(playerToKick.getUniqueId())) {
                        audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.kick.kick-owner")));
                        return true;
                    }
                    if (commandSender.equals(playerToKick.getUniqueId())) {
                        audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.kick.kick-yourself")));
                        return true;
                    }
                    String kickMessage = partyChat.getMessage("commands.kick.kicked").replace("%player1%", playerToKick.getName()).replace("%player2%", sender.getName());
                    partyChat.partyManagement.sendPartyMessage(miniMessage.deserialize(kickMessage), partyID);
                    partyChat.partyManagement.removePlayerFromParty(playerToKick.getUniqueId(), partyID);
                    return true;
                }
                audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.kick.kick-trusted")));
                return true;
            }
            case "transfer": {
                if (args.length == 1 || args.length > 2) {
                    audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.transfer.invalid-syntax")));
                    return true;
                }
                if (partyChat.partyManagement.loadParty(commandSender) == null) {
                    audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("errors.not-in-a-party")));
                    return true;
                }
                if (partyChat.partyManagement.isPlayerOwner(commandSender)) {
                    if (Bukkit.getPlayerExact(args[1]) == null) {
                        audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("errors.player-not-found")));
                        return true;
                    }
                    Player newOwner = Bukkit.getPlayerExact(args[1]);
                    String partyID = partyChat.partyManagement.loadParty(commandSender).getPartyID();
                    String newOwnerMessage = partyChat.getMessage("commands.transfer.new-owner").replace("%player%", newOwner.getName());
                    partyChat.partyManagement.sendPartyMessage(miniMessage.deserialize(newOwnerMessage), partyID);
                    partyChat.partyManagement.updatePartyOwner(newOwner.getUniqueId(), partyID);
                    return true;
                }
                audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.transfer.not-owner")));
                return true;
            }
            case "info": {
                if (partyChat.partyManagement.loadParty(commandSender) == null) {
                    audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("errors.not-in-a-party")));
                    return true;
                }
                Party party = partyChat.partyManagement.loadParty(commandSender);
                UUID partyOwner = party.getPartyOwner();
                List<String> infoCommandLines = partyChat.messages.getStringList("commands.info.command");
                List<String> players = new ArrayList<>();
                // since members are saved as UUIDs, we have to convert them to names
                // if the server doesn't have the name saved, use the UUID
                for (UUID player : party.getPartyMembers()) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
                    String name = offlinePlayer.getName();
                    if (name != null) {
                        players.add(name);
                    } else {
                        players.add(player.toString());
                    }
                }
                Component infoCommand = Component.empty();
                for (int i = 0; i < infoCommandLines.size(); i++) {
                    String line = infoCommandLines.get(i);
                    if (line.contains("%size%")) {
                        line = line.replace("%size%", String.valueOf(party.getPartyMembers().size()));
                    }
                    if (line.contains("%ID%")) {
                        line = line.replace("%ID%", party.getPartyID());
                    }
                    if (line.contains("%members%")) {
                        line = String.join(", ", players);
                    }
                    if (line.contains("%owner%")) {
                        OfflinePlayer owner = Bukkit.getOfflinePlayer(partyOwner);
                        String name = owner.getName();
                        if (name != null) {
                            line = line.replace("%owner%", name);
                        } else {
                            line = line.replace("%owner%", partyOwner.toString());
                        }
                    }
                    if (i == 0) {
                        infoCommand = miniMessage.deserialize(line);
                    } else {
                        infoCommand = infoCommand.append(Component.newline()).append(miniMessage.deserialize(line));
                    }
                }
                audiences.sender(sender).sendMessage(infoCommand);
                return true;
            }
            case "trust": {
                if (args.length == 1 || args.length > 2) {
                    audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.trust.invalid-syntax")));
                    return true;
                }
                if (partyChat.partyManagement.loadParty(commandSender) == null) {
                    audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("errors.not-in-a-party")));
                    return true;
                }
                if (partyChat.partyManagement.isPlayerOwner(commandSender)) {
                    if (Bukkit.getPlayerExact(args[1]) == null) {
                        audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("errors.player-not-found")));
                        return true;
                    }
                    Player memberToTrust = Bukkit.getPlayerExact(args[1]);
                    String partyID = partyChat.partyManagement.loadParty(commandSender).getPartyID();
                    String partyIDTrusted = partyChat.partyManagement.loadParty(memberToTrust.getUniqueId()).getPartyID();
                    if (!partyID.equals(partyIDTrusted)) {
                        audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.trust.not-in-party")));
                        return true;
                    }
                    if (commandSender.equals(memberToTrust.getUniqueId())) {
                        audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.trust.already-owner")));
                        return true;
                    }
                    if (partyChat.partyManagement.checkTrusted(memberToTrust.getUniqueId())) {
                        audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.trust.already-trusted")));
                        return true;
                    }
                    partyChat.partyManagement.trustPlayer(memberToTrust.getUniqueId());
                    return true;
                }
                audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.trust.not-owner")));
                return true;
            }
            case "untrust": {
                if (args.length == 1 || args.length > 2) {
                    audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.untrust.invalid-syntax")));
                    return true;
                }
                if (partyChat.partyManagement.loadParty(commandSender) == null) {
                    audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("errors.not-in-a-party")));
                    return true;
                }
                if (partyChat.partyManagement.isPlayerOwner(commandSender)) {
                    if (Bukkit.getPlayerExact(args[1]) == null) {
                        audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("errors.player-not-found")));
                        return true;
                    }
                    Player memberToTrust = Bukkit.getPlayerExact(args[1]);
                    String partyID = partyChat.partyManagement.loadParty(commandSender).getPartyID();
                    String partyIDTrusted = partyChat.partyManagement.loadParty(memberToTrust.getUniqueId()).getPartyID();
                    if (!partyID.equals(partyIDTrusted)) {
                        audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.untrust.not-in-party")));
                        return true;
                    }
                    if (commandSender.equals(memberToTrust.getUniqueId())) {
                        audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.untrust.already-owner")));
                        return true;
                    }
                    if (!partyChat.partyManagement.checkTrusted(memberToTrust.getUniqueId())) {
                        audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.untrust.not-trusted")));
                        return true;
                    }
                    partyChat.partyManagement.removeTrustedPlayer(memberToTrust.getUniqueId());
                    return true;
                }
                audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.untrust.not-owner")));
                return true;
            }
            default: {
                audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.invalid-syntax")));
                return true;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length > 0) {
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
