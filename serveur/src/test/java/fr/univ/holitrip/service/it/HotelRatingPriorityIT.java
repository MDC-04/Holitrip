package fr.univ.holitrip.service.it;

import fr.univ.holitrip.model.Package;
import fr.univ.holitrip.service.impl.JsonActivityService;
import fr.univ.holitrip.service.impl.JsonHotelService;
import fr.univ.holitrip.service.impl.JsonTransportService;
import fr.univ.holitrip.service.impl.HaversineDistanceService;
import fr.univ.holitrip.service.impl.PackageBuilder;
import fr.univ.holitrip.testhelpers.stubs.TestGeocodingService;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HotelRatingPriorityIT {

    @Test
    void hotelRatingPriorityPackageCreation() {
        //Arrange: real services
        JsonTransportService transportService = new JsonTransportService("data/transports.json");
        JsonHotelService hotelService = new JsonHotelService("data/hotels.json");
        HaversineDistanceService distanceService = new HaversineDistanceService();
        TestGeocodingService geocodingService = new TestGeocodingService();
        JsonActivityService activityService = new JsonActivityService("data/activities.json", geocodingService, distanceService);

        PackageBuilder builder = new PackageBuilder(transportService, hotelService, activityService, distanceService, geocodingService);

        // Act: Request with PLANE mode
        List<Package> packages1 = builder.findPackages(
                "Bordeaux", "Paris", "2026-02-10", 3, 10000.0,
                null, null, 5, null,
                Collections.emptyList(), 50.0
        );

        List<Package> packages2 = builder.findPackages(
                "Paris", "Marseille", "2026-02-10", 3, 10000.0,
                null, null, 3, null,
                Collections.emptyList(), 50.0
        );

        // Assert: Should return package with rating >= 5 & >= 3 respectively
        assertFalse(packages1.isEmpty(), "Expected at least one package for hotel rating priority");
        assertFalse(packages2.isEmpty(), "Expected at least one package for hotel rating priority");

        for (Package p : packages1) {
            assertNotNull(p.getHotel(), "Hotel should exist");
            assertTrue(p.getHotel().getRating() >= 5, "Hotel rating should be at least 5");
            System.out.println("Hotel rating: " + p.getHotel().getRating() );
        }

        for (Package p : packages2) {
            assertNotNull(p.getHotel(), "Hotel should exist");
            assertTrue(p.getHotel().getRating() >= 3, "Hotel rating should be at least 3");
            System.out.println("Hotel rating: " + p.getHotel().getRating() );
        }
    }
    
}
