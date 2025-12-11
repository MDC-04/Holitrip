package fr.univ.holitrip.service;

import java.time.LocalDate;
import java.util.List;

import fr.univ.holitrip.model.Activity;
import fr.univ.holitrip.model.Coordinates;

public interface ActivityService {
    /**
     * Finds available activities matching the specified criteria.
     *
     * @param city the city to search in
     * @param categories the list of desired activity categories (e.g., "sport", "music"), or empty for all categories
     * @param date the date of the activity
     * @param maxPrice the maximum price for the activity
     * @param hotelLocation the GPS coordinates of the hotel (to calculate distance)
     * @param maxDistance the maximum distance in km between the hotel and the activity (use Double.MAX_VALUE for no preference)
     * @return a list of activities matching the criteria
     */
    List<Activity> findActivities(String city, List<String> categories, LocalDate date, double maxPrice, 
                                    Coordinates hotelLocation, double maxDistance);
}
