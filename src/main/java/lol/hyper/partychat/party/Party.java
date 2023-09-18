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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Party {

    private Set<UUID> partyMembers = new HashSet<>();
    private Set<UUID> trustedMembers = new HashSet<>();

    private UUID partyOwner;
    private final String partyID;
    private final PartyChat partyChat;
    private final BukkitAudiences audiences;
    private final MiniMessage miniMessage;

    /**
     * Create a new party.
     *
     * @param partyChat PartyChat instance.
     * @param partyID   The party ID that was generated. This should match the party's file name.
     */
    public Party(PartyChat partyChat, String partyID) {
        this.partyChat = partyChat;
        this.partyID = partyID;
        this.audiences = partyChat.getAdventure();
        this.miniMessage = partyChat.miniMessage;
    }

    /**
     * Sets the members of the party.
     * This should only be used when the party is first loaded.
     */
    public void setPartyMembers(Set<UUID> members) {
        this.partyMembers = members;
    }

    /**
     * Sets the trusted members of the party.
     * This should only be used when the party is first loaded.
     */
    public void setTrustedMembers(Set<UUID> members) {
        this.trustedMembers = members;
    }

    /**
     * Get the owner.
     *
     * @return The owner of the party.
     */
    public UUID owner() {
        return partyOwner;
    }

    /**
     * Set the party owner.
     *
     * @param partyOwner The new owner.
     */
    public void setPartyOwner(UUID partyOwner) {
        this.partyOwner = partyOwner;
        exportParty();
    }

    /**
     * Is the player the owner.
     *
     * @param player The player to check.
     * @return If the player is an owner.
     */
    public boolean isOwner(UUID player) {
        return player.equals(partyOwner);
    }

    /**
     * Gets all party members.
     *
     * @return The members.
     */
    public Set<UUID> partyMembers() {
        return partyMembers;
    }

    /**
     * Remove a player from the party.
     *
     * @param player The player to remove.
     */
    public void removePartyMember(UUID player) {
        partyMembers.remove(player);
        // remove if they are a trusted member
        trustedMembers.remove(player);
        exportParty();
    }

    /**
     * Add a player to the party.
     *
     * @param player The player to add.
     */
    public void addPartyMember(UUID player) {
        partyMembers.add(player);
        exportParty();
    }

    /**
     * Add a trusted member.
     *
     * @param player The player to trust.
     */
    public void addTrustedMember(UUID player) {
        trustedMembers.add(player);
        exportParty();
    }

    /**
     * Remove a trusted member.
     *
     * @param player The player to remove.
     */
    public void removeTrustedMember(UUID player) {
        trustedMembers.remove(player);
        exportParty();
    }

    /**
     * Gets the trusted members.
     *
     * @return The trusted members.
     */
    public Set<UUID> trustedMembers() {
        return trustedMembers;
    }

    /**
     * Check if the player is trusted.
     *
     * @param player The player to check.
     * @return If the player is trusted or not.
     */
    public boolean isTrusted(UUID player) {
        return trustedMembers.contains(player);
    }

    /**
     * Gets the party ID.
     *
     * @return The party ID.
     */
    public String partyID() {
        return partyID;
    }

    /**
     * Invite a player to this party.
     *
     * @param sender   The player who is sending the invite.
     * @param receiver The player who is being invited.
     */
    public void invitePlayer(UUID sender, UUID receiver) {
        Invite invite = new Invite(sender, receiver);
        partyChat.invites.add(invite);
        Player receiverPlayer = Bukkit.getPlayer(receiver);
        Player senderPlayer = Bukkit.getPlayer(sender);

        if (receiverPlayer != null && senderPlayer != null) {
            String inviteReceived = partyChat.getConfigMessage("commands.invite.invite-received").replace("%player%", receiverPlayer.getName());
            audiences.player(receiverPlayer).sendMessage(miniMessage.deserialize(inviteReceived));
            audiences.player(senderPlayer).sendMessage(partyChat.getComponent("commands.invite.invite-sent"));
        } else {
            return;
        }

        String sentInvite = partyChat.getConfigMessage("commands.invite.sent-invite").replace("%player1%", senderPlayer.getName()).replace("%player2%", receiverPlayer.getName());
        partyChat.logger.info(sender + " sent an invite to " + receiver + " for party " + partyID);
        sendMessage(miniMessage.deserialize(sentInvite));
    }

    /**
     * Accept an invite.
     *
     * @param invite The invite to accept.
     */
    public void acceptInvite(Invite invite) {
        Player player = Bukkit.getPlayer(invite.getReceiver());
        addPartyMember(invite.getReceiver());
        String newJoin = partyChat.getConfigMessage("commands.accept.sender-accepted").replace("%player%", player.getName());
        sendMessage(miniMessage.deserialize(newJoin));
        partyChat.logger.info(invite.getReceiver() + " has accepted invite for party " + partyID);
        partyChat.invites.remove(invite);
    }

    /**
     * Deny an invite.
     *
     * @param invite The invite to deny.
     */
    public void denyInvite(Invite invite) {
        Player sentInvitePlayer = Bukkit.getPlayer(invite.getSender());
        Player invitedPlayer = Bukkit.getPlayer(invite.getReceiver());
        if (sentInvitePlayer != null) {
            String denied = partyChat.getConfigMessage("commands.deny.sender-denied").replace("%player%", sentInvitePlayer.getName());
            audiences.sender(sentInvitePlayer).sendMessage(miniMessage.deserialize(denied));
        }
        if (invitedPlayer != null) {
            audiences.sender(invitedPlayer).sendMessage(partyChat.getComponent("commands.deny.denied"));
        }
        partyChat.logger.info(invitedPlayer + " has denied invite for party " + partyID);
        partyChat.invites.remove(invite);
    }

    /**
     * Sends a message to all online party members.
     *
     * @param message The message to send.
     */
    public void sendMessage(Component message) {
        for (UUID partyMember : partyMembers) {
            Player player = Bukkit.getPlayer(partyMember);
            if (player != null) {
                Component partyMessage = partyChat.getComponent("party-prefix").append(message);
                audiences.sender(player).sendMessage(partyMessage);
            }
        }
    }

    /**
     * Exports this party into it's file on disk. Any changes to the party should call this after
     */
    private void exportParty() {
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
