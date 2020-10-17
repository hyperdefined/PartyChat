/*
  PartyMangement.java
  Created on 4/15/2020
  - hyperdefined
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

    /**
     * Invite player to the party.
     *
     * @param receiver UUID of player receiving the invite.
     * @param sender   UUID of player sending the invite.
     * @param partyID  Party ID of the person inviting.
     */
    public static void invitePlayer(UUID receiver, UUID sender, String partyID) {
        pendingInvites.put(receiver, partyID);
        Bukkit.getPlayer(receiver).sendMessage(ChatColor.DARK_AQUA + "You have received a party invite from " + ChatColor.GOLD + Bukkit.getPlayer(sender).getName() + ".");
        Bukkit.getPlayer(receiver).sendMessage(ChatColor.DARK_AQUA + "To join, type /party accept. To deny, type /party deny.");
        Bukkit.getPlayer(sender).sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.DARK_AQUA + "Invite sent!");
    }

    /**
     * Remove player from pending invites list.
     *
     * @param pendingPlayer UUID of person to remove invite from.
     * @param partyID Party ID of person who is being invited.
     * @param answer Player's response to being invited.
     */
    public static void removeInvite(UUID pendingPlayer, String partyID, boolean answer) {
        pendingInvites.remove(pendingPlayer);
        if (answer) {
            addPlayerToParty(pendingPlayer, partyID);
            sendPartyMessage(PartyChat.MESSAGE_PREFIX + ChatColor.DARK_AQUA + Bukkit.getPlayer(pendingPlayer).getName() + " has joined the party!", partyID);
        } else {
            Bukkit.getPlayer(lookupOwner(partyID)).sendMessage(PartyChat.MESSAGE_PREFIX+ ChatColor.RED + Bukkit.getPlayer(pendingPlayer).getName() + " has denied the invite.");
            Bukkit.getPlayer(pendingPlayer).sendMessage(PartyChat.MESSAGE_PREFIX + ChatColor.RED + "You denied the party invite.");
        }
    }

    /**
     * Delete a party.
     *
     * @param partyID Party ID of party to delete.
     */
    public static void deleteParty(String partyID) {
        File partyFile = new File(PartyChat.getInstance().partyFolder.toFile(), partyID + ".json");
        if (!partyFile.delete()) {
            Bukkit.getLogger().warning("Cannot delete party! Please delete: " + partyFile.getAbsolutePath());
        }
    }

    /**
     * Add given player to given party.
     *
     * @param newMember UUID of new player to add to party.
     * @param partyID   Party ID the new player is joining.
     */
    public static void addPlayerToParty(UUID newMember, String partyID) {
        JSONParser jsonParser = new JSONParser();
        File partyFile = new File(PartyChat.getInstance().partyFolder.toFile(), partyID + ".json");
        JSONObject jsonObject;
        try {
            reader = new FileReader(partyFile);
            jsonObject = (JSONObject) jsonParser.parse(reader);
            reader.close();
        } catch (ParseException | IOException e) {
            Bukkit.getLogger().severe("Unable to read party file " + partyFile.getAbsolutePath());
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
        } catch (IOException e) {
            Bukkit.getLogger().severe("Unable to write party file " + partyFile.getAbsolutePath());
            e.printStackTrace();
        }
    }

    /**
     * Update owner of a party.
     *
     * @param newOwner UUID of new party owner.
     * @param partyID  Party ID of new owner.
     */
    public static void updatePartyOwner(UUID newOwner, String partyID) {
        JSONParser jsonParser = new JSONParser();
        File partyFile = new File(PartyChat.getInstance().partyFolder.toFile(), partyID + ".json");
        JSONObject jsonObject;
        try {
            reader = new FileReader(partyFile);
            jsonObject = (JSONObject) jsonParser.parse(reader);
            reader.close();
        } catch (ParseException | IOException e) {
            Bukkit.getLogger().severe("Unable to read party file " + partyFile.getAbsolutePath());
            e.printStackTrace();
            return;
        }
        jsonObject.put("owner", newOwner.toString());
        try {
            writer = new FileWriter(partyFile);
            writer.write(jsonObject.toJSONString());
            writer.close();
        } catch (IOException e) {
            Bukkit.getLogger().severe("Unable to write party file " + partyFile.getAbsolutePath());
            e.printStackTrace();
        }
    }

    /**
     * Remove player from party.
     *
     * @param oldPlayer UUID of player being removed from party.
     * @param partyID   Party ID player is being removed from.
     */
    public static void removePlayerFromParty(UUID oldPlayer, String partyID) {
        JSONParser jsonParser = new JSONParser();
        File partyFile = new File(PartyChat.getInstance().partyFolder.toFile(), partyID + ".json");
        JSONObject jsonObject;
        try {
            reader = new FileReader(partyFile);
            jsonObject = (JSONObject) jsonParser.parse(reader);
            reader.close();
        } catch (ParseException | IOException e) {
            Bukkit.getLogger().severe("Unable to read party file " + partyFile.getAbsolutePath());
            e.printStackTrace();
            return;
        }
        JSONArray partyMembers = (JSONArray) jsonObject.get("members");
        partyMembers.remove(oldPlayer.toString());
        jsonObject.put("members", partyMembers);
        try {
            writer = new FileWriter(partyFile);
            writer.write(jsonObject.toJSONString());
            writer.close();
        } catch (IOException e) {
            Bukkit.getLogger().severe("Unable to write party file " + partyFile.getAbsolutePath());
            e.printStackTrace();
        }
    }

    /**
     * Check if player is in a party.
     *
     * @param player UUID of player to check party.
     * @return Returns their party id if they have one.
     */
    public static String lookupParty(UUID player) {
        File[] partyDirectory = PartyChat.getInstance().partyFolder.toFile().listFiles();
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
                    Bukkit.getLogger().severe("Unable to read party file " + currentFile.getAbsolutePath());
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
    public static boolean isPlayerOwner(UUID player) {
        return lookupOwner(lookupParty(player)).equals(player);
    }

    /**
     * Looks up who the owner of a party is.
     *
     * @param partyID Party ID to see who owner is.
     * @return returns their party id if they have one
     */
    public static UUID lookupOwner(String partyID) {
        File partyFile = new File(PartyChat.getInstance().partyFolder.toFile(), partyID + ".json");
        UUID owner;
        JSONParser parser = new JSONParser();
        Object obj;
        try {
            reader = new FileReader(partyFile);
            obj = parser.parse(reader);
            reader.close();
        } catch (ParseException | IOException e) {
            Bukkit.getLogger().severe("Unable to read party file " + partyFile.getAbsolutePath());
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
    public static void sendPartyMessage(String message, String partyID) {
        JSONParser jsonParser = new JSONParser();
        File partyFile = new File(PartyChat.getInstance().partyFolder.toFile(), partyID + ".json");
        JSONObject jsonObject;
        try {
            reader = new FileReader(partyFile);
            jsonObject = (JSONObject) jsonParser.parse(reader);
            reader.close();
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("Unable to read party file " + partyFile.getAbsolutePath());
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
    public static ArrayList<UUID> listPartyMembers(String partyID) {
        JSONParser jsonParser = new JSONParser();
        File partyFile = new File(PartyChat.getInstance().partyFolder.toFile(), partyID + ".json");
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
    public static void createParty(UUID player) {
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

        File partyFile = new File(PartyChat.getInstance().partyFolder.toFile(), random.toString() + ".json");
        try {
            writer = new FileWriter(partyFile);
            writer.write(partyObject.toJSONString());
            writer.close();
        } catch (IOException e) {
            Bukkit.getLogger().severe("Unable to write party file " + partyFile.getAbsolutePath());
            e.printStackTrace();
        }
    }
}