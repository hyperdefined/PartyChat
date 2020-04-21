/**
 * CommandParty.java
 * Created on 4/15/2020
 * - hyperdefined
 */

package lol.hyper.partychat.tools;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class UUIDLookup {
    public static String getName(UUID player) {
        String url = "https://api.mojang.com/user/profiles/" + player.toString().replace("-", "") + "/names";
        try {
            String nameJson = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
            JSONArray nameValue = (JSONArray) JSONValue.parseWithException(nameJson);
            String playerSlot = nameValue.get(nameValue.size() - 1).toString();
            JSONObject nameObject = (JSONObject) JSONValue.parseWithException(playerSlot);
            return nameObject.get("name").toString();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return "error";
    }
}