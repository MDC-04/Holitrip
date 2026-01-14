package fr.univ.holitrip;

import fr.univ.holitrip.model.Package;
import fr.univ.holitrip.service.impl.*;

import java.util.Collections;
import java.util.List;

/**
 * Demo 5: Voyage avec CORRESPONDANCE (multi-leg transport)
 * Tours → Nice (pas de vol direct, donc Tours→Paris→Nice)
 * Budget modéré, 3★, CULTURE
 */
public class HolitripMain5 {
    public static void main(String[] args) {
        System.out.println("=== HolitripMain5: Voyage avec CORRESPONDANCE ===\n");

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
        String departureCity = "Tours";
        String destinationCity = "Nice";
        String departureDate = "2026-02-10";
        int tripDuration = 3;
        double maxBudget = 1000.0;
        String transportMode = "TRAIN"; // Force l'utilisation du train (correspondance Tours→Paris→Nice en train)
        String transportPriority = "PRICE";
        int minHotelRating = 3;
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
                    System.out.println("  Outbound transports: " + pkg.getOutboundTrip().getTransports());
                    if (pkg.getOutboundTrip().getTransports().size() > 1) {
                        System.out.println("  ✈️ CORRESPONDANCE DÉTECTÉE: " + pkg.getOutboundTrip().getTransports().size() + " trajets");
                    }
                }
                if (pkg.getReturnTrip() != null && pkg.getReturnTrip().getTransports() != null) {
                    System.out.println("  Return transports: " + pkg.getReturnTrip().getTransports());
                    if (pkg.getReturnTrip().getTransports().size() > 1) {
                        System.out.println("  ✈️ CORRESPONDANCE DÉTECTÉE: " + pkg.getReturnTrip().getTransports().size() + " trajets");
                    }
                }
                if (pkg.getHotel() != null) {
                    System.out.println("  Hotel: " + pkg.getHotel());
                }
                if (pkg.getActivities() != null && !pkg.getActivities().isEmpty()) {
                    System.out.println("  Activities: ");
                    pkg.getActivities().forEach(a -> System.out.println("    - " + a));
                }
                System.out.println("  Total price (nights=" + tripDuration + "): " + pkg.getTotalPrice(tripDuration));
                if (pkg.getErrors() != null && !pkg.getErrors().isEmpty()) {
                    System.out.println("  Errors: " + pkg.getErrors());
                }
            }
        }
    }
}
