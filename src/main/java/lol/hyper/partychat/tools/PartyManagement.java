/**
 * PartyMangement.java
 * Created on 4/15/2020
 * - hyperdefined
 */

package lol.hyper.partychat.tools;

import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class PartyManagement {

    /**
     * Player is invite receiver
     * String is partyID
     */
    public static final HashMap<Player, String> pendingInvites = new HashMap<>();

    private static FileWriter writer;
    private static FileReader reader;

    /**
     * Invite player to the party.
     *
     * @param receiver player who is receiving the invite
     * @param sender   player who sent the invite
     * @param partyID  partyID of inviter
     */
    public static void invitePlayer(Player receiver, Player sender, String partyID) {
        pendingInvites.put(receiver, partyID);
        receiver.sendMessage(ChatColor.BLUE + "You have received a party invite from " + sender.getName() + ".");
        receiver.sendMessage(ChatColor.BLUE + "To join, type /party accept. To deny, type /party deny.");
        sender.sendMessage(ChatColor.GREEN + "Invite sent!");
    }

    /**
     * Remove player from pending invites list.
     *
     * @param pendingPlayer player to remove pending invite
     */
    public static void removeInvite(Player pendingPlayer, String partyID, boolean answer) throws IOException, ParseException {
        pendingInvites.remove(pendingPlayer);
        if (answer) {
            addPlayerToParty(pendingPlayer, partyID);
            sendPartyMessage(ChatColor.GREEN + pendingPlayer.getName() + " has joined the party!", partyID);
        } else {
            Bukkit.getPlayer(lookupOwner(partyID)).sendMessage(ChatColor.RED + pendingPlayer.getName() + " has denied the invite.");
        }
    }

    /**
     * Delete a party.
     *
     * @param partyID party to delete
     */
    public static void deleteParty(String partyID) {
        File partyFile = new File("parties/" + partyID + ".json");
        if (!partyFile.delete()) {
            Bukkit.getLogger().warning("Cannot delete party! Please delete: " + partyFile.getAbsolutePath());
        }
    }

    /**
     * Add given player to given party.
     *
     * @param newMember player to add party to
     * @param partyID   party to add player to
     */
    public static void addPlayerToParty(Player newMember, String partyID) throws IOException, ParseException {
        String UUID = newMember.getUniqueId().toString();
        JSONParser jsonParser = new JSONParser();
        reader = new FileReader("parties/" + partyID + ".json");
        JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
        reader.close();
        JSONArray partyMembers = (JSONArray) jsonObject.get("members");
        partyMembers.add(UUID);
        jsonObject.put("members", partyMembers);
        writer = new FileWriter("parties/" + partyID + ".json");
        writer.write(jsonObject.toJSONString());
        writer.close();
    }

    /**
     * Update owner of a party.
     *
     * @param newOwner new owner of party
     * @param partyID  party to add player to
     */
    public static void updatePartyOwner(Player newOwner, String partyID) throws IOException, ParseException {
        String UUID = newOwner.getUniqueId().toString();
        JSONParser jsonParser = new JSONParser();
        reader = new FileReader("parties/" + partyID + ".json");
        JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
        reader.close();
        jsonObject.put("owner", UUID);
        writer = new FileWriter("parties/" + partyID + ".json");
        writer.write(jsonObject.toJSONString());
        writer.close();
    }

    /**
     * Remove player from party.
     *
     * @param oldPlayer player to remove party from
     * @param partyID   party to remove player from
     */
    public static void removePlayerFromParty(Player oldPlayer, String partyID) throws IOException, ParseException {
        String UUID = oldPlayer.getUniqueId().toString();
        JSONParser jsonParser = new JSONParser();
        reader = new FileReader("parties/" + partyID + ".json");
        JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
        reader.close();
        JSONArray partyMembers = (JSONArray) jsonObject.get("members");
        partyMembers.remove(UUID);
        jsonObject.put("members", partyMembers);
        writer = new FileWriter("parties/" + partyID + ".json");
        writer.write(jsonObject.toJSONString());
        writer.close();
    }

    /**
     * Check if player is in a party.
     *
     * @param player player to look up their party
     * @return returns their party id if they have one
     * @throws IOException
     * @throws ParseException
     */
    public static String lookupParty(Player player) throws IOException, ParseException {
        File partyFolder = new File("parties");
        File[] partyDirectory = partyFolder.listFiles();
        if (partyDirectory != null) {
            JSONParser parser = new JSONParser();
            for (File currentFile : partyDirectory) {
                reader = new FileReader(currentFile);
                Object obj = parser.parse(reader);
                reader.close();
                JSONObject currentJSON = (JSONObject) obj;
                JSONArray memberList = (JSONArray) currentJSON.get("members");
                if (memberList.contains(player.getUniqueId().toString())) {
                    return FilenameUtils.removeExtension(currentFile.getName());
                }
            }
        }
        return null;
    }

    /**
     * Check if player is the party owner.
     *
     * @param player player to check if they are owner
     * @return returns returns if player is owner
     * @throws IOException
     * @throws ParseException
     */
    public static boolean isPlayerOwner(Player player) throws IOException, ParseException {
        return lookupOwner(lookupParty(player)).toString().equals(player.getUniqueId().toString());
    }

    /**
     * Looks up who the owner of a party is.
     *
     * @param partyID partyID to find owner
     * @return returns their party id if they have one
     * @throws IOException
     * @throws ParseException
     */
    public static UUID lookupOwner(String partyID) throws IOException, ParseException {
        File partyFile = new File("parties/" + partyID + ".json");
        UUID owner;
        JSONParser parser = new JSONParser();
        reader = new FileReader(partyFile);
        Object obj = parser.parse(reader);
        reader.close();
        JSONObject currentJSON = (JSONObject) obj;
        owner = UUID.fromString(currentJSON.get("owner").toString());
        return owner;
    }

    /**
     * Send a message to everyone in a party.
     *
     * @param message message to send to members
     * @return returns their party id if they have one
     * @throws IOException
     * @throws ParseException
     */
    public static void sendPartyMessage(String message, String partyID) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        reader = new FileReader("parties/" + partyID + ".json");
        JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
        reader.close();
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
     * @param partyID party to list members for
     * @return returns the list of party members
     * @throws IOException
     * @throws ParseException
     */
    public static ArrayList<String> listPartyMembers(String partyID) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        reader = new FileReader("parties/" + partyID + ".json");
        JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
        reader.close();
        ArrayList<String> partyArray = new ArrayList<>();
        JSONArray partyMembers = (JSONArray) jsonObject.get("members");
        for (String partyMember : (Iterable<String>) partyMembers) {
            partyArray.add(partyMember);
        }
        return partyArray;
    }

    /**
     * Create a party.
     *
     * @param player player who created the party
     */
    public static void createParty(Player player) throws IOException {
        String charset = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder random = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            random.append(charset.charAt(new Random().nextInt(charset.length() - 1)));
        }
        JSONObject partyFile = new JSONObject();
        partyFile.put("owner", player.getUniqueId().toString());
        partyFile.put("id", random.toString());
        JSONArray members = new JSONArray();
        members.add(player.getUniqueId().toString());
        partyFile.put("members", members);

        writer = new FileWriter("parties/" + random.toString() + ".json");
        writer.write(partyFile.toJSONString());
        writer.close();
    }
}