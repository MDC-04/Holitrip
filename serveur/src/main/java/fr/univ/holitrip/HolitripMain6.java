package fr.univ.holitrip;

import fr.univ.holitrip.model.Package;
import fr.univ.holitrip.service.impl.*;

import java.util.Collections;
import java.util.List;

/**
 * Demo 6: Voyage avec 3 CORRESPONDANCES
 * Bordeaux → Nice avec 3 legs (Bordeaux→Toulouse→Marseille→Nice)
 * Budget élevé, 1★+, CULTURE
 */
public class HolitripMain6 {
    public static void main(String[] args) {
        System.out.println("=== HolitripMain6: Voyage avec 3 CORRESPONDANCES ===\n");

        // Services
        System.out.println("Initializing services...");
        var transportService = new JsonTransportService("data/transports.json");
        var hotelService = new JsonHotelService("data/hotels.json");
        var distanceService = new HaversineDistanceService();
        
        System.out.println("Using real ApiGeocodingService...");
        var geocodingService = new ApiGeocodingService();
        
        var activityService = new JsonActivityService("data/activities.json", geocodingService, distanceService);
        var packageService = new PackageBuilder(transportService, hotelService, activityService, distanceService, geocodingService);

        // Critères utilisateur
        String departureCity = "Bordeaux";
        String destinationCity = "Nice";
        String departureDate = "2026-02-15";
        int tripDuration = 3;
        double maxBudget = 2000.0;
        String transportMode = "TRAIN"; // Force train pour avoir les 3 legs
        String transportPriority = "PRICE";
        int minHotelRating = 1;
        String hotelPriority = "PRICE";
        List<String> activityCategories = Collections.singletonList("CULTURE");
        double maxDistanceKm = 15.0;

        System.out.println("--- Demo: user criteria ---");
        System.out.println("Departure: " + departureCity + " -> Destination: " + destinationCity);
        System.out.println("DepartureDate: " + departureDate + ", Duration(days): " + tripDuration + ", Budget: " + maxBudget);
        System.out.println("TransportMode: " + transportMode + ", TransportPriority: " + transportPriority);
        System.out.println("Hotel min rating: " + minHotelRating + ", HotelPriority: " + hotelPriority);
        System.out.println("Activity categories: " + activityCategories + ", MaxDistanceKm: " + maxDistanceKm);

        // Recherche de forfaits
        System.out.println("\n=== DEBUG: Searching packages... ===");
        List<Package> packages = packageService.findPackages(
            departureCity, destinationCity, departureDate, tripDuration, maxBudget,
            transportMode, transportPriority, minHotelRating, hotelPriority,
            activityCategories, maxDistanceKm
        );
        System.out.println("=== DEBUG: Search completed ===\n");

        // Affichage
        System.out.println("--- Demo: result (" + packages.size() + " package(s)) ---");

        if (packages.isEmpty()) {
            System.out.println("No package found matching criteria.");
        } else {
            for (int i = 0; i < packages.size(); i++) {
                Package pkg = packages.get(i);
                System.out.println("Package #" + (i + 1) + ": " + pkg);

                if (pkg.getOutboundTrip() != null && pkg.getOutboundTrip().getTransports() != null) {
                    var transports = pkg.getOutboundTrip().getTransports();
                    System.out.println("  Outbound transports: " + transports);
                    if (transports.size() > 1) {
                        System.out.println("  ✈️ CORRESPONDANCE DÉTECTÉE: " + transports.size() + " trajets");
                    }
                }

                if (pkg.getReturnTrip() != null && pkg.getReturnTrip().getTransports() != null) {
                    var transports = pkg.getReturnTrip().getTransports();
                    System.out.println("  Return transports: " + transports);
                    if (transports.size() > 1) {
                        System.out.println("  ✈️ CORRESPONDANCE DÉTECTÉE: " + transports.size() + " trajets");
                    }
                }

                if (pkg.getHotel() != null) {
                    System.out.println("  Hotel: " + pkg.getHotel());
                }

                System.out.println("  Total price (nights=" + tripDuration + "): " + pkg.getTotalPrice(tripDuration));
            }
        }
    }
}
