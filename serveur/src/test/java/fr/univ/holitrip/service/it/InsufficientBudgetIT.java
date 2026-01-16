package fr.univ.holitrip.service.it;

import fr.univ.holitrip.model.Package;
import fr.univ.holitrip.service.impl.JsonActivityService;
import fr.univ.holitrip.service.impl.JsonHotelService;
import fr.univ.holitrip.service.impl.JsonTransportService;
import fr.univ.holitrip.service.impl.ApiGeocodingService;
import fr.univ.holitrip.service.impl.HaversineDistanceService;
import fr.univ.holitrip.service.impl.PackageBuilder;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test d'intégration vérifiant le comportement quand le budget est insuffisant.
 * Selon les exigences, le forfait doit être retourné en l'état avec les erreurs rencontrées.
 */
class InsufficientBudgetIT {

    @Test
    void insufficientBudgetReturnsEmptyOrPackageWithErrors() {
        // Arrange: real services
        JsonTransportService transportService = new JsonTransportService("data/transports.json");
        JsonHotelService hotelService = new JsonHotelService("data/hotels.json");
        HaversineDistanceService distanceService = new HaversineDistanceService();
        ApiGeocodingService geocodingService = new ApiGeocodingService();
        JsonActivityService activityService = new JsonActivityService("data/activities.json", geocodingService, distanceService);

        PackageBuilder builder = new PackageBuilder(transportService, hotelService, activityService, distanceService, geocodingService);

        // Act: Request with very low budget (10€ for a 3-day trip is impossible)
        List<Package> packages = builder.findPackages(
                "Paris", "Lyon", "2026-02-10", 3, 0.0,
                null, "PRICE", 1, "PRICE",
                Collections.emptyList(), 50.0
        );

        // Assert: Either no packages returned, or packages with error messages
        assertTrue(packages.isEmpty() || 
                   packages.stream().allMatch(p -> !p.getErrors().isEmpty()),
                   "With insufficient budget, should return empty list or packages with errors");
        
        // If packages are returned with errors, verify they document the budget issue
        if (!packages.isEmpty()) {
            for (Package p : packages) {
                assertFalse(p.getErrors().isEmpty(), "Package should contain error messages");
                System.out.println("Package errors: " + p.getErrors());
            }
        }
    }

    @Test
    void sufficientBudgetReturnsValidPackages() {
        // Arrange: real services
        JsonTransportService transportService = new JsonTransportService("data/transports.json");
        JsonHotelService hotelService = new JsonHotelService("data/hotels.json");
        HaversineDistanceService distanceService = new HaversineDistanceService();
        ApiGeocodingService geocodingService = new ApiGeocodingService();
        JsonActivityService activityService = new JsonActivityService("data/activities.json", geocodingService, distanceService);

        PackageBuilder builder = new PackageBuilder(transportService, hotelService, activityService, distanceService, geocodingService);

        // Act: Request with sufficient budget
        List<Package> packages = builder.findPackages(
                "Paris", "Lyon", "2026-02-10", 3, 800.0,
                null, "PRICE", 1, "PRICE",
                Collections.emptyList(), 50.0
        );

        // Assert: Should return at least one valid package
        assertFalse(packages.isEmpty(), "With sufficient budget, should return at least one package");
        
        Package p = packages.get(0);
        assertNotNull(p.getOutboundTrip(), "Package should have outbound trip");
        assertNotNull(p.getReturnTrip(), "Package should have return trip");
        assertNotNull(p.getHotel(), "Package should have hotel");
        
        // Verify total price is within budget
        double totalPrice = p.getTotalPrice(3);
        assertTrue(totalPrice <= 800.0, "Total price " + totalPrice + " should be within budget 800.0");
    }
}
