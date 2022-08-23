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

import java.util.UUID;

public class Invite {

    private final UUID sender;
    private final UUID receiver;

    public Invite(Party party, UUID sender, UUID receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    public UUID getReceiver() {
        return receiver;
    }

    public UUID getSender() {
        return sender;
    }
}
