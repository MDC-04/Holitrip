package fr.univ.holitrip;

import fr.univ.holitrip.model.Package;
import fr.univ.holitrip.service.impl.ApiGeocodingService;
import fr.univ.holitrip.service.impl.JsonActivityService;
import fr.univ.holitrip.service.impl.JsonHotelService;
import fr.univ.holitrip.service.impl.JsonTransportService;
import fr.univ.holitrip.service.impl.HaversineDistanceService;
import fr.univ.holitrip.service.impl.PackageBuilder;
import fr.univ.holitrip.service.GeocodingService;

import java.util.Collections;
import java.util.List;
/**
 * Demo CLI that simulates a full user scenario and prints detailed results.
 * It shows the sequence: search transports, hotels, activities, and assembles a package.
 */
public class HolitripMain {
    public static void main(String[] args) {
        System.out.println("--- Holitrip Full Demo ---");

        // Use real API geocoding service
        System.out.println("Using real ApiGeocodingService (geocode.maps.co API).");
        GeocodingService geocodingService = new ApiGeocodingService();

        JsonTransportService transportService = new JsonTransportService("data/transports.json");
        JsonHotelService hotelService = new JsonHotelService("data/hotels.json");
        HaversineDistanceService distanceService = new HaversineDistanceService();
        JsonActivityService activityService = new JsonActivityService("data/activities.json", geocodingService, distanceService);

        PackageBuilder builder = new PackageBuilder(transportService, hotelService, activityService, distanceService, geocodingService);

        // User scenario
        String departureCity = "Bordeaux";
        String destinationCity = "Paris";
        String departureDate = "2025-01-15";
        int tripDurationDays = 3;
        double maxBudget = 600.0;
        String transportMode = null; // any mode
        String transportPriority = "PRICE";
        int minHotelRating = 3;
        String hotelPriority = "PRICE";
        List<String> activityCategories = Collections.singletonList("CULTURE");
        double maxDistanceKm = 20.0;

        System.out.println("Scenario: " + departureCity + " -> " + destinationCity + ", " + departureDate + ", " + tripDurationDays + " days, budget=" + maxBudget);

        List<Package> packages = builder.findPackages(departureCity, destinationCity, departureDate,
                tripDurationDays, maxBudget, transportMode, transportPriority, minHotelRating,
                hotelPriority, activityCategories, maxDistanceKm);

        if (packages == null || packages.isEmpty()) {
            System.out.println("No packages found for the given criteria.");
            return;
        }

        for (int i = 0; i < packages.size(); i++) {
            Package pkg = packages.get(i);
            System.out.println("\n--- Package #" + (i + 1) + " ---");

            System.out.println("Hotel: " + (pkg.getHotel() != null ? pkg.getHotel().toString() : "none"));
            // Attempt to geocode hotel for display
            fr.univ.holitrip.model.Coordinates hotelCoords = null;
            try {
                if (pkg.getHotel() != null) {
                    String fa = pkg.getHotel().getAddress() + ", " + pkg.getHotel().getCity();
                    hotelCoords = geocodingService.geocode(fa);
                }
            } catch (Exception ignored) {}
            System.out.println("Hotel coords: " + (hotelCoords != null ? hotelCoords : "unknown"));

            System.out.println("Outbound transports:");
            if (pkg.getOutboundTrip() != null && pkg.getOutboundTrip().getTransports() != null) {
                pkg.getOutboundTrip().getTransports().forEach(t -> System.out.println("  - " + t));
            } else {
                System.out.println("  none");
            }

            System.out.println("Return transports:");
            if (pkg.getReturnTrip() != null && pkg.getReturnTrip().getTransports() != null) {
                pkg.getReturnTrip().getTransports().forEach(t -> System.out.println("  - " + t));
            } else {
                System.out.println("  none");
            }

            System.out.println("Activities:");
            if (pkg.getActivities() != null && !pkg.getActivities().isEmpty()) {
                for (fr.univ.holitrip.model.Activity a : pkg.getActivities()) {
                    fr.univ.holitrip.model.Coordinates ac = null;
                    try {
                        if (a.getAddress() != null) ac = geocodingService.geocode(a.getAddress() + ", " + a.getCity());
                    } catch (Exception ignored) {}
                    double dist = -1.0;
                    try {
                        if (hotelCoords != null && ac != null) dist = distanceService.calculateDistance(hotelCoords, ac);
                    } catch (Exception ignored) {}
                    System.out.println("  - " + a + " | coords=" + (ac != null ? ac : "unknown") + " | distance_km=" + (dist >= 0 ? String.format("%.2f", dist) : "unknown"));
                }
            } else {
                System.out.println("  none within distance/budget");
            }

            System.out.println("Total price (nights=" + tripDurationDays + "): " + pkg.getTotalPrice(tripDurationDays));
            if (pkg.getErrors() != null && !pkg.getErrors().isEmpty()) {
                System.out.println("Errors: " + pkg.getErrors());
            }
        }

        System.out.println("\nDemo finished.");
    }
}
