package fr.univ.holitrip.service;

import java.util.List;
import fr.univ.holitrip.model.Package;

public interface PackageService {
    /**
     * Finds all packages matching the user's search criteria.
     * 
     * This method:
     * - Searches for outbound and return trips (direct or with connections)
     * - Finds hotels in the destination city
     * - Searches for activities near the hotel
     * - Assembles complete packages respecting all criteria
     * - Returns packages with errors if some criteria cannot be satisfied
     * 
     * @param departureCity the departure city
     * @param destinationCity the destination city
     * @param departureDate the departure date (format: "2025-01-15")
     * @param tripDurationDays the duration of the stay in days
     * @param maxBudget the maximum budget for the complete package
     * @param transportMode the preferred transport mode ("TRAIN", "PLANE", or null for no preference)
     * @param transportPriority the transport priority ("PRICE" or "DURATION")
     * @param minHotelRating the minimum hotel rating (1-5 stars, or 0 for no preference)
     * @param hotelPriority the hotel priority ("PRICE" or "RATING")
     * @param activityCategories the desired activity categories (e.g., ["sport", "music"])
     * @param maxDistanceKm the maximum distance in km between hotel and activities
     * @return a list of packages matching the criteria (may contain packages with errors)
     */
    List<Package> findPackages(String departureCity, String destinationCity, String departureDate,
                               int tripDurationDays, double maxBudget, String transportMode,
                               String transportPriority, int minHotelRating, String hotelPriority,
                               List<String> activityCategories, double maxDistanceKm);
}
