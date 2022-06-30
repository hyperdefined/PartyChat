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

import java.util.ArrayList;
import java.util.UUID;

public class Party {

    private final ArrayList<UUID> partyMembers = new ArrayList<>();
    private final ArrayList<UUID> trustedMembers = new ArrayList<>();
    private UUID partyOwner;
    private final String partyID;

    public Party(String partyID) {
        this.partyID = partyID;
    }

    public UUID getPartyOwner() {
        return partyOwner;
    }

    public void setPartyOwner(UUID partyOwner) {
        this.partyOwner = partyOwner;
    }

    public ArrayList<UUID> getPartyMembers() {
        return partyMembers;
    }

    public void removePartyMember(UUID player) {
        partyMembers.remove(player);
    }

    public void addPartyMember(UUID player) {
        partyMembers.add(player);
    }

    public void addTrustedMember(UUID player) {
        trustedMembers.add(player);
    }

    public void removeTrustedMember(UUID player) {
        trustedMembers.add(player);
    }

    public ArrayList<UUID> getTrustedMembers() {
        return trustedMembers;
    }

    public String getPartyID() {
        return partyID;
    }
}
