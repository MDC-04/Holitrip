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
 * Demo CLI - Scénario 3: Voyage Marseille -> Paris avec petit budget
 * Priorité: économique avec activités culturelles et musicales gratuites
 */
public class HolitripMain3 {
    public static void main(String[] args) {
        System.out.println("=== Holitrip Demo 3: Marseille -> Paris (Budget Étudiant) ===");

        // Use real API geocoding service
        System.out.println("Using real ApiGeocodingService (geocode.maps.co API).");
        GeocodingService geocodingService = new ApiGeocodingService();

        JsonTransportService transportService = new JsonTransportService("data/transports.json");
        JsonHotelService hotelService = new JsonHotelService("data/hotels.json");
        HaversineDistanceService distanceService = new HaversineDistanceService();
        JsonActivityService activityService = new JsonActivityService("data/activities.json", geocodingService, distanceService);

        PackageBuilder builder = new PackageBuilder(transportService, hotelService, activityService, distanceService, geocodingService);

        // User scenario: voyage économique pour étudiant
        String departureCity = "Marseille";
        String destinationCity = "Paris";
        String departureDate = "2026-02-10";
        int tripDurationDays = 2;  // Court séjour
        double maxBudget = 250.0;  // Budget serré
        String transportMode = "TRAIN";  // Train uniquement
        String transportPriority = "PRICE";  // Le moins cher
        int minHotelRating = 1;  // Pas d'exigence sur les étoiles
        String hotelPriority = "PRICE";  // Hôtel le moins cher
        List<String> activityCategories = Arrays.asList("CULTURE", "MUSIC", "TOURISM");
        double maxDistanceKm = 25.0;  // Prêt à marcher

        System.out.println("Scenario: " + departureCity + " -> " + destinationCity + ", " + departureDate);
        System.out.println("Duration: " + tripDurationDays + " days, Budget: " + maxBudget + "€");
        System.out.println("Transport: " + transportMode + " (priority: " + transportPriority + ")");
        System.out.println("Hotel: min " + minHotelRating + " star (priority: " + hotelPriority + ")");
        System.out.println("Activities: " + activityCategories + " (max distance: " + maxDistanceKm + " km)");
        System.out.println("🎓 Mode étudiant: recherche d'activités gratuites prioritaires");

        List<Package> packages = builder.findPackages(departureCity, destinationCity, departureDate,
                tripDurationDays, maxBudget, transportMode, transportPriority, minHotelRating,
                hotelPriority, activityCategories, maxDistanceKm);

        if (packages == null || packages.isEmpty()) {
            System.out.println("\n❌ No packages found for the given criteria.");
            System.out.println("💡 Tips: Increase budget, reduce hotel rating requirement, or extend max distance.");
            return;
        }

        for (int i = 0; i < packages.size(); i++) {
            Package pkg = packages.get(i);
            System.out.println("\n========== Package #" + (i + 1) + " ==========");

            System.out.println("\n🏨 Hotel: " + (pkg.getHotel() != null ? pkg.getHotel().getName() + " (" + pkg.getHotel().getRating() + "⭐) - " + pkg.getHotel().getPricePerNight() + "€/night" : "none"));
            fr.univ.holitrip.model.Coordinates hotelCoords = null;
            try {
                if (pkg.getHotel() != null) {
                    String fa = pkg.getHotel().getAddress() + ", " + pkg.getHotel().getCity();
                    hotelCoords = geocodingService.geocode(fa);
                }
            } catch (Exception ignored) {}

            System.out.println("\n🚆 Outbound:");
            if (pkg.getOutboundTrip() != null && pkg.getOutboundTrip().getTransports() != null) {
                double outboundTotal = pkg.getOutboundTrip().getTotalPrice();
                pkg.getOutboundTrip().getTransports().forEach(t -> 
                    System.out.println("   " + t.getDepartureCity() + " → " + t.getArrivalCity() + " | " + 
                                     t.getMode() + " | " + t.getPrice() + "€"));
                System.out.println("   Total outbound: " + outboundTotal + "€");
            } else {
                System.out.println("   none");
            }

            System.out.println("\n🔙 Return:");
            if (pkg.getReturnTrip() != null && pkg.getReturnTrip().getTransports() != null) {
                double returnTotal = pkg.getReturnTrip().getTotalPrice();
                pkg.getReturnTrip().getTransports().forEach(t -> 
                    System.out.println("   " + t.getDepartureCity() + " → " + t.getArrivalCity() + " | " + 
                                     t.getMode() + " | " + t.getPrice() + "€"));
                System.out.println("   Total return: " + returnTotal + "€");
            } else {
                System.out.println("   none");
            }

            System.out.println("\n🎨 Activities:");
            if (pkg.getActivities() != null && !pkg.getActivities().isEmpty()) {
                int freeCount = 0;
                double activitiesTotal = 0.0;
                for (fr.univ.holitrip.model.Activity a : pkg.getActivities()) {
                    fr.univ.holitrip.model.Coordinates ac = null;
                    try {
                        if (a.getAddress() != null) ac = geocodingService.geocode(a.getAddress() + ", " + a.getCity());
                    } catch (Exception ignored) {}
                    double dist = -1.0;
                    try {
                        if (hotelCoords != null && ac != null) dist = distanceService.calculateDistance(hotelCoords, ac);
                    } catch (Exception ignored) {}
                    
                    String priceStr = a.getPrice() == 0.0 ? "GRATUIT ✨" : a.getPrice() + "€";
                    if (a.getPrice() == 0.0) freeCount++;
                    activitiesTotal += a.getPrice();
                    
                    System.out.println("   • " + a.getName() + " - " + priceStr);
                    System.out.println("     " + a.getCategory() + " | " + (dist >= 0 ? String.format("%.2f", dist) + " km" : "distance unknown"));
                }
                System.out.println("   Total activities: " + activitiesTotal + "€ (" + freeCount + " gratuite(s))");
            } else {
                System.out.println("   none within distance/budget");
            }

            double totalPrice = pkg.getTotalPrice(tripDurationDays);
            System.out.println("\n💰 TOTAL PACKAGE: " + String.format("%.2f", totalPrice) + "€ / " + maxBudget + "€");
            System.out.println("   Remaining budget: " + String.format("%.2f", maxBudget - totalPrice) + "€");
            
            if (pkg.getErrors() != null && !pkg.getErrors().isEmpty()) {
                System.out.println("\n⚠️ Errors: " + pkg.getErrors());
            }
        }

        System.out.println("\n✅ Demo 3 finished.");
    }
}
