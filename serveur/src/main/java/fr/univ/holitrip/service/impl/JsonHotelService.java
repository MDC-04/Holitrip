package fr.univ.holitrip.service.impl;

import fr.univ.holitrip.model.Hotel;
import fr.univ.holitrip.service.HotelService;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JSON-based implementation of HotelService.
 * Reads hotel data from a JSON file and filters based on criteria.
 */
public class JsonHotelService implements HotelService {
    private String jsonFilePath;

    public JsonHotelService(String jsonFilePath) {
        this.jsonFilePath = jsonFilePath;
    }

    @Override
    public List<Hotel> findHotels(String city, int minRating, double maxPricePerNight) {
        try {
            // 1. Read JSON file
            InputStream is = getClass().getClassLoader().getResourceAsStream(jsonFilePath);
            if (is == null) {
                return new ArrayList<>();
            }

            // 2. Parse JSON with Gson
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Hotel>>(){}.getType();
            List<Hotel> allHotels = gson.fromJson(new InputStreamReader(is), listType);

            // 3. Filter by criteria
            return allHotels.stream()
                .filter(h -> h.getCity().equalsIgnoreCase(city))
                .filter(h -> h.getRating() >= minRating)
                .filter(h -> h.getPricePerNight() <= maxPricePerNight)
                .collect(Collectors.toList());

        } catch (Exception e) {
            // In case of error: return empty list
            return new ArrayList<>();
        }
    }
}
