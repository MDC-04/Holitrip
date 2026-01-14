package fr.univ.holitrip.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import fr.univ.holitrip.model.Coordinates;
import fr.univ.holitrip.service.GeocodingService;
import fr.univ.holitrip.exception.GeocodingException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Implementation of GeocodingService using the geocode.maps.co API.
 * Converts a postal address into GPS coordinates (latitude, longitude).
 */
public class ApiGeocodingService implements GeocodingService {
    private static final String API_URL = "https://geocode.maps.co/search?q=";
    private final HttpClient httpClient;
    private final Gson gson;

    /**
     * Default constructor: creates a standard HttpClient.
     */
    public ApiGeocodingService() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
    }

    /**
     * Constructor for HttpClient injection (unit tests).
     * @param httpClient HTTP client to use
     */
    public ApiGeocodingService(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.gson = new Gson();
    }

    /**
     * Converts a postal address into GPS coordinates via the geocode.maps.co API.
     * @param address the postal address to geocode
     * @return the corresponding GPS coordinates
     * @throws GeocodingException in case of network error, parsing error, or no result
     */
    @Override
    public Coordinates geocode(String address) throws GeocodingException {
        String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
        String url = API_URL + encodedAddress;
        // Read the API key from application.properties
        String apiKey = null;
        try (java.io.InputStream in = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                java.util.Properties props = new java.util.Properties();
                props.load(in);
                apiKey = props.getProperty("GEOCODING_API_KEY");
            }
        } catch (IOException ignore) {}
        // If missing, fallback to the environment variable
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("GEOCODING_API_KEY");
        }
        if (apiKey != null && !apiKey.isBlank()) {
            url = url + "&api_key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
        }

        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .timeout(Duration.ofSeconds(5))
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int sc = response.statusCode();
                if (sc == 200) {
                    JsonArray results = gson.fromJson(response.body(), JsonArray.class);
                    if (results.size() == 0) {
                        throw new GeocodingException("No result found for address: " + address);
                    }
                    JsonObject first = results.get(0).getAsJsonObject();
                    if (!first.has("lat") || !first.has("lon")) {
                        throw new GeocodingException("Invalid JSON response: missing lat/lon fields");
                    }
                    double lat = Double.parseDouble(first.get("lat").getAsString());
                    double lon = Double.parseDouble(first.get("lon").getAsString());
                    return new Coordinates(lat, lon);
                }
                // Retry on server errors or rate limit, otherwise fail fast
                if (sc >= 500 || sc == 429) {
                    if (attempt == maxAttempts) {
                        throw new GeocodingException("HTTP error: " + sc);
                    }
                    try {
                        Thread.sleep(300L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new GeocodingException("Interrupted during retry wait", ie);
                    }
                    continue;
                }
                throw new GeocodingException("HTTP error: " + sc);
            } catch (IOException e) {
                if (attempt == maxAttempts) {
                    throw new GeocodingException("Network error", e);
                }
                try {
                    Thread.sleep(200L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new GeocodingException("Interrupted during retry wait", ie);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new GeocodingException("Interrupted", e);
            } catch (JsonParseException | NumberFormatException e) {
                throw new GeocodingException("JSON parsing error", e);
            }
        }
        throw new GeocodingException("Failed to geocode address after retries: " + address);
    }
}
