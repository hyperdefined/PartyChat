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
import lol.hyper.partychat.party.Party;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

public class PartyManagement {

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
     *
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
     *
     * @param file        File to write data to.
     * @param jsonToWrite Data to write to file. This much be a JSON string.
     */
    public void writeFile(File file, JSONObject jsonToWrite) {
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
     * Delete a party.
     *
     * @param party The party to delete.
     */
    public void deleteParty(Party party) {
        File partyFile = new File(partyChat.partyFolder.toFile(), party.partyID() + ".json");
        if (!partyFile.delete()) {
            partyChat.logger.warning("Cannot delete party! Please delete: " + partyFile.getAbsolutePath());
            return;
        }
        partyChat.logger.info("Deleting party " + party.partyID());
        partyChat.loadedParties.remove(party);
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
        Party newParty = new Party(partyChat, newUUID.toString());
        newParty.setPartyOwner(player);
        newParty.addPartyMember(player);
        partyChat.loadedParties.add(newParty);
    }

    /**
     * Gets a loaded party based on a player's UUID.
     *
     * @param player The player to look for.
     * @return The party.
     */
    public Party getParty(UUID player) {
        for (Party party : partyChat.loadedParties) {
            if (party.partyMembers().contains(player)) {
                return party;
            }
        }
        return null;
    }

    /**
     * Load all parties from the disk.
     */
    public void loadParties() {
        partyChat.logger.info("Loading parties...");
        File[] partyDirectory = partyChat.partyFolder.toFile().listFiles();
        if (partyDirectory != null) {
            for (File currentFile : partyDirectory) {
                JSONObject partyJSON = readFile(currentFile);
                Set<UUID> members = new HashSet<>();
                Set<UUID> tMembers = new HashSet<>();
                String partyID = partyJSON.getString("id");
                UUID owner = null;
                if (partyJSON.has("members")) {
                    JSONArray partyMembers = partyJSON.getJSONArray("members");
                    for (int i = 0; i < partyMembers.length(); i++) {
                        members.add(UUID.fromString(partyMembers.getString(i)));
                    }
                }

                if (partyJSON.has("trusted")) {
                    JSONArray trustedMembers = partyJSON.getJSONArray("trusted");
                    for (int i = 0; i < trustedMembers.length(); i++) {
                        tMembers.add(UUID.fromString(trustedMembers.getString(i)));
                    }
                }

                if (partyJSON.has("owner")) {
                    owner = UUID.fromString(partyJSON.getString("owner"));
                }

                if (owner == null || members.isEmpty()) {
                    partyChat.logger.warning("Party ID " + partyID + " does NOT have an owner or members! Raw data: " + partyJSON);
                    continue;
                }

                Party party = new Party(partyChat, partyID);
                party.setPartyOwner(owner);
                party.setPartyMembers(members);
                party.setTrustedMembers(tMembers);
                partyChat.loadedParties.add(party);
            }
        }
        partyChat.logger.info("Loaded " + partyChat.loadedParties.size() + " parties.");
    }
}
