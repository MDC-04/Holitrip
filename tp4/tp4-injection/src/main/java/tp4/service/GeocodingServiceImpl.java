package tp4.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import tp4.WeatherException;
import tp4.model.Coordinates;

import java.io.IOException;

public class GeocodingServiceImpl implements GeocodingService {
    private static final String API_URL = "https://geocode.maps.co/search";
    private volatile long lastRequestTime = 0;

    @Override
    public Coordinates getCoordinates(String address) throws WeatherException {
        enforceRateLimit();

        OkHttpClient client = new OkHttpClient();
        try {
            String encoded = java.net.URLEncoder.encode(address, java.nio.charset.StandardCharsets.UTF_8);

            String apiKey = System.getenv("GEOCODING_API_KEY");

            String url = API_URL + "?q=" + encoded;
            if (apiKey != null && !apiKey.isBlank()) {
                url = url + "&api_key=" + java.net.URLEncoder.encode(apiKey, java.nio.charset.StandardCharsets.UTF_8);
            }

            Request request = new Request.Builder()
                .url(url)
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new WeatherException("Geocoding API error: " + response);
                }

                if (response.body() == null) {
                    throw new WeatherException("Geocoding API returned empty body for address: " + address);
                }

                String body = response.body().string();
                JSONArray results = new JSONArray(body);

                if (results.isEmpty()) {
                    throw new WeatherException("No coordinates found for address: " + address);
                }

                JSONObject firstResult = results.getJSONObject(0);
                double lat = firstResult.getDouble("lat");
                double lon = firstResult.getDouble("lon");

                return new Coordinates(lat, lon);
            }
        } catch (IOException e) {
            throw new WeatherException("Error contacting geocoding service", e);
        }
    }

    private void enforceRateLimit() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRequest = currentTime - lastRequestTime;

        if (timeSinceLastRequest < 1000) {
            try {
                Thread.sleep(1000 - timeSinceLastRequest);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        lastRequestTime = System.currentTimeMillis();
    }
}
