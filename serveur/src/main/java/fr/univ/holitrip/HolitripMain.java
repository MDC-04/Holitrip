package fr.univ.holitrip;

import fr.univ.holitrip.model.Package;
import fr.univ.holitrip.model.Activity;
import fr.univ.holitrip.model.Transport;
import fr.univ.holitrip.service.impl.ApiGeocodingService;
import fr.univ.holitrip.service.impl.JsonActivityService;
import fr.univ.holitrip.service.impl.JsonHotelService;
import fr.univ.holitrip.service.impl.JsonTransportService;
import fr.univ.holitrip.service.impl.HaversineDistanceService;
import fr.univ.holitrip.service.impl.PackageBuilder;

import java.util.Collections;
import java.util.List;

/**
 * Application principale Holitrip - Démonstration de planification de voyage
 */
public class HolitripMain {
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║           HOLITRIP - Planificateur de Voyages              ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();

        // Initialisation des services
        System.out.println("📋 Initialisation des services...");
        ApiGeocodingService geocodingService = new ApiGeocodingService();
        JsonTransportService transportService = new JsonTransportService("data/transports.json");
        JsonHotelService hotelService = new JsonHotelService("data/hotels.json");
        HaversineDistanceService distanceService = new HaversineDistanceService();
        JsonActivityService activityService = new JsonActivityService("data/activities.json", geocodingService, distanceService);
        PackageBuilder packageBuilder = new PackageBuilder(transportService, hotelService, activityService, distanceService, geocodingService);
        System.out.println("✅ Services initialisés avec succès");
        System.out.println();

        // Paramètres de la requête utilisateur
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("📝 REQUÊTE DE L'UTILISATEUR");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        String departureCity = "Bordeaux";
        String destinationCity = "Paris";
        String departureDate = "2026-02-10";
        int tripDurationDays = 3;
        double maxBudget = 600.0;
        String transportMode = null; // Tous modes acceptés
        String transportPriority = "PRICE";
        int minHotelRating = 3;
        String hotelPriority = "PRICE";
        List<String> activityCategories = Collections.singletonList("CULTURE");
        double maxDistanceKm = 20.0;

        System.out.println("🏙️  Ville de départ      : " + departureCity);
        System.out.println("🏙️  Ville de destination : " + destinationCity);
        System.out.println("📅 Date de départ       : " + departureDate);
        System.out.println("📅 Durée du séjour      : " + tripDurationDays + " jours");
        System.out.println("💰 Budget maximum       : " + String.format("%.2f", maxBudget) + " €");
        System.out.println("🚆 Mode de transport    : " + (transportMode != null ? transportMode : "Tous modes"));
        System.out.println("⭐ Priorité transport   : " + transportPriority);
        System.out.println("🏨 Note minimale hôtel  : " + minHotelRating + " étoiles");
        System.out.println("⭐ Priorité hôtel       : " + hotelPriority);
        System.out.println("🎭 Catégories activités : " + String.join(", ", activityCategories));
        System.out.println("📍 Distance maximale    : " + String.format("%.1f", maxDistanceKm) + " km");
        System.out.println();

        // Recherche de forfaits
        System.out.println("🔍 Recherche de forfaits en cours...");
        System.out.println();

        List<Package> packages = packageBuilder.findPackages(
            departureCity, destinationCity, departureDate, tripDurationDays, maxBudget,
            transportMode, transportPriority, minHotelRating, hotelPriority, 
            activityCategories, maxDistanceKm
        );

        // Affichage des résultats
        if (packages == null || packages.isEmpty()) {
            System.out.println("❌ Aucun forfait trouvé pour ces critères.");
            System.out.println();
            System.out.println("💡 Suggestions :");
            System.out.println("   - Augmentez votre budget");
            System.out.println("   - Réduisez la note minimale de l'hôtel");
            System.out.println("   - Modifiez vos dates de voyage");
            return;
        }

        System.out.println("✅ " + packages.size() + " forfait(s) trouvé(s) !");
        System.out.println();

        for (int i = 0; i < packages.size(); i++) {
            Package pkg = packages.get(i);
            printPackage(pkg, i + 1, tripDurationDays);
        }

        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║            Merci d'avoir utilisé Holitrip !                ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }

    private static void printPackage(Package pkg, int packageNumber, int nights) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                      FORFAIT #" + packageNumber + "                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();

