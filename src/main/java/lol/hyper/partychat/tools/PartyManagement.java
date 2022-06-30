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

package lol.hyper.partychat.tools;

import lol.hyper.partychat.PartyChat;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;

public class PartyManagement {

    /**
     * invite receiver
     * invite sender
     */
    public final HashMap<UUID, UUID> pendingInvites = new HashMap<>();

    private final PartyChat partyChat;
    private final BukkitAudiences audiences;
    private final MiniMessage miniMessage;

    public PartyManagement(PartyChat partyChat) {
        this.partyChat = partyChat;
        this.audiences = partyChat.getAdventure();
        this.miniMessage = partyChat.miniMessage;
    }

    /**
     * Read data from JSON file.
     * @param file File to read data from.
     * @return JSONObject with JSON data.
     */
    private JSONObject readFile(File file) {
        JSONObject object = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            object = new JSONObject(sb.toString());
            br.close();
        } catch (Exception e) {
            partyChat.logger.severe("Unable to read file " + file.getAbsolutePath());
            partyChat.logger.severe("This is bad, really bad.");
            e.printStackTrace();
        }
        return object;
    }

    /**
     * Write data to JSON file.
     * @param file File to write data to.
     * @param jsonToWrite Data to write to file. This much be a JSON string.
     */
    private void writeFile(File file, JSONObject jsonToWrite) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(jsonToWrite.toString());
            writer.close();
        } catch (IOException e) {
            partyChat.logger.severe("Unable to write file " + file.getAbsolutePath());
            partyChat.logger.severe("This is bad, really bad.");
            e.printStackTrace();
        }
    }

    /**
     * Invite player to the party.
     *
     * @param receiver UUID of player receiving the invite.
     * @param sender   UUID of player sending the invite.
     * @param partyID  Party ID of the person inviting.
     */
    public void invitePlayer(UUID receiver, UUID sender, String partyID) {
        pendingInvites.put(receiver, sender);
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
        sendPartyMessage(miniMessage.deserialize(sentInvite), partyID);
    }

    /**
     * Remove player from pending invites list.
     *
     * @param pendingPlayer UUID of person to remove invite from.
     * @param answer Player's response to being invited.
     */
    public void removeInvite(UUID pendingPlayer, boolean answer) {
        Player invitedPlayer = Bukkit.getPlayer(pendingPlayer);
        Party party = loadParty(pendingInvites.get(pendingPlayer));
        if (answer) {
            addPlayerToParty(pendingPlayer, party.getPartyID());
            sendPartyMessage(Component.text(invitedPlayer.getName() + " has joined the party!").color(NamedTextColor.GREEN), party.getPartyID());
            partyChat.logger.info(pendingPlayer + " has accepted invite for party " + party.getPartyID());
        } else {
            Player sentInvitePlayer = Bukkit.getPlayer(pendingInvites.get(pendingPlayer));
            if (sentInvitePlayer != null) {
                String denied = partyChat.getMessage("commands.deny.sender-denied").replace("%player%", sentInvitePlayer.getName());
                audiences.sender(sentInvitePlayer).sendMessage(miniMessage.deserialize(denied));
            }
            if (invitedPlayer != null) {
                audiences.sender(invitedPlayer).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.deny.denied")));
            }
            partyChat.logger.info(pendingPlayer + " has denied invite for party " + party.getPartyID());
        }
        pendingInvites.remove(pendingPlayer);
    }

    /**
     * Delete a party.
     *
     * @param partyID Party ID of party to delete.
     */
    public void deleteParty(String partyID) {
        File partyFile = new File(partyChat.partyFolder.toFile(), partyID + ".json");
        if (!partyFile.delete()) {
            partyChat.logger.warning("Cannot delete party! Please delete: " + partyFile.getAbsolutePath());
            return;
        }
        partyChat.logger.info("Deleting party " + partyID);
    }

    /**
     * Add given player to given party.
     *
     * @param newMember UUID of new player to add to party.
     * @param partyID   Party ID the new player is joining.
     */
    public void addPlayerToParty(UUID newMember, String partyID) {
        Party party = loadParty(partyID);
        party.addPartyMember(newMember);
        exportParty(party);
        partyChat.logger.info("Adding player " + newMember + "to party " + partyID);
    }

    /**
     * Update owner of a party.
     *
     * @param newOwner UUID of new party owner.
     * @param partyID  Party ID of new owner.
     */
    public void updatePartyOwner(UUID newOwner, String partyID) {
        Party party = loadParty(partyID);
        party.setPartyOwner(newOwner);
        exportParty(party);
        partyChat.logger.info("Party " + partyID + " is now owned by " + newOwner);
    }

    /**
     * Remove player from party.
     *
     * @param oldPlayer UUID of player being removed from party.
     * @param partyID   Party ID player is being removed from.
     */
    public void removePlayerFromParty(UUID oldPlayer, String partyID) {
        Party party = loadParty(partyID);
        party.removePartyMember(oldPlayer);
        if (party.getTrustedMembers().contains(oldPlayer)) {
            party.removeTrustedMember(oldPlayer);
        }
        exportParty(party);
        partyChat.logger.info(oldPlayer + " has left party " + partyID);
    }

    /**
     * Check if player is the party owner.
     *
     * @param player UUID of player to check.
     * @return Returns if player is owner.
     */
    public boolean isPlayerOwner(UUID player) {
        return lookupOwner(loadParty(player).getPartyID()).equals(player);
    }

    /**
     * See who owns a party.
     *
     * @param partyID Party ID to check.
     * @return Returns the owner.
     */
    public UUID lookupOwner(String partyID) {
        Party party = loadParty(partyID);
        return party.getPartyOwner();
    }

    /**
     * Send a message to everyone in a party.
     *
     * @param message Message to send the whole party.
     */
    public void sendPartyMessage(Component message, String partyID) {
        Party party = loadParty(partyID);
        for (UUID partyMember : party.getPartyMembers()) {
            Player player = Bukkit.getPlayer(partyMember);
            if (player != null) {
                Component partyMessage = miniMessage.deserialize(partyChat.getMessage("party-prefix")).append(message);
                audiences.sender(player).sendMessage(partyMessage);
            }
        }
    }

    /**
     * Create a party.
     *
     * @param player UUID of new party owner.
     */
    public void createParty(UUID player) {
        UUID newUUID = UUID.randomUUID();
        JSONObject partyObject = new JSONObject();
        partyObject.put("owner", player.toString());
        partyObject.put("id", newUUID.toString());
        JSONArray members = new JSONArray();
        members.put(player.toString());
        partyObject.put("members", members);
        partyObject.put("trusted", new JSONArray());

        File partyFile = new File(partyChat.partyFolder.toFile(), newUUID + ".json");
        writeFile(partyFile, partyObject);
        partyChat.logger.info("Party " + newUUID + " has been created by " + player);
    }

    /**
     * Trust player in a party.
     * @param player Player to trust.
     */
    public void trustPlayer(UUID player) {
        Party party = loadParty(player);
        party.addTrustedMember(player);
        exportParty(party);
        Player trusted = Bukkit.getPlayer(player);
        if (trusted != null) {
            String joinedTrusted = partyChat.getMessage("commands.trust.join-trust").replace("%player%", trusted.getName());
            sendPartyMessage(miniMessage.deserialize(joinedTrusted), party.getPartyID());
        }
        partyChat.logger.info(player + " is now a trusted player of " + party.getPartyID());
    }

    /**
     * Check if a player is trusted.
     * @param player Player to check.
     * @return True if the player is trusted, false if not.
     */
    public boolean checkTrusted(UUID player) {
        Party party = loadParty(player);
        return party.getTrustedMembers().contains(player);
    }

    /**
     * Remove a player's trust rank.
     * @param player Player to remove.
     */
    public void removeTrustedPlayer(UUID player) {
        Party party = loadParty(player);
        party.removeTrustedMember(player);
        exportParty(party);
        String playerName = Bukkit.getPlayer(player).getName();
        String removedTrusted = partyChat.getMessage("commands.untrust.leave-trust").replace("%player%", playerName);
        sendPartyMessage(miniMessage.deserialize(removedTrusted), party.getPartyID());
        partyChat.logger.info(playerName + " is no longer a trusted player of " + party.getPartyID());
    }

    /**
     * Loads a party based on a player's UUID.
     * @param player Player who is in a party.
     * @return The party object.
     */
    public Party loadParty(UUID player) {
        File[] partyDirectory = partyChat.partyFolder.toFile().listFiles();
        String partyID = null;
        if (partyDirectory != null) {
            for (File currentFile : partyDirectory) {
                JSONObject currentJSON = readFile(currentFile);
                JSONArray partyMembers = currentJSON.getJSONArray("members");
                for (int i = 0; i < partyMembers.length(); i++) {
                    if (partyMembers.getString(i).equalsIgnoreCase(player.toString())) {
                        partyID = FilenameUtils.removeExtension(currentFile.getName());
                        break;
                    }
                }
            }
        }
        if (partyID == null) {
            return null;
        }
        File partyFile = new File(partyChat.partyFolder.toFile(), partyID + ".json");
        JSONObject jsonObject = readFile(partyFile);
        Party party = new Party(partyID);
        // add owners
        JSONArray partyMembers = jsonObject.getJSONArray("members");
        for (int i = 0; i < partyMembers.length(); i++) {
            party.addPartyMember(UUID.fromString(partyMembers.getString(i)));
        }
        // set party owner
        party.setPartyOwner(UUID.fromString(jsonObject.getString("owner")));
        // add trusted players
        JSONArray trustedMembers = jsonObject.getJSONArray("trusted");
        for (int i = 0; i < trustedMembers.length(); i++) {
            party.addTrustedMember(UUID.fromString(trustedMembers.getString(i)));
        }
        return party;
    }

    /**
     * Loads a party based on ID.
     * @param partyID Party of ID to load.
     * @return The party object.
     */
    public Party loadParty(String partyID) {
        File partyFile = new File(partyChat.partyFolder.toFile(), partyID + ".json");
        JSONObject jsonObject = readFile(partyFile);
        Party party = new Party(partyID);
        // add owners
        JSONArray partyMembers = jsonObject.getJSONArray("members");
        for (int i = 0; i < partyMembers.length(); i++) {
            party.addPartyMember(UUID.fromString(partyMembers.getString(i)));
        }
        // set party owner
        party.setPartyOwner(UUID.fromString(jsonObject.getString("owner")));
        // add trusted players
        JSONArray trustedMembers = jsonObject.getJSONArray("trusted");
        for (int i = 0; i < trustedMembers.length(); i++) {
            party.addTrustedMember(UUID.fromString(trustedMembers.getString(i)));
        }
        return party;
    }

    /**
     * Exports current party in memory to disk.
     * @param party The party to export.
     */
    public void exportParty(Party party) {
        File partyFile = new File(partyChat.partyFolder.toFile(), party.getPartyID() + ".json");
        JSONObject newPartyObject = new JSONObject();
        newPartyObject.put("owner", party.getPartyOwner().toString());
        JSONArray partyMembers = new JSONArray();
        for (UUID member : party.getPartyMembers()) {
            partyMembers.put(member.toString());
        }
        newPartyObject.put("members", partyMembers);

        JSONArray trustedMembers = new JSONArray();
        for (UUID member : party.getTrustedMembers()) {
            trustedMembers.put(member.toString());
        }
        newPartyObject.put("trusted", trustedMembers);
        newPartyObject.put("id", party.getPartyID());

        writeFile(partyFile, newPartyObject);
    }
}
