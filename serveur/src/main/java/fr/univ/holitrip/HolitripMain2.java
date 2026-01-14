package fr.univ.holitrip;

import fr.univ.holitrip.model.Package;
import fr.univ.holitrip.service.impl.ApiGeocodingService;
import fr.univ.holitrip.service.impl.JsonActivityService;
import fr.univ.holitrip.service.impl.JsonHotelService;
import fr.univ.holitrip.service.impl.JsonTransportService;
import fr.univ.holitrip.service.impl.HaversineDistanceService;
import fr.univ.holitrip.service.impl.PackageBuilder;
import fr.univ.holitrip.service.GeocodingService;

import java.util.Arrays;
import java.util.List;

/**
 * Demo CLI - Scénario 2: Voyage Paris -> Lyon en avion avec hôtel de luxe
 * Priorité: confort (5 étoiles) et activités sportives, budget élevé
 */
public class HolitripMain2 {
    public static void main(String[] args) {
        System.out.println("=== Holitrip Demo 2: Paris -> Lyon (Luxe & Sport) ===");

        // Use real API geocoding service
        System.out.println("Using real ApiGeocodingService (geocode.maps.co API).");
        GeocodingService geocodingService = new ApiGeocodingService();

        JsonTransportService transportService = new JsonTransportService("data/transports.json");
        JsonHotelService hotelService = new JsonHotelService("data/hotels.json");
        HaversineDistanceService distanceService = new HaversineDistanceService();
        JsonActivityService activityService = new JsonActivityService("data/activities.json", geocodingService, distanceService);

        PackageBuilder builder = new PackageBuilder(transportService, hotelService, activityService, distanceService, geocodingService);

        // User scenario: voyage de luxe avec activités sportives
        String departureCity = "Paris";
        String destinationCity = "Lyon";
        String departureDate = "2026-02-10";
        int tripDurationDays = 3;
        double maxBudget = 1500.0;  // Budget élevé
        String transportMode = "PLANE";  // Avion uniquement
        String transportPriority = "DURATION";  // Plus rapide
        int minHotelRating = 5;  // Hôtel 5 étoiles
        String hotelPriority = "RATING";  // Meilleur hôtel
        List<String> activityCategories = Arrays.asList("SPORT", "CULTURE");
        double maxDistanceKm = 15.0;

        System.out.println("Scenario: " + departureCity + " -> " + destinationCity + ", " + departureDate);
        System.out.println("Duration: " + tripDurationDays + " days, Budget: " + maxBudget + "€");
        System.out.println("Transport: " + transportMode + " (priority: " + transportPriority + ")");
        System.out.println("Hotel: min " + minHotelRating + " stars (priority: " + hotelPriority + ")");
        System.out.println("Activities: " + activityCategories + " (max distance: " + maxDistanceKm + " km)");

        List<Package> packages = builder.findPackages(departureCity, destinationCity, departureDate,
                tripDurationDays, maxBudget, transportMode, transportPriority, minHotelRating,
                hotelPriority, activityCategories, maxDistanceKm);

        if (packages == null || packages.isEmpty()) {
            System.out.println("\n❌ No packages found for the given criteria.");
            System.out.println("Try relaxing some constraints (budget, hotel rating, transport mode, etc.)");
            return;
        }

        for (int i = 0; i < packages.size(); i++) {
            Package pkg = packages.get(i);
            System.out.println("\n========== Package #" + (i + 1) + " ==========");

            System.out.println("\n🏨 Hotel: " + (pkg.getHotel() != null ? pkg.getHotel().toString() : "none"));
            fr.univ.holitrip.model.Coordinates hotelCoords = null;
            try {
                if (pkg.getHotel() != null) {
                    String fa = pkg.getHotel().getAddress() + ", " + pkg.getHotel().getCity();
                    hotelCoords = geocodingService.geocode(fa);
                    System.out.println("   Location: " + (hotelCoords != null ? hotelCoords : "unknown"));
                }
            } catch (Exception ignored) {}

            System.out.println("\n✈️ Outbound transports:");
            if (pkg.getOutboundTrip() != null && pkg.getOutboundTrip().getTransports() != null) {
                pkg.getOutboundTrip().getTransports().forEach(t -> System.out.println("   → " + t));
            } else {
                System.out.println("   none");
            }

            System.out.println("\n🔙 Return transports:");
            if (pkg.getReturnTrip() != null && pkg.getReturnTrip().getTransports() != null) {
                pkg.getReturnTrip().getTransports().forEach(t -> System.out.println("   → " + t));
            } else {
                System.out.println("   none");
            }

            System.out.println("\n🎯 Activities:");
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
                    System.out.println("   • " + a.getName() + " (" + a.getCategory() + ") - " + a.getPrice() + "€");
                    System.out.println("     Distance: " + (dist >= 0 ? String.format("%.2f", dist) + " km" : "unknown"));
                }
            } else {
                System.out.println("   none within distance/budget");
            }

            System.out.println("\n💰 Total price (nights=" + tripDurationDays + "): " + String.format("%.2f", pkg.getTotalPrice(tripDurationDays)) + "€");
            if (pkg.getErrors() != null && !pkg.getErrors().isEmpty()) {
                System.out.println("\n⚠️ Errors: " + pkg.getErrors());
            }
        }

        System.out.println("\n✅ Demo 2 finished.");
    }
}
