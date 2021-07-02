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
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class PartyManagement {

    private static FileWriter writer;
    /**
     * invite receiver
     * invite sender
     */
    public final HashMap<UUID, UUID> pendingInvites = new HashMap<>();

    private final PartyChat partyChat;

    public PartyManagement(PartyChat partyChat) {
        this.partyChat = partyChat;
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
            writer = new FileWriter(file);
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
        Bukkit.getPlayer(receiver)
                .sendMessage(PartyChat.MESSAGE_PREFIX + "You have received a party invite from " + ChatColor.GOLD
                        + Bukkit.getPlayer(sender).getName() + ".");
        Bukkit.getPlayer(receiver)
                .sendMessage(ChatColor.DARK_AQUA + "To join, type /party accept. To deny, type /party deny.");
        Bukkit.getPlayer(sender).sendMessage(PartyChat.MESSAGE_PREFIX + "Invite sent!");
        partyChat.logger.info(Bukkit.getPlayer(sender).getName() + " sent an invite to " + Bukkit.getPlayer(receiver).getName() + " for party " + partyID);
    }

    /**
     * Remove player from pending invites list.
     *
     * @param pendingPlayer UUID of person to remove invite from.
     * @param answer Player's response to being invited.
     */
    public void removeInvite(UUID pendingPlayer, boolean answer) {
        String player = Bukkit.getPlayer(pendingPlayer).getName();
        String partyID = lookupParty(pendingInvites.get(pendingPlayer));
        if (answer) {
            addPlayerToParty(pendingPlayer, partyID);
            sendPartyMessage(PartyChat.MESSAGE_PREFIX + player + " has joined the party!", partyID);
            partyChat.logger.info(player + " has accepted invite for party " + partyID);
        } else {
            Bukkit.getPlayer(pendingInvites.get(pendingPlayer)).sendMessage(PartyChat.MESSAGE_PREFIX + player + " has denied the invite.");
            Bukkit.getPlayer(pendingPlayer).sendMessage(PartyChat.MESSAGE_PREFIX + "You denied the party invite.");
            partyChat.logger.info(player + " has denied invite for party " + partyID);
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
        File partyFile = new File(partyChat.partyFolder.toFile(), partyID + ".json");
        JSONObject jsonObject = readFile(partyFile);
        JSONArray partyMembers = jsonObject.getJSONArray("members");
        partyMembers.put(newMember.toString());
        jsonObject.put("members", partyMembers);
        writeFile(partyFile, jsonObject);
        String player = Bukkit.getPlayer(newMember).getName();
        partyChat.logger.info("Adding player " + player + "to party " + partyID);
    }

    /**
     * Update owner of a party.
     *
     * @param newOwner UUID of new party owner.
     * @param partyID  Party ID of new owner.
     */
    public void updatePartyOwner(UUID newOwner, String partyID) {
        File partyFile = new File(partyChat.partyFolder.toFile(), partyID + ".json");
        JSONObject jsonObject = readFile(partyFile);
        jsonObject.put("owner", newOwner.toString());
        writeFile(partyFile, jsonObject);
        String player = Bukkit.getPlayer(newOwner).getName();
        partyChat.logger.info("Party " + partyID + " is now owned by " + player);
    }

    /**
     * Remove player from party.
     *
     * @param oldPlayer UUID of player being removed from party.
     * @param partyID   Party ID player is being removed from.
     */
    public void removePlayerFromParty(UUID oldPlayer, String partyID) {
        File partyFile = new File(partyChat.partyFolder.toFile(), partyID + ".json");
        JSONObject jsonObject = readFile(partyFile);
        JSONArray partyMembers = jsonObject.getJSONArray("members");
        for (int i = 0; i < partyMembers.length(); i++) {
            String player = partyMembers.getString(i);
            if (oldPlayer.toString().equalsIgnoreCase(player)) {
                partyMembers.remove(i);
            }
        }
        if (jsonObject.has("trusted")) {
            JSONArray trusted = jsonObject.getJSONArray("trusted");
            boolean didWeRemove = false;
            for (int i = 0; i < trusted.length(); i++) {
                String player = trusted.getString(i);
                if (oldPlayer.toString().equalsIgnoreCase(player)) {
                    trusted.remove(i);
                    didWeRemove = true;
                    break;
                }
            }
            if (didWeRemove) {
                jsonObject.put("trusted", trusted);
            }
        }
        jsonObject.put("members", partyMembers);
        writeFile(partyFile, jsonObject);
        String player = Bukkit.getPlayer(oldPlayer).getName();
        partyChat.logger.info(player + " has left party " + partyID);
    }

    /**
     * Check if player is in a party.
     *
     * @param player UUID of player to check party.
     * @return Returns their party id if they have one.
     */
    public String lookupParty(UUID player) {
        File[] partyDirectory = partyChat.partyFolder.toFile().listFiles();
        if (partyDirectory != null) {
            for (File currentFile : partyDirectory) {
                JSONObject currentJSON = readFile(currentFile);
                JSONArray partyMembers = currentJSON.getJSONArray("members");
                for (int i = 0; i < partyMembers.length(); i++) {
                    if (partyMembers.getString(i).equalsIgnoreCase(player.toString())) {
                        return FilenameUtils.removeExtension(currentFile.getName());
                    }
                }
            }
        }
        return null;
    }

    /**
     * Check if player is the party owner.
     *
     * @param player UUID of player to check.
     * @return Returns returns if player is owner.
     */
    public boolean isPlayerOwner(UUID player) {
        return lookupOwner(lookupParty(player)).equals(player);
    }

    /**
     * Looks up who the owner of a party is.
     *
     * @param partyID Party ID to see who owner is.
     * @return returns their party id if they have one
     */
    public UUID lookupOwner(String partyID) {
        File partyFile = new File(partyChat.partyFolder.toFile(), partyID + ".json");
        JSONObject currentJSON = readFile(partyFile);
        return UUID.fromString(currentJSON.get("owner").toString());
    }

    /**
     * Send a message to everyone in a party.
     *
     * @param message Message to send the whole party.
     */
    public void sendPartyMessage(String message, String partyID) {
        File partyFile = new File(partyChat.partyFolder.toFile(), partyID + ".json");
        JSONObject jsonObject = readFile(partyFile);
        JSONArray partyMembers = jsonObject.getJSONArray("members");
        for (Object partyMember : partyMembers) {
            UUID uuid = UUID.fromString((String) partyMember);
            if (Bukkit.getPlayer(uuid) != null) {
                Bukkit.getPlayer(uuid).sendMessage(PartyChat.MESSAGE_PREFIX + message);
            }
        }
    }

    /**
     * Get and array of party members.
     *
     * @param partyID Party ID to get list of members.
     * @return returns the list of party members
     */
    public ArrayList<UUID> listPartyMembers(String partyID) {
        File partyFile = new File(partyChat.partyFolder.toFile(), partyID + ".json");
        JSONObject jsonObject = readFile(partyFile);
        ArrayList<UUID> partyArray = new ArrayList<>();
        JSONArray partyMembers = jsonObject.getJSONArray("members");
        for (int i = 0; i < partyMembers.length(); i++) {
            partyArray.add(UUID.fromString(partyMembers.getString(i)));
        }
        return partyArray;
    }

    /**
     * Create a party.
     *
     * @param player UUID of new party owner.
     */
    public void createParty(UUID player) {
        String charset = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder random = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            random.append(charset.charAt(new Random().nextInt(charset.length() - 1)));
        }
        JSONObject partyObject = new JSONObject();
        partyObject.put("owner", player.toString());
        partyObject.put("id", random.toString());
        JSONArray members = new JSONArray();
        members.put(player.toString());
        partyObject.put("members", members);

        File partyFile = new File(partyChat.partyFolder.toFile(), random + ".json");
        writeFile(partyFile, partyObject);
        String owner = Bukkit.getPlayer(player).getName();
        partyChat.logger.info("Party " + random + " has been created by " + owner);
    }

    /**
     * Trust player in a party.
     * @param player Player to trust.
     */
    public void trustPlayer(UUID player) {
        String partyID = lookupParty(player);
        File partyFile = new File(partyChat.partyFolder.toFile(), partyID + ".json");
        JSONObject jsonObject = readFile(partyFile);
        // throws exception if this doesn't exist, so we have to check it since this trusted feature is new
        JSONArray trusted;
        if (jsonObject.has("trusted")) {
            trusted = new JSONArray(jsonObject.getJSONArray("trusted"));
        } else {
            trusted = new JSONArray();
        }
        trusted.put(player.toString());
        jsonObject.put("trusted", trusted);
        writeFile(partyFile, jsonObject);
        String trustedPlayer = Bukkit.getPlayer(player).getName();
        sendPartyMessage(trustedPlayer + " has become a trusted member.", partyID);
        partyChat.logger.info(trustedPlayer + " is now a trusted player of " + partyID);
    }

    /**
     * Check if a player is trusted.
     * @param player Player to check.
     * @return True if the player is trusted, false if not.
     */
    public boolean checkTrusted(UUID player) {
        String partyID = lookupParty(player);
        File partyFile = new File(partyChat.partyFolder.toFile(), partyID + ".json");
        JSONObject jsonObject = readFile(partyFile);
        // throws exception if this doesn't exist, so we have to check it since this trusted feature is new
        if (jsonObject.has("trusted")) {
            JSONArray trusted = new JSONArray(jsonObject.getJSONArray("trusted"));
            for (int i = 0; i < trusted.length(); i++) {
                if (trusted.getString(i).equalsIgnoreCase(player.toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Remove a player's trust rank.
     * @param player Player to remove.
     */
    public void removeTrustedPlayer(UUID player) {
        String partyID = lookupParty(player);
        File partyFile = new File(partyChat.partyFolder.toFile(), partyID + ".json");
        JSONObject jsonObject = readFile(partyFile);
        // throws exception if this doesn't exist, so we have to check it since this trusted feature is new
        JSONArray trusted = new JSONArray(jsonObject.getJSONArray("trusted"));
        for (int i = 0; i < trusted.length(); i++) {
            if (trusted.getString(i).equalsIgnoreCase(player.toString())) {
                trusted.remove(i);
                break;
            }
        }
        jsonObject.put("trusted", trusted);
        writeFile(partyFile, jsonObject);
        sendPartyMessage(Bukkit.getPlayer(player).getName() + " has been removed as a trusted member.", partyID);
        String trustedPlayer = Bukkit.getPlayer(player).getName();
        partyChat.logger.info(trustedPlayer + " is no longer a trusted player of " + partyID);
    }
}
