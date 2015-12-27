/*
 * Copyright (C) 2015 evilmidget38
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.realized.tm.utilities;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * All credit to evilmidget38! A small bit of cleanup for Java 8. This class can
 * dynamically retrieve the relevant {@link UUID}s for one or multiple players
 * on the server
 *
 * @since 0.0.1
 * @author evilmidget38
 * @author 1Rogue (Cleanup / Documentation)
 * @version 0.1.0
 */
public class UUIDFetcher implements Callable<Map<String, UUID>> {

    private static final double PROFILES_PER_REQUEST = 100;
    private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
    private final JSONParser jsonParser = new JSONParser();
    private final List<String> names;
    private final boolean rateLimiting;

    /**
     * Makes a copy of the names to be retrieved
     *
     * @since 0.0.1
     * @version 0.0.1
     *
     * @param names The player names to retrieve
     * @param rateLimiting Whether or not to rate limit requests to Mojang 
     */
    public UUIDFetcher(List<String> names, boolean rateLimiting) {
        this.names = ImmutableList.copyOf(names);
        this.rateLimiting = rateLimiting;
    }

    /**
     * Passes the names to the other constructor, and {@code true} for rate
     * limiting
     *
     * @since 0.0.1
     * @version 0.0.1
     *
     * @see UUIDFetcher#UUIDFetcher(List, boolean)
     * @param names The names to convert
     */
    public UUIDFetcher(List<String> names) {
        this(names, true);
    }

    /**
     * Makes a request to mojang's servers of a sublist of at most 100 player's
     * names.
     * <br><br> {@inheritDoc}
     *
     * @since 0.0.1
     * @version 0.1.0
     *
     * @return A {@link Map} of player names to their {@link UUID}s
     * @throws IOException If there's a problem sending or receiving the request
     * @throws ParseException If the request response cannot be read
     * @throws InterruptedException If the thread is interrupted while sleeping
     */
    @Override
    public Map<String, UUID> call() throws IOException, ParseException, InterruptedException {
        return this.callWithProgressOutput(false, null, null);
    }

    /**
     * Makes a request to mojang's servers of a sublist of at most 100 player's
     * names. Additionally can provide progress outputs
     *
     * @since 0.0.1
     * @version 0.1.0
     *
     * @param output Whether or not to print output
     * @param log The {@link Logger} to print to
     * @param doOutput A {@link Predicate} representing when to output a number
     * @return A {@link Map} of player names to their {@link UUID}s
     * @throws IOException If there's a problem sending or receiving the request
     * @throws ParseException If the request response cannot be read
     * @throws InterruptedException If the thread is interrupted while sleeping
     */
    public Map<String, UUID> callWithProgressOutput(boolean output, Logger log, Predicate<? super Integer> doOutput) throws IOException, ParseException, InterruptedException {
        Map<String, UUID> uuidMap = new HashMap<>();
        int totalNames = this.names.size();
        int completed = 0;
        int failed = 0;
        int requests = (int) Math.ceil(this.names.size() / UUIDFetcher.PROFILES_PER_REQUEST);
        for (int i = 0; i < requests; i++) {
            List<String> request = names.subList(i * 100, Math.min((i + 1) * 100, this.names.size()));
            String body = JSONArray.toJSONString(request);
            HttpURLConnection connection = UUIDFetcher.createConnection();
            UUIDFetcher.writeBody(connection, body);
            if (connection.getResponseCode() == 429 && this.rateLimiting) {
                String out = "[UUIDFetcher] Rate limit hit! Waiting 10 minutes until continuing conversion...";
                if (log != null) {
                    log.warning(out);
                } else {
                    Bukkit.getLogger().warning(out);
                }
                Thread.sleep(TimeUnit.MINUTES.toMillis(10));
                connection = UUIDFetcher.createConnection();
                UUIDFetcher.writeBody(connection, body);
            }
            JSONArray array = (JSONArray) this.jsonParser.parse(new InputStreamReader(connection.getInputStream()));
            completed += array.size();
            failed += request.size() - array.size();
            for (Object profile : array) {
                JSONObject jsonProfile = (JSONObject) profile;
                UUID uuid = UUIDFetcher.getUUID((String) jsonProfile.get("id"));
                uuidMap.put((String) jsonProfile.get("name"), uuid);
            }
            if (output) {
                int processed = completed + failed;
                if (doOutput.apply(processed) || processed == totalNames) {
                    if (log != null) {
                        log.info(String.format("[UUIDFetcher] Progress: %d/%d, %.2f%%, Failed names: %d",
                                processed, totalNames, ((double) processed / totalNames) * 100D, failed));
                    }
                }
            }
        }
        return uuidMap;
    }

    /**
     * Writes a JSON payload an {@link HttpURLConnection} object
     *
     * @since 0.0.1
     * @version 0.1.0
     *
     * @param connection The {@link HttpURLConnection} object to write to
     * @param body The JSON payload to write
     * @throws IOException If there is an error closing the stream
     */
    private static void writeBody(HttpURLConnection connection, String body) throws IOException {
        try (OutputStream stream = connection.getOutputStream()) {
            stream.write(body.getBytes());
            stream.flush();
        }
    }

    /**
     * Opens the connection to Mojang's profile API
     *
     * @since 0.0.1
     * @version 0.0.1
     *
     * @return The {@link HttpURLConnection} object to the API server
     * @throws IOException If there is a problem opening the stream, a malformed
     *                     URL, or if there is a ProtocolException
     */
    private static HttpURLConnection createConnection() throws IOException {
        URL url = new URL(UUIDFetcher.PROFILE_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }

    /**
     * Creates a connection object for requesting a single profile name
     *
     * @since 0.1.0
     * @version 0.1.0
     *
     * @param name The name to request
     * @return The {@link HttpURLConnection} to Mojang's server
     * @throws IOException If there is a problem opening the stream, a malformed
     *                     URL, or if there is a ProtocolException
     */
    private static HttpURLConnection createSingleProfileConnection(String name) throws IOException {
        URL url = new URL(String.format("https://api.mojang.com/users/profiles/minecraft/%s?at=0", name));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        return connection;
    }

    /**
     * Returns a {@link UUID} formatted from Mojang's server to include dashes
     *
     * @since 0.0.1
     * @version 0.0.1
     *
     * @param id The UUID in a "raw" format without dashes
     * @return The newly constructed {@link UUID} object
     */
    private static UUID getUUID(String id) {
        return UUID.fromString(id.substring(0, 8)
                + "-" + id.substring(8, 12)
                + "-" + id.substring(12, 16)
                + "-" + id.substring(16, 20)
                + "-" +id.substring(20, 32));
    }

    /**
     * Returns the {@link UUID} of a player's username. Note that this is a
     * blocking method
     *
     * @since 0.0.1
     * @version 0.0.1
     *
     * @param name The username of the player to fetch a {@link UUID} for
     * @return The {@link UUID} of the player name that is passed
     * @throws IOException If there's a problem sending or receiving the request
     * @throws ParseException If the request response cannot be read
     * @throws InterruptedException If the thread is interrupted while sleeping
     */
    public static UUID getUUIDOf(String name) throws IOException, ParseException, InterruptedException {
        return new UUIDFetcher(Collections.singletonList(name)).call().get(name);
    }

}