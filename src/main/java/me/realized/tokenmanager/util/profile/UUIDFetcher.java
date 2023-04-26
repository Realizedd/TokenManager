package me.realized.tokenmanager.util.profile;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Modified version of UUIDFetcher by evilmidget38
 **/

final class UUIDFetcher {

    private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
    private static final JsonParser JSON_PARSER = new JsonParser();
    private static final Gson GSON = new Gson();
    private static final Cache<String, UUID> NAME_TO_UUID = CacheBuilder.newBuilder()
        .concurrencyLevel(4)
        .maximumSize(1000)
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .build();

    private UUIDFetcher() {}

    static String getUUID(final String name) throws Exception {
        final UUID cached = NAME_TO_UUID.getIfPresent(name);

        if (cached != null) {
            return cached.toString();
        }

        final HttpURLConnection connection = createConnection();
        final String body = GSON.toJson(Collections.singletonList(name));
        writeBody(connection, body);

        try (Reader reader = new InputStreamReader(connection.getInputStream())) {
            JsonArray array = (JsonArray) JSON_PARSER.parse(reader);
            final JsonObject profile = (JsonObject) array.get(0);
            final UUID uuid;
            NAME_TO_UUID.put(profile.get("name").getAsString(), uuid = get(profile.get("id").getAsString()));
            return uuid.toString();
        }
    }

    private static void writeBody(HttpURLConnection connection, String body) throws Exception {
        OutputStream stream = connection.getOutputStream();
        stream.write(body.getBytes());
        stream.flush();
        stream.close();
    }

    private static HttpURLConnection createConnection() throws Exception {
        URL url = new URL(PROFILE_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }

    private static UUID get(String id) {
        return UUID.fromString(
            id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
    }
}
