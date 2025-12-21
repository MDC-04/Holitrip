package fr.univ.holitrip.service.impl;

import fr.univ.holitrip.model.Activity;
import fr.univ.holitrip.model.Coordinates;
import fr.univ.holitrip.service.ActivityService;
import fr.univ.holitrip.service.DistanceService;
import fr.univ.holitrip.service.GeocodingService;
import fr.univ.holitrip.exception.GeocodingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JSON-based implementation of ActivityService.
 * Reads activity data from a JSON file and filters based on criteria.
 * Uses GeocodingService and DistanceService for distance filtering.
 */
public class JsonActivityService implements ActivityService {
    private String jsonFilePath;
    private GeocodingService geocodingService;
    private DistanceService distanceService;

    /**
     * Constructor with dependency injection.
     * 
     * @param jsonFilePath path to the JSON file
     * @param geocodingService service to convert addresses to GPS coordinates
     * @param distanceService service to calculate distances
     */
    public JsonActivityService(String jsonFilePath, 
                               GeocodingService geocodingService,
                               DistanceService distanceService) {
        this.jsonFilePath = jsonFilePath;
        this.geocodingService = geocodingService;
        this.distanceService = distanceService;
    }

    @Override
    public List<Activity> findActivities(String city, List<String> categories, 
                                         LocalDate date, double maxPrice, 
                                         Coordinates hotelLocation, double maxDistance) {
        try {
            // 1. Read JSON file
            InputStream is = getClass().getClassLoader().getResourceAsStream(jsonFilePath);
            if (is == null) {
                return new ArrayList<>();
            }

            // 2. Parse JSON with Gson (custom adapter for LocalDate)
            Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, 
                    (JsonDeserializer<LocalDate>) (json, type, context) -> 
                        LocalDate.parse(json.getAsString()))
                .create();
            Type listType = new TypeToken<List<Activity>>(){}.getType();
            List<Activity> allActivities = gson.fromJson(new InputStreamReader(is), listType);

            // 3. Filter by basic criteria
            List<Activity> filtered = allActivities.stream()
                .filter(a -> a.getCity().equalsIgnoreCase(city))
                .filter(a -> categories.isEmpty() || categories.contains(a.getCategory()))
                .filter(a -> a.getPrice() <= maxPrice)
                .collect(Collectors.toList());

            // 4. Filter by distance if hotelLocation is provided
            if (hotelLocation != null && maxDistance > 0) {
                filtered = filtered.stream()
                    .filter(a -> isWithinDistance(a, hotelLocation, maxDistance))
                    .collect(Collectors.toList());
            }

            return filtered;

        } catch (Exception e) {
            // In case of error: return empty list
            return new ArrayList<>();
        }
    }

    /**
     * Checks if an activity is within the specified distance from the hotel.
     * 
     * @param activity the activity to check
     * @param hotelLocation the hotel GPS coordinates
     * @param maxDistance the maximum distance in km
     * @return true if within distance, false otherwise
     */
    private boolean isWithinDistance(Activity activity, Coordinates hotelLocation, double maxDistance) {
        try {
            // Geocode activity address
            String fullAddress = activity.getAddress() + ", " + activity.getCity();
            Coordinates activityLocation = geocodingService.geocode(fullAddress);
            
            // Calculate distance
            double distance = distanceService.calculateDistance(hotelLocation, activityLocation);
            
            return distance <= maxDistance;
            
        } catch (GeocodingException e) {
            // If geocoding fails, exclude the activity
            return false;
        }
    }
}
