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

package lol.hyper.partychat.party;

import lol.hyper.partychat.PartyChat;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

public class Party {

    private Set<UUID> partyMembers = new HashSet<>();
    private Set<UUID> trustedMembers = new HashSet<>();

    private UUID partyOwner;
    private final String partyID;
    private final PartyChat partyChat;
    private final BukkitAudiences audiences;
    private final MiniMessage miniMessage;

    public Party(PartyChat partyChat, String partyID) {
        this.partyChat = partyChat;
        this.partyID = partyID;
        this.audiences = partyChat.getAdventure();
        this.miniMessage = partyChat.miniMessage;
    }

    public UUID owner() {
        return partyOwner;
    }

    public void setPartyMembers(Set<UUID> members) {
        this.partyMembers = members;
    }

    public void setTrustedMembers(Set<UUID> members) {
        this.trustedMembers = members;
    }

    public void setPartyOwner(UUID partyOwner) {
        this.partyOwner = partyOwner;
        exportParty();
    }

    public boolean isOwner(UUID player) {
        return player.equals(partyOwner);
    }

    public Set<UUID> partyMembers() {
        return partyMembers;
    }

    public void removePartyMember(UUID player) {
        partyMembers.remove(player);
        // remove if they are a trusted member
        trustedMembers.remove(player);
        exportParty();
    }

    public void addPartyMember(UUID player) {
        partyMembers.add(player);
        exportParty();
    }

    public void addTrustedMember(UUID player) {
        trustedMembers.add(player);
        exportParty();
    }

    public void removeTrustedMember(UUID player) {
        trustedMembers.remove(player);
        exportParty();
    }

    public Set<UUID> trustedMembers() {
        return trustedMembers;
    }

    public String partyID() {
        return partyID;
    }

    public boolean isTrusted(UUID player) {
        return trustedMembers.contains(player);
    }

    public void invitePlayer(UUID sender, UUID receiver) {
        Invite invite = new Invite(this, sender, receiver);
        partyChat.invites.add(invite);
        Player receiverPlayer = Bukkit.getPlayer(receiver);
        Player senderPlayer = Bukkit.getPlayer(sender);

        if (receiverPlayer != null && senderPlayer != null) {
            String inviteReceived = partyChat.getMessage("commands.invite.invite-received").replace("%player%", receiverPlayer.getName());
            audiences.player(receiverPlayer).sendMessage(miniMessage.deserialize(inviteReceived));

            audiences.player(senderPlayer).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.invite.invite-sent")));
        } else {
            return;
        }

        String sentInvite = partyChat.getMessage("commands.invite.sent-invite").replace("%player1%", senderPlayer.getName()).replace("%player2%", receiverPlayer.getName());
        partyChat.logger.info(
                sender + " sent an invite to " + receiver + " for party " + partyID);
        sendMessage(miniMessage.deserialize(sentInvite));
    }

    public void acceptInvite(Invite invite) {
        Player player = Bukkit.getPlayer(invite.getReceiver());
        addPartyMember(invite.getReceiver());
        String newJoin = partyChat.getMessage("commands.accept.sender-accepted").replace("%player%", player.getName());
        sendMessage(miniMessage.deserialize(newJoin));
        partyChat.logger.info(invite.getReceiver() + " has accepted invite for party " + partyID);
        partyChat.invites.remove(invite);
    }

    public void denyInvite(Invite invite) {
        Player sentInvitePlayer = Bukkit.getPlayer(invite.getSender());
        Player invitedPlayer = Bukkit.getPlayer(invite.getReceiver());
        if (sentInvitePlayer != null) {
            String denied = partyChat.getMessage("commands.deny.sender-denied").replace("%player%", sentInvitePlayer.getName());
            audiences.sender(sentInvitePlayer).sendMessage(miniMessage.deserialize(denied));
        }
        if (invitedPlayer != null) {
            audiences.sender(invitedPlayer).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.deny.denied")));
        }
        partyChat.logger.info(invitedPlayer + " has denied invite for party " + partyID);
        partyChat.invites.remove(invite);
    }

    public void sendMessage(Component message) {
        for (UUID partyMember : partyMembers) {
            Player player = Bukkit.getPlayer(partyMember);
            if (player != null) {
                Component partyMessage = miniMessage.deserialize(partyChat.getMessage("party-prefix")).append(message);
                audiences.sender(player).sendMessage(partyMessage);
            }
        }
    }

    public void exportParty() {
        File partyFile = new File(partyChat.partyFolder.toFile(), partyID + ".json");
        JSONObject newPartyObject = new JSONObject();
        newPartyObject.put("owner", partyOwner.toString());
        JSONArray partyMembers = new JSONArray();
        for (UUID member : this.partyMembers) {
            partyMembers.put(member.toString());
        }
        newPartyObject.put("members", partyMembers);

        JSONArray trustedMembers = new JSONArray();
        for (UUID member : this.trustedMembers) {
            trustedMembers.put(member.toString());
        }
        newPartyObject.put("trusted", trustedMembers);
        newPartyObject.put("id", partyID);

        partyChat.partyManagement.writeFile(partyFile, newPartyObject);
    }
}
