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

package lol.hyper.partychat.commands;

import lol.hyper.partychat.PartyChat;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CommandPartyChatMessage implements TabExecutor {

    private final PartyChat partyChat;
    // anyone on this list has party chat enabled
    public final ArrayList<UUID> partyChatEnabled = new ArrayList<>();
    private final BukkitAudiences audiences;
    private final MiniMessage miniMessage;

    public CommandPartyChatMessage(PartyChat partyChat) {
        this.partyChat = partyChat;
        this.audiences = partyChat.getAdventure();
        this.miniMessage = partyChat.miniMessage;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("errors.must-be-a-player")));
            return true;
        }
        UUID player = Bukkit.getPlayerExact(sender.getName()).getUniqueId();
        if (args.length < 1) {
            audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.pc.invalid-syntax")));
            return true;
        }
        if (partyChat.partyManagement.loadParty(player) == null) {
            audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("errors.not-in-a-party")));
            return true;
        }
        String arg = args[0];
        if (arg.equalsIgnoreCase("on") || arg.equalsIgnoreCase("off")) {
            if (arg.equalsIgnoreCase("on")) {
                partyChatEnabled.add(player);
                audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.pc.enabled")));
            }
            if (arg.equalsIgnoreCase("off")) {
                partyChatEnabled.remove(player);
                audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.pc.disabled")));
            }
        } else {
            audiences.sender(sender).sendMessage(miniMessage.deserialize(partyChat.getMessage("commands.pc.invalid-syntax")));
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length > 0) {
            return Arrays.asList("on", "off");
        } else {
            return null;
        }
    }
}
