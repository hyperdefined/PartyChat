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
import lol.hyper.partychat.party.Invite;
import lol.hyper.partychat.party.Party;
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

import java.util.*;

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

        UUID commandSender = ((Player) sender).getUniqueId();
        Party senderParty = partyChat.partyManagement.getParty(commandSender);

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
                    audiences.sender(sender).sendMessage(partyChat.getComponent("commands.invite.invalid-syntax"));
                    return true;
                }
                if (senderParty == null) {
                    audiences.sender(sender).sendMessage(partyChat.getComponent("errors.not-in-a-party"));
                    return true;
                }
                if (senderParty.isOwner(commandSender) || senderParty.isTrusted(commandSender)) {
                    Player playerToInvite = Bukkit.getPlayerExact(args[1]);
                    if (playerToInvite == null) {
                        audiences.sender(sender).sendMessage(partyChat.getComponent("errors.player-not-found"));
                        return true;
                    }
                    if (partyChat.partyManagement.pendingInvites.containsKey(playerToInvite.getUniqueId())) {
                        audiences.sender(sender).sendMessage(partyChat.getComponent("commands.invite.pending-invite"));
                        return true;
                    }
                    if (partyChat.partyManagement.getParty(playerToInvite.getUniqueId()) != null) {
                        audiences.sender(sender).sendMessage(partyChat.getComponent("errors.player-in-party"));
                        return true;
                    }
                    senderParty.invitePlayer(commandSender, playerToInvite.getUniqueId());
                    return true;
                }
                audiences.sender(sender).sendMessage(partyChat.getComponent("commands.invite.cant-invite"));
                return true;
            }
            case "create": {
                if (senderParty == null) {
                    partyChat.partyManagement.createParty(commandSender);
                    audiences.sender(sender).sendMessage(partyChat.getComponent("commands.create.party-created"));
                } else {
                    audiences.sender(sender).sendMessage(partyChat.getComponent("errors.player-in-party"));
                }
                return true;
            }
            case "accept": {
                for (Invite invite : partyChat.invites) {
                    if (invite.getReceiver().equals(commandSender)) {
                        UUID inviteSender = invite.getSender();
                        Party inviteSenderParty = partyChat.partyManagement.getParty(inviteSender);
                        inviteSenderParty.acceptInvite(invite);
                        return true;
                    }
                    audiences.sender(sender).sendMessage(partyChat.getComponent("commands.accept.no-invites"));
                    return true;
                }
            }
            case "deny": {
                for (Invite invite : partyChat.invites) {
                    if (invite.getReceiver().equals(commandSender)) {
                        UUID inviteSender = invite.getSender();
                        Party inviteSenderParty = partyChat.partyManagement.getParty(inviteSender);
                        inviteSenderParty.denyInvite(invite);
                        return true;
                    }
                    audiences.sender(sender).sendMessage(partyChat.getComponent("commands.accept.no-invites"));
                    return true;
                }
            }
            case "leave": {
                if (senderParty == null) {
                    audiences.sender(sender).sendMessage(partyChat.getComponent("errors.not-in-a-party"));
                    return true;
                }
                if (senderParty.isOwner(commandSender)) {
                    audiences.sender(sender).sendMessage(partyChat.getComponent("commands.leave.owner-leave"));
                    return true;
                }
                senderParty.sendMessage(miniMessage.deserialize(partyChat.getConfigMessage("commands.leave.has-left").replace("%player%", sender.getName())));
                senderParty.removePartyMember(commandSender);
                return true;
            }
            case "disband": {
                if (senderParty == null) {
                    audiences.sender(sender).sendMessage(partyChat.getComponent("errors.not-in-a-party"));
                    return true;
                }
                if (!senderParty.isOwner(commandSender)) {
                    audiences.sender(sender).sendMessage(partyChat.getComponent("commands.disband.not-party-owner"));
                    return true;
                }
                senderParty.sendMessage(miniMessage.deserialize(partyChat.getConfigMessage("commands.disband.disbanded")));
                partyChat.partyManagement.deleteParty(senderParty);
                return true;
            }
            case "kick": {
                if (args.length == 1 || args.length > 2) {
                    audiences.sender(sender).sendMessage(partyChat.getComponent("commands.kick.invalid-syntax"));
                    return true;
                }
                if (senderParty == null) {
                    audiences.sender(sender).sendMessage(partyChat.getComponent("errors.not-in-a-party"));
                    return true;
                }
                if (senderParty.isOwner(commandSender) || senderParty.isTrusted(commandSender)) {
                    Player playerToKick = Bukkit.getPlayerExact(args[1]);
                    if (playerToKick == null) {
                        audiences.sender(sender).sendMessage(partyChat.getComponent("errors.player-not-found"));
                        return true;
                    }
                    if (!senderParty.partyMembers().contains(playerToKick.getUniqueId())) {
                        audiences.sender(sender).sendMessage(partyChat.getComponent("commands.kick.not-in-party"));
                        return true;
                    }
                    if (senderParty.isOwner(playerToKick.getUniqueId())) {
                        audiences.sender(sender).sendMessage(partyChat.getComponent("commands.kick.kick-owner"));
                        return true;
                    }
                    if (commandSender.equals(playerToKick.getUniqueId())) {
                        audiences.sender(sender).sendMessage(partyChat.getComponent("commands.kick.kick-yourself"));
                        return true;
                    }
                    if (senderParty.trustedMembers().contains(playerToKick.getUniqueId())) {
                        if (!senderParty.isOwner(commandSender)) {
                            audiences.sender(sender).sendMessage(partyChat.getComponent("commands.kick.kick-trusted"));
                            return true;
                        }
                    }
                    String kickMessage = partyChat.getConfigMessage("commands.kick.kicked").replace("%player1%", playerToKick.getName()).replace("%player2%", sender.getName());
                    senderParty.sendMessage(miniMessage.deserialize(kickMessage));
                    senderParty.removePartyMember(playerToKick.getUniqueId());
                    return true;
                }
                audiences.sender(sender).sendMessage(partyChat.getComponent("commands.kick.not-trusted"));
                return true;
            }
            case "transfer": {
                if (args.length == 1 || args.length > 2) {
                    audiences.sender(sender).sendMessage(partyChat.getComponent("commands.transfer.invalid-syntax"));
                    return true;
                }
                if (senderParty == null) {
                    audiences.sender(sender).sendMessage(partyChat.getComponent("errors.not-in-a-party"));
                    return true;
                }
                if (senderParty.isOwner(commandSender)) {
                    Player newOwner = Bukkit.getPlayerExact(args[1]);
                    if (newOwner == null) {
                        audiences.sender(sender).sendMessage(partyChat.getComponent("errors.player-not-found"));
                        return true;
                    }
                    String newOwnerMessage = partyChat.getConfigMessage("commands.transfer.new-owner").replace("%player%", newOwner.getName());
                    senderParty.sendMessage(miniMessage.deserialize(newOwnerMessage));
                    senderParty.setPartyOwner(newOwner.getUniqueId());
                    return true;
                }
                audiences.sender(sender).sendMessage(partyChat.getComponent("commands.transfer.not-owner"));
                return true;
            }
            case "info": {
                if (senderParty == null) {
                    audiences.sender(sender).sendMessage(partyChat.getComponent("errors.not-in-a-party"));
                    return true;
                }
                UUID partyOwner = senderParty.owner();
                List<String> infoCommandLines = partyChat.messages.getStringList("commands.info.command");
                List<String> players = new ArrayList<>();
                // since members are saved as UUIDs, we have to convert them to names
                // if the server doesn't have the name saved, use the UUID
                for (UUID player : senderParty.partyMembers()) {
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
                        line = line.replace("%size%", String.valueOf(senderParty.partyMembers().size()));
                    }
                    if (line.contains("%ID%")) {
                        line = line.replace("%ID%", senderParty.partyID());
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
                    audiences.sender(sender).sendMessage(partyChat.getComponent("commands.trust.invalid-syntax"));
                    return true;
                }
                if (senderParty == null) {
                    audiences.sender(sender).sendMessage(partyChat.getComponent("errors.not-in-a-party"));
                    return true;
                }
                if (senderParty.isOwner(commandSender)) {
                    Player memberToTrust = Bukkit.getPlayerExact(args[1]);
                    if (memberToTrust == null) {
                        audiences.sender(sender).sendMessage(partyChat.getComponent("errors.player-not-found"));
                        return true;
                    }
                    if (!senderParty.partyMembers().contains(memberToTrust.getUniqueId())) {
                        audiences.sender(sender).sendMessage(partyChat.getComponent("commands.trust.not-in-party"));
                        return true;
                    }
                    if (commandSender.equals(memberToTrust.getUniqueId())) {
                        audiences.sender(sender).sendMessage(partyChat.getComponent("commands.trust.already-owner"));
                        return true;
                    }
                    if (senderParty.isTrusted(memberToTrust.getUniqueId())) {
                        audiences.sender(sender).sendMessage(partyChat.getComponent("commands.trust.already-trusted"));
                        return true;
                    }
                    senderParty.addTrustedMember(memberToTrust.getUniqueId());
                    return true;
                }
                audiences.sender(sender).sendMessage(partyChat.getComponent("commands.trust.not-owner"));
                return true;
            }
            case "untrust": {
                if (args.length == 1 || args.length > 2) {
                    audiences.sender(sender).sendMessage(partyChat.getComponent("commands.untrust.invalid-syntax"));
                    return true;
                }
                if (senderParty == null) {
                    audiences.sender(sender).sendMessage(partyChat.getComponent("errors.not-in-a-party"));
                    return true;
                }
                if (senderParty.isOwner(commandSender)) {
                    Player memberToUnTrust = Bukkit.getPlayerExact(args[1]);
                    if (memberToUnTrust == null) {
                        audiences.sender(sender).sendMessage(partyChat.getComponent("errors.player-not-found"));
                        return true;
                    }
                    if (!senderParty.partyMembers().contains(memberToUnTrust.getUniqueId())) {
                        audiences.sender(sender).sendMessage(partyChat.getComponent("commands.untrust.not-in-party"));
                        return true;
                    }
                    if (commandSender.equals(memberToUnTrust.getUniqueId())) {
                        audiences.sender(sender).sendMessage(partyChat.getComponent("commands.untrust.already-owner"));
                        return true;
                    }
                    if (!senderParty.isTrusted(memberToUnTrust.getUniqueId())) {
                        audiences.sender(sender).sendMessage(partyChat.getComponent("commands.untrust.not-trusted"));
                        return true;
                    }
                    senderParty.removeTrustedMember(memberToUnTrust.getUniqueId());
                    return true;
                }
                audiences.sender(sender).sendMessage(partyChat.getComponent("commands.untrust.not-owner"));
                return true;
            }
            default: {
                audiences.sender(sender).sendMessage(partyChat.getComponent("commands.invalid-syntax"));
                return true;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String
            alias, String[] args) {
        if (args.length == 1) {
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
        }
        return null;
    }
}