        // Validation
        if (!pkg.isValid()) {
            System.out.println("⚠️  FORFAIT INCOMPLET");
            if (!pkg.getErrors().isEmpty()) {
                System.out.println("    Erreurs : " + String.join(", ", pkg.getErrors()));
            }
            System.out.println();
        }

        // Transport aller
        System.out.println("🚆 TRANSPORT ALLER");
        System.out.println("───────────────────────────────────────────────────────────");
        if (pkg.getOutboundTrip() != null && pkg.getOutboundTrip().getTransports() != null) {
            for (Transport t : pkg.getOutboundTrip().getTransports()) {
                System.out.println("   " + formatTransport(t));
            }
            System.out.println("   Prix total : " + String.format("%.2f", pkg.getOutboundTrip().getTotalPrice()) + " €");
        } else {
            System.out.println("   ❌ Aucun transport aller");
        }
        System.out.println();

        // Transport retour
        System.out.println("🚆 TRANSPORT RETOUR");
        System.out.println("───────────────────────────────────────────────────────────");
        if (pkg.getReturnTrip() != null && pkg.getReturnTrip().getTransports() != null) {
            for (Transport t : pkg.getReturnTrip().getTransports()) {
                System.out.println("   " + formatTransport(t));
            }
            System.out.println("   Prix total : " + String.format("%.2f", pkg.getReturnTrip().getTotalPrice()) + " €");
        } else {
            System.out.println("   ❌ Aucun transport retour");
        }
        System.out.println();

        // Hôtel
        System.out.println("🏨 HÉBERGEMENT");
        System.out.println("───────────────────────────────────────────────────────────");
        if (pkg.getHotel() != null) {
            System.out.println("   " + pkg.getHotel().getName());
            System.out.println("   📍 " + pkg.getHotel().getAddress() + ", " + pkg.getHotel().getCity());
            System.out.println("   ⭐ " + "★".repeat(pkg.getHotel().getRating()) + " (" + pkg.getHotel().getRating() + " étoiles)");
            System.out.println("   💰 " + String.format("%.2f", pkg.getHotel().getPricePerNight()) + " € / nuit");
            System.out.println("   💰 Total (" + nights + " nuits) : " + String.format("%.2f", pkg.getHotel().getPricePerNight() * nights) + " €");
        } else {
            System.out.println("   ❌ Aucun hôtel");
        }
        System.out.println();

        // Activités
        System.out.println("🎭 ACTIVITÉS");
        System.out.println("───────────────────────────────────────────────────────────");
        if (pkg.getActivities() != null && !pkg.getActivities().isEmpty()) {
            double totalActivities = 0;
            for (Activity a : pkg.getActivities()) {
                System.out.println("   • " + a.getName());
                System.out.println("     📍 " + a.getAddress() + ", " + a.getCity());
                System.out.println("     🎯 Catégorie : " + a.getCategory());
                System.out.println("     📅 Date : " + a.getDate());
                System.out.println("     💰 Prix : " + String.format("%.2f", a.getPrice()) + " €");
                totalActivities += a.getPrice();
            }
            System.out.println("   ─────────────────────────────");
            System.out.println("   💰 Total activités : " + String.format("%.2f", totalActivities) + " €");
        } else {
            System.out.println("   ℹ️  Aucune activité incluse");
        }
        System.out.println();

        // Prix total
        double totalPrice = pkg.getTotalPrice(nights);
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("💰 PRIX TOTAL DU FORFAIT : " + String.format("%.2f", totalPrice) + " €");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println();

        // Erreurs éventuelles
        if (!pkg.getErrors().isEmpty()) {
            System.out.println("⚠️  AVERTISSEMENTS :");
            for (String error : pkg.getErrors()) {
                System.out.println("   • " + error);
            }
            System.out.println();
        }
    }

    private static String formatTransport(Transport t) {
        String icon = t.getMode().equals("TRAIN") ? "🚆" : "✈️";
        return icon + " " + t.getDepartureCity() + " → " + t.getArrivalCity() + 
               " | Départ: " + t.getDepartureDateTime().toLocalTime() + 
               " | Arrivée: " + t.getArrivalDateTime().toLocalTime() + 
               " | " + t.getMode() + " | " + String.format("%.2f", t.getPrice()) + " €";
    }
}
