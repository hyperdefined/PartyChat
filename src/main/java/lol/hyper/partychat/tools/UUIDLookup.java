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

import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class UUIDLookup {

    public static String getName(UUID player) {
        String url = "https://api.mojang.com/user/profiles/" + player.toString().replace("-", "") + "/names";
        try {
            String nameJson = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
            JSONArray nameValue = new JSONArray(nameJson);
            JSONObject nameObject = nameValue.getJSONObject(nameValue.length() - 1);
            return nameObject.get("name").toString();
        } catch (IOException e) {
            Bukkit.getLogger().severe("Unable to lookup UUID for player " + player);
            e.printStackTrace();
        }
        return "error";
    }
}
