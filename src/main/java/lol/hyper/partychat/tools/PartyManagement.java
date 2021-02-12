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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class PartyManagement {

    /**
     * UUID is invite receiver
     * String is partyID
     */
    public static final HashMap<UUID, String> pendingInvites = new HashMap<>();

    private static FileWriter writer;
    private static FileReader reader;

    private final PartyChat partyChat;

    public PartyManagement(PartyChat partyChat) {
        this.partyChat = partyChat;
    }

    /**
     * Invite player to the party.
     *
     * @param receiver UUID of player receiving the invite.
     * @param sender   UUID of player sending the invite.
     * @param partyID  Party ID of the person inviting.
     */
    public void invitePlayer(UUID receiver, UUID sender, String partyID) {
        pendingInvites.put(receiver, partyID);
        Bukkit.getPlayer(receiver).sendMessage(ChatColor.DARK_AQUA + "You have received a party invite from " + ChatColor.GOLD + Bukkit.getPlayer(sender).getName() + ".");
        Bukkit.getPlayer(receiver).sendMessage(ChatColor.DARK_AQUA + "To join, type /party accept. To deny, type /party deny.");
        Bukkit.getPlayer(sender).sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.DARK_AQUA + "Invite sent!");
        partyChat.logger.info(sender + " sent an invite to " + receiver + " for party " + partyID);
    }

    /**
     * Remove player from pending invites list.
     *
     * @param pendingPlayer UUID of person to remove invite from.
     * @param partyID Party ID of person who is being invited.
     * @param answer Player's response to being invited.
     */
    public void removeInvite(UUID pendingPlayer, String partyID, boolean answer) {
        pendingInvites.remove(pendingPlayer);
        if (answer) {
            addPlayerToParty(pendingPlayer, partyID);
            sendPartyMessage(PartyChat.MESSAGE_PREFIX + ChatColor.DARK_AQUA + Bukkit.getPlayer(pendingPlayer).getName() + " has joined the party!", partyID);
            partyChat.logger.info("Player " + pendingPlayer + " has accepted invite for party " + partyID);
        } else {
            Bukkit.getPlayer(lookupOwner(partyID)).sendMessage(PartyChat.MESSAGE_PREFIX+ ChatColor.RED + Bukkit.getPlayer(pendingPlayer).getName() + " has denied the invite.");
            Bukkit.getPlayer(pendingPlayer).sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You denied the party invite.");
            partyChat.logger.info("Player " + pendingPlayer + " has denied invite for party " + partyID);
        }
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
        JSONParser jsonParser = new JSONParser();
        File partyFile = new File(partyChat.partyFolder.toFile(), partyID + ".json");
        JSONObject jsonObject;
        try {
            reader = new FileReader(partyFile);
            jsonObject = (JSONObject) jsonParser.parse(reader);
            reader.close();
        } catch (ParseException | IOException e) {
            partyChat.logger.severe("Unable to read party file " + partyFile.getAbsolutePath());
            e.printStackTrace();
            return;
        }
        JSONArray partyMembers = (JSONArray) jsonObject.get("members");
        partyMembers.add(newMember.toString());
        jsonObject.put("members", partyMembers);
        try {
            writer = new FileWriter(partyFile);
            writer.write(jsonObject.toJSONString());
            writer.close();
            partyChat.logger.info("Adding player " + newMember + " to party " + partyID);
        } catch (IOException e) {
            partyChat.logger.severe("Unable to write party file " + partyFile.getAbsolutePath());
            e.printStackTrace();
        }
    }

    /**
     * Update owner of a party.
     *
     * @param newOwner UUID of new party owner.
     * @param partyID  Party ID of new owner.
     */
    public void updatePartyOwner(UUID newOwner, String partyID) {
        JSONParser jsonParser = new JSONParser();
        File partyFile = new File(partyChat.partyFolder.toFile(), partyID + ".json");
        JSONObject jsonObject;
        try {
            reader = new FileReader(partyFile);
            jsonObject = (JSONObject) jsonParser.parse(reader);
            reader.close();
        } catch (ParseException | IOException e) {
            partyChat.logger.severe("Unable to read party file " + partyFile.getAbsolutePath());
            e.printStackTrace();
            return;
        }
        jsonObject.put("owner", newOwner.toString());
        try {
            writer = new FileWriter(partyFile);
            writer.write(jsonObject.toJSONString());
            writer.close();
            partyChat.logger.info("Updating party ownership for party " + partyID + ". New owner is now " + newOwner);
        } catch (IOException e) {
            partyChat.logger.severe("Unable to write party file " + partyFile.getAbsolutePath());
            e.printStackTrace();
        }
    }

    /**
     * Remove player from party.
     *
     * @param oldPlayer UUID of player being removed from party.
     * @param partyID   Party ID player is being removed from.
     */
    public void removePlayerFromParty(UUID oldPlayer, String partyID) {
        JSONParser jsonParser = new JSONParser();
        File partyFile = new File(partyChat.partyFolder.toFile(), partyID + ".json");
        JSONObject jsonObject;
        try {
            reader = new FileReader(partyFile);
            jsonObject = (JSONObject) jsonParser.parse(reader);
            reader.close();
        } catch (ParseException | IOException e) {
            partyChat.logger.severe("Unable to read party file " + partyFile.getAbsolutePath());
            e.printStackTrace();
            return;
        }
        JSONArray partyMembers = (JSONArray) jsonObject.get("members");
        partyMembers.remove(oldPlayer.toString());
        jsonObject.put("members", partyMembers);
        try {
            writer = new FileWriter(partyFile);
            writer.write(jsonObject.toJSONString());
            partyChat.logger.info("Removing player " + oldPlayer + " from party " + partyID);
            writer.close();
        } catch (IOException e) {
            partyChat.logger.severe("Unable to write party file " + partyFile.getAbsolutePath());
            e.printStackTrace();
        }
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
            JSONParser parser = new JSONParser();
            for (File currentFile : partyDirectory) {
                Object obj;
                try {
                    reader = new FileReader(currentFile);
                    obj = parser.parse(reader);
                    reader.close();
                } catch (ParseException | IOException e) {
                    e.printStackTrace();
                    partyChat.logger.severe("Unable to read party file " + currentFile.getAbsolutePath());
                    return null;
                }
                JSONObject currentJSON = (JSONObject) obj;
                JSONArray memberList = (JSONArray) currentJSON.get("members");
                if (memberList.contains(player.toString())) {
                    return FilenameUtils.removeExtension(currentFile.getName());
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
        UUID owner;
        JSONParser parser = new JSONParser();
        Object obj;
        try {
            reader = new FileReader(partyFile);
            obj = parser.parse(reader);
            reader.close();
        } catch (ParseException | IOException e) {
            partyChat.logger.severe("Unable to read party file " + partyFile.getAbsolutePath());
            e.printStackTrace();
            return null;
        }
        JSONObject currentJSON = (JSONObject) obj;
        owner = UUID.fromString(currentJSON.get("owner").toString());
        return owner;
    }

    /**
     * Send a message to everyone in a party.
     *
     * @param message Message to send the whole party.
     */
    public void sendPartyMessage(String message, String partyID) {
        JSONParser jsonParser = new JSONParser();
        File partyFile = new File(partyChat.partyFolder.toFile(), partyID + ".json");
        JSONObject jsonObject;
        try {
            reader = new FileReader(partyFile);
            jsonObject = (JSONObject) jsonParser.parse(reader);
            reader.close();
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            partyChat.logger.severe("Unable to read party file " + partyFile.getAbsolutePath());
            return;
        }
        JSONArray partyMembers = (JSONArray) jsonObject.get("members");
        for (String partyMember : (Iterable<String>) partyMembers) {
            UUID uuid = UUID.fromString(partyMember);
            if (Bukkit.getPlayer(uuid) != null) {
                Bukkit.getPlayer(uuid).sendMessage(message);
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
        JSONParser jsonParser = new JSONParser();
        File partyFile = new File(partyChat.partyFolder.toFile(), partyID + ".json");
        JSONObject jsonObject;
        try {
            reader = new FileReader(partyFile);
            jsonObject = (JSONObject) jsonParser.parse(reader);
            reader.close();
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            return null;
        }
        ArrayList<UUID> partyArray = new ArrayList<>();
        JSONArray partyMembers = (JSONArray) jsonObject.get("members");
        for (String partyMember : (Iterable<String>) partyMembers) {
            partyArray.add(UUID.fromString(partyMember));
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
        members.add(player.toString());
        partyObject.put("members", members);

        File partyFile = new File(partyChat.partyFolder.toFile(), random.toString() + ".json");
        try {
            writer = new FileWriter(partyFile);
            writer.write(partyObject.toJSONString());
            writer.close();
            partyChat.logger.info("Creating new party with owner " + player);
        } catch (IOException e) {
            partyChat.logger.severe("Unable to write party file " + partyFile.getAbsolutePath());
            e.printStackTrace();
        }
    }
}