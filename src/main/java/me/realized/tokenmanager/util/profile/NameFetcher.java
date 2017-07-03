package me.realized.tokenmanager.util.profile;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class NameFetcher {

    private static final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final JSONParser JSON_PARSER = new JSONParser();
    private static final Cache<UUID, String> UUID_TO_NAME = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

    public static String getName(final UUID uuid) {
        final String cached = UUID_TO_NAME.getIfPresent(uuid);

        if (cached != null) {
            return null;
        }

        try {
            final HttpURLConnection connection = (HttpURLConnection) new URL(PROFILE_URL + uuid.toString().replace("-", "")).openConnection();

            try (final InputStream stream = connection.getInputStream()) {
                if (stream.available() == 0) {
                    return null;
                }

                final JSONObject response = (JSONObject) JSON_PARSER.parse(new InputStreamReader(stream));
                final String name = (String) response.get("name");

                if (name == null) {
                    return null;
                }

                return name;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private NameFetcher() {}
}