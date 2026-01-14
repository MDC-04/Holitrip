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
 * Demo CLI - Scénario 4: Voyage Toulouse -> Nice, vacances en famille
 * Priorité: confort moyen, activités variées (famille, culture, sport), budget modéré
 */
public class HolitripMain4 {
    public static void main(String[] args) {
        System.out.println("=== Holitrip Demo 4: Toulouse -> Nice (Vacances Famille) ===");

        // Use real API geocoding service
        System.out.println("Using real ApiGeocodingService (geocode.maps.co API).");
        GeocodingService geocodingService = new ApiGeocodingService();

        JsonTransportService transportService = new JsonTransportService("data/transports.json");
        JsonHotelService hotelService = new JsonHotelService("data/hotels.json");
        HaversineDistanceService distanceService = new HaversineDistanceService();
        JsonActivityService activityService = new JsonActivityService("data/activities.json", geocodingService, distanceService);

        PackageBuilder builder = new PackageBuilder(transportService, hotelService, activityService, distanceService, geocodingService);

        // User scenario: voyage en famille avec enfants
        String departureCity = "Toulouse";
        String destinationCity = "Nice";
        String departureDate = "2026-02-10";
        int tripDurationDays = 5;  // Séjour d'une semaine
        double maxBudget = 1200.0;  // Budget familial
        String transportMode = null;  // Tout mode de transport accepté
        String transportPriority = "PRICE";  // Économiser sur le transport
        int minHotelRating = 3;  // Confort moyen
        String hotelPriority = "PRICE";  // Prix raisonnable
        List<String> activityCategories = Arrays.asList("FAMILY", "CULTURE", "SPORT", "TOURISM");
        double maxDistanceKm = 10.0;  // Proche de l'hôtel (pratique avec enfants)

        System.out.println("Scenario: " + departureCity + " -> " + destinationCity + ", " + departureDate);
        System.out.println("Duration: " + tripDurationDays + " days, Budget: " + maxBudget + "€");
        System.out.println("Transport: any mode (priority: " + transportPriority + ")");
        System.out.println("Hotel: min " + minHotelRating + " stars (priority: " + hotelPriority + ")");
        System.out.println("Activities: " + activityCategories + " (max distance: " + maxDistanceKm + " km)");
        System.out.println("👨‍👩‍👧‍👦 Mode famille: recherche activités adaptées à tous");

        List<Package> packages = builder.findPackages(departureCity, destinationCity, departureDate,
                tripDurationDays, maxBudget, transportMode, transportPriority, minHotelRating,
                hotelPriority, activityCategories, maxDistanceKm);

        if (packages == null || packages.isEmpty()) {
            System.out.println("\n❌ No packages found for the given criteria.");
            System.out.println("💡 Suggestions:");
            System.out.println("   - Increase the budget (currently " + maxBudget + "€)");
            System.out.println("   - Reduce trip duration (currently " + tripDurationDays + " days)");
            System.out.println("   - Lower hotel rating requirement (currently " + minHotelRating + " stars)");
            System.out.println("   - Increase max distance for activities (currently " + maxDistanceKm + " km)");
            return;
        }

        for (int i = 0; i < packages.size(); i++) {
            Package pkg = packages.get(i);
            System.out.println("\n" + "=".repeat(60));
            System.out.println("PACKAGE #" + (i + 1));
            System.out.println("=".repeat(60));

            // Hotel details
            System.out.println("\n🏨 HÉBERGEMENT:");
            if (pkg.getHotel() != null) {
                System.out.println("   Nom: " + pkg.getHotel().getName());
                System.out.println("   Adresse: " + pkg.getHotel().getAddress() + ", " + pkg.getHotel().getCity());
                System.out.println("   Catégorie: " + pkg.getHotel().getRating() + " ⭐");
                System.out.println("   Prix: " + pkg.getHotel().getPricePerNight() + "€/nuit × " + tripDurationDays + " nuits = " + 
                                 (pkg.getHotel().getPricePerNight() * tripDurationDays) + "€");
            }
            
            fr.univ.holitrip.model.Coordinates hotelCoords = null;
            try {
                if (pkg.getHotel() != null) {
                    String fa = pkg.getHotel().getAddress() + ", " + pkg.getHotel().getCity();
                    hotelCoords = geocodingService.geocode(fa);
                }
            } catch (Exception ignored) {}

            // Transport details
            System.out.println("\n✈️ TRANSPORTS:");
            System.out.println("   Aller:");
            if (pkg.getOutboundTrip() != null && pkg.getOutboundTrip().getTransports() != null) {
                for (fr.univ.holitrip.model.Transport t : pkg.getOutboundTrip().getTransports()) {
                    System.out.println("      • " + t.getDepartureCity() + " → " + t.getArrivalCity());
                    System.out.println("        " + t.getDepartureDateTime() + " - " + t.getArrivalDateTime());
                    System.out.println("        Mode: " + t.getMode() + " | Prix: " + t.getPrice() + "€");
                }
                System.out.println("      Total aller: " + pkg.getOutboundTrip().getTotalPrice() + "€");
            }
            
            System.out.println("   Retour:");
            if (pkg.getReturnTrip() != null && pkg.getReturnTrip().getTransports() != null) {
                for (fr.univ.holitrip.model.Transport t : pkg.getReturnTrip().getTransports()) {
                    System.out.println("      • " + t.getDepartureCity() + " → " + t.getArrivalCity());
                    System.out.println("        " + t.getDepartureDateTime() + " - " + t.getArrivalDateTime());
                    System.out.println("        Mode: " + t.getMode() + " | Prix: " + t.getPrice() + "€");
                }
                System.out.println("      Total retour: " + pkg.getReturnTrip().getTotalPrice() + "€");
            }

            // Activities by category
            System.out.println("\n🎯 ACTIVITÉS:");
            if (pkg.getActivities() != null && !pkg.getActivities().isEmpty()) {
                java.util.Map<String, java.util.List<fr.univ.holitrip.model.Activity>> byCategory = 
                    pkg.getActivities().stream()
                        .collect(java.util.stream.Collectors.groupingBy(fr.univ.holitrip.model.Activity::getCategory));
                
                double activitiesTotal = 0.0;
                for (String category : byCategory.keySet()) {
                    System.out.println("   " + category + ":");
                    for (fr.univ.holitrip.model.Activity a : byCategory.get(category)) {
                        fr.univ.holitrip.model.Coordinates ac = null;
                        try {
                            if (a.getAddress() != null) ac = geocodingService.geocode(a.getAddress() + ", " + a.getCity());
                        } catch (Exception ignored) {}
                        double dist = -1.0;
                        try {
                            if (hotelCoords != null && ac != null) dist = distanceService.calculateDistance(hotelCoords, ac);
                        } catch (Exception ignored) {}
                        
                        System.out.println("      • " + a.getName() + " - " + a.getPrice() + "€");
                        System.out.println("        Date: " + a.getDate() + " | Distance: " + 
                                         (dist >= 0 ? String.format("%.2f", dist) + " km" : "unknown"));
                        activitiesTotal += a.getPrice();
                    }
                }
                System.out.println("   Total activités: " + activitiesTotal + "€");
            } else {
                System.out.println("   Aucune activité trouvée dans les contraintes");
            }

            // Budget summary
            double totalPrice = pkg.getTotalPrice(tripDurationDays);
            System.out.println("\n💰 RÉCAPITULATIF FINANCIER:");
            System.out.println("   Prix total du package: " + String.format("%.2f", totalPrice) + "€");
            System.out.println("   Budget maximal: " + maxBudget + "€");
            System.out.println("   Économies: " + String.format("%.2f", maxBudget - totalPrice) + "€");
            System.out.println("   Utilisation du budget: " + String.format("%.1f", (totalPrice / maxBudget) * 100) + "%");
            
            if (pkg.getErrors() != null && !pkg.getErrors().isEmpty()) {
                System.out.println("\n⚠️ ERREURS DÉTECTÉES:");
                pkg.getErrors().forEach(err -> System.out.println("   - " + err));
            }
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ Demo 4 terminée - Bon voyage en famille!");
        System.out.println("=".repeat(60));
    }
}
