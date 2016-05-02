package me.realized.tm.utilities.profile;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

public class NameFetcher implements Callable<Map<UUID, String>> {
    private static final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private final JSONParser jsonParser = new JSONParser();
    private final UUID uuid;

    public NameFetcher(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public Map<UUID, String> call() throws Exception {
        Map<UUID, String> uuidStringMap = new HashMap<>();
        HttpURLConnection connection = (HttpURLConnection) new URL(PROFILE_URL + uuid.toString().replace("-", "")).openConnection();
        InputStream stream = connection.getInputStream();

        if (stream.available() == 0) {
            return uuidStringMap;
        }

        JSONObject response = (JSONObject) jsonParser.parse(new InputStreamReader(stream));
        String name = (String) response.get("name");

        if (name == null) {
            return uuidStringMap;
        }

        String cause = (String) response.get("cause");
        String errorMessage = (String) response.get("errorMessage");

        if (cause != null && cause.length() > 0) {
            throw new IllegalStateException(errorMessage);
        }

        uuidStringMap.put(uuid, name);
        return uuidStringMap;
    }

    public static String getNameOf(UUID uuid) throws Exception {
        Map<UUID, String> result = new NameFetcher(uuid).call();

        if (result.isEmpty()) {
            return null;
        }

        return Collections.max(result.values());
    }
}