package fr.univ.holitrip.service.it;

import fr.univ.holitrip.model.Activity;
import fr.univ.holitrip.model.Coordinates;
import fr.univ.holitrip.model.Hotel;
import fr.univ.holitrip.model.Package;
import fr.univ.holitrip.service.impl.ApiGeocodingService;
import fr.univ.holitrip.service.impl.JsonActivityService;
import fr.univ.holitrip.service.impl.JsonHotelService;
import fr.univ.holitrip.service.impl.JsonTransportService;
import fr.univ.holitrip.service.impl.HaversineDistanceService;
import fr.univ.holitrip.service.impl.PackageBuilder;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test d'intégration vérifiant que les activités respectent la distance maximale
 * par rapport à l'hôtel en utilisant la vraie API de geocoding.
 */
class ActivityDistanceFilterIT {

    @Test
    void activitiesRespectMaxDistanceFilter() {
        // Arrange: real services (including real geocoding API)
        JsonTransportService transportService = new JsonTransportService("data/transports.json");
        JsonHotelService hotelService = new JsonHotelService("data/hotels.json");
        HaversineDistanceService distanceService = new HaversineDistanceService();
        ApiGeocodingService geocodingService = new ApiGeocodingService();
        JsonActivityService activityService = new JsonActivityService("data/activities.json", geocodingService, distanceService);

        PackageBuilder builder = new PackageBuilder(transportService, hotelService, activityService, distanceService, geocodingService);

        // Act: Request with strict distance constraint (5km max)
        List<Package> packages = builder.findPackages(
                "Paris", "Lyon", "2026-02-10", 3, 800.0,
                null, "PRICE", 3, "PRICE",
                Arrays.asList("CULTURE", "SPORT"), 5.0
        );

        // Assert: Should return packages
        assertFalse(packages.isEmpty(), "Expected at least one package with distance filter");
        
        Package p = packages.get(0);
        Hotel hotel = p.getHotel();
        List<Activity> activities = p.getActivities();
        
        assertNotNull(activities, "Activities list should not be null");
        assertNotNull(hotel, "Hotel should not be null");
        
        // Get hotel coordinates using real geocoding API
        Coordinates hotelCoords = null;
        try {
            String hotelAddress = hotel.getAddress() + ", " + hotel.getCity();
            hotelCoords = geocodingService.geocode(hotelAddress);
            assertNotNull(hotelCoords, "Hotel coordinates should be found");
            System.out.println("Hotel: " + hotel.getName() + " at " + hotelAddress);
            System.out.println("Hotel coords: " + hotelCoords.getLatitude() + ", " + hotelCoords.getLongitude());
        } catch (Exception e) {
            fail("Failed to geocode hotel address: " + e.getMessage());
        }
        
        // Verify each activity is within the max distance
        for (Activity activity : activities) {
            // Verify category matches
            assertTrue(
                Arrays.asList("CULTURE", "SPORT").contains(activity.getCategory()),
                "Activity category should match requested categories: " + activity.getCategory()
            );
            
            // Calculate real distance
            try {
                String activityAddress = activity.getAddress() + ", " + activity.getCity();
                Coordinates activityCoords = geocodingService.geocode(activityAddress);
                assertNotNull(activityCoords, "Activity coordinates should be found for: " + activity.getName());
                
                double distance = distanceService.calculateDistance(hotelCoords, activityCoords);
                
                assertTrue(distance <= 5.0, 
                    "Activity '" + activity.getName() + "' at " + distance + "km should be within 5km of hotel");
                
                System.out.println("  - " + activity.getName() + " at " + distance + "km (✓ within 5km)");
            } catch (Exception e) {
                fail("Failed to calculate distance for activity '" + activity.getName() + "': " + e.getMessage());
            }
        }
        
        System.out.println("✓ All " + activities.size() + " activities are within 5km of hotel");
    }

    @Test
    void relaxedDistanceIncludesMoreActivities() {
        // Arrange: real services (including real geocoding API)
        JsonTransportService transportService = new JsonTransportService("data/transports.json");
        JsonHotelService hotelService = new JsonHotelService("data/hotels.json");
        HaversineDistanceService distanceService = new HaversineDistanceService();
        ApiGeocodingService geocodingService = new ApiGeocodingService();
        JsonActivityService activityService = new JsonActivityService("data/activities.json", geocodingService, distanceService);

        PackageBuilder builder = new PackageBuilder(transportService, hotelService, activityService, distanceService, geocodingService);

        // Act: First request with strict distance (5km)
        List<Package> strictPackages = builder.findPackages(
                "Paris", "Lyon", "2026-02-10", 3, 800.0,
                null, "PRICE", 3, "PRICE",
                Arrays.asList("CULTURE"), 5.0
        );

        // Act: Second request with relaxed distance (50km)
        List<Package> relaxedPackages = builder.findPackages(
                "Paris", "Lyon", "2026-02-10", 3, 800.0,
                null, "PRICE", 3, "PRICE",
                Arrays.asList("CULTURE"), 50.0
        );

        // Assert: Relaxed distance should include at least as many activities as strict
        if (!strictPackages.isEmpty() && !relaxedPackages.isEmpty()) {
            int strictActivityCount = strictPackages.get(0).getActivities().size();
            int relaxedActivityCount = relaxedPackages.get(0).getActivities().size();
            
            assertTrue(
                relaxedActivityCount >= strictActivityCount,
                "Relaxed distance (50km) should include at least as many activities as strict (5km). " +
                "Strict: " + strictActivityCount + ", Relaxed: " + relaxedActivityCount
            );
            
            System.out.println("Strict (5km): " + strictActivityCount + " activities");
            System.out.println("Relaxed (50km): " + relaxedActivityCount + " activities");
            
            // Verify distances for relaxed package
            Package relaxedPackage = relaxedPackages.get(0);
            Hotel hotel = relaxedPackage.getHotel();
            
            try {
                String hotelAddress = hotel.getAddress() + ", " + hotel.getCity();
                Coordinates hotelCoords = geocodingService.geocode(hotelAddress);
                
                System.out.println("\nVerifying relaxed package activities:");
                for (Activity activity : relaxedPackage.getActivities()) {
                    String activityAddress = activity.getAddress() + ", " + activity.getCity();
                    Coordinates activityCoords = geocodingService.geocode(activityAddress);
                    double distance = distanceService.calculateDistance(hotelCoords, activityCoords);
                    
                    assertTrue(distance <= 50.0, 
                        "Activity '" + activity.getName() + "' at " + distance + "km should be within 50km");
                    
                    System.out.println("  - " + activity.getName() + " at " + distance + "km");
                }
            } catch (Exception e) {
                System.err.println("Warning: Could not verify distances: " + e.getMessage());
            }
        }
    }
}
