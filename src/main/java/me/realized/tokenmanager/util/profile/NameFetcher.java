package me.realized.tokenmanager.util.profile;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import me.realized.tokenmanager.util.Callback;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public final class NameFetcher {

    private static class NameCollectorTask implements Runnable {

        private final List<UUID> uuids;
        private final Callback<Map<UUID, String>> callback;

        private final Map<UUID, String> names = new HashMap<>();

        NameCollectorTask(final List<UUID> uuids, final Callback<Map<UUID, String>> callback) {
            this.uuids = uuids;
            this.callback = callback;
        }

        @Override
        public void run() {
            if (uuids.isEmpty()) {
                callback.call(names);
                return;
            }

            final UUID next = uuids.remove(uuids.size() - 1);
            final String result = ProfileUtil.getName(next);

            if (result != null) {
                names.put(next, result);
            }

            // Run with delay to prevent being blocked by Mojang
            EXECUTOR_SERVICE.schedule(this, 200L, TimeUnit.MILLISECONDS);
        }
    }

    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    private static final String URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final JSONParser JSON_PARSER = new JSONParser();
    private static final Cache<UUID, String> UUID_TO_NAME = CacheBuilder.newBuilder()
        .concurrencyLevel(4)
        .maximumSize(1000)
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .build();

    private NameFetcher() {}

    public static void getNames(final List<UUID> uuids, final Callback<Map<UUID, String>> callback) {
        EXECUTOR_SERVICE.schedule(new NameCollectorTask(uuids, callback), 0L, TimeUnit.MILLISECONDS);
    }

    public static String getName(final UUID uuid) {
        final String cached = UUID_TO_NAME.getIfPresent(uuid);

        if (cached != null) {
            return cached;
        }

        try {
            final HttpURLConnection connection = (HttpURLConnection) new URL(URL + uuid.toString().replace("-", ""))
                .openConnection();

            try (final InputStream stream = connection.getInputStream()) {
                if (stream.available() == 0) {
                    return null;
                }

                final JSONObject response = (JSONObject) JSON_PARSER.parse(new InputStreamReader(stream));
                final String name = (String) response.get("name");

                if (name != null) {
                    UUID_TO_NAME.put(uuid, name);
                    return name;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}