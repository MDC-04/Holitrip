package fr.univ.holitrip.service.it;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import fr.univ.holitrip.model.Activity;
import fr.univ.holitrip.model.Package;
import fr.univ.holitrip.service.impl.HaversineDistanceService;
import fr.univ.holitrip.service.impl.JsonActivityService;
import fr.univ.holitrip.service.impl.JsonHotelService;
import fr.univ.holitrip.service.impl.JsonTransportService;
import fr.univ.holitrip.service.impl.PackageBuilder;
import fr.univ.holitrip.testhelpers.stubs.TestGeocodingService;

public class UniqueActivityDateIT {
    
    @Test
    void testUniqueActivityDate() {
        //Arrange: real services
        JsonTransportService transportService = new JsonTransportService("data/transports.json");
        JsonHotelService hotelService = new JsonHotelService("data/hotels.json");
        HaversineDistanceService distanceService = new HaversineDistanceService();
        TestGeocodingService geocodingService = new TestGeocodingService();
        JsonActivityService activityService = new JsonActivityService("data/activities.json", geocodingService, distanceService);

        PackageBuilder builder = new PackageBuilder(transportService, hotelService, activityService, distanceService, geocodingService);

        //Act: Request packages
        List<Package> packages = builder.findPackages(
                "Paris", "Lyon", "2026-02-10", 3, 1000.0,
                null, null, 1, "PRICE",
                Collections.emptyList(), 500.0
        );

        //Assert: Each date has unique activitie 
        for (Package p : packages) {
            for (Activity a : p.getActivities()) {
                LocalDate activityDate = a.getDate();
                for (Activity b : p.getActivities()) {
                    if (a != b) {
                        LocalDate otherDate = b.getDate();
                        assertNotEquals(activityDate, otherDate, "A date should have unique activity within the package");
                    }
                }
            }
        }

        for (Package p : packages) {
            for (Activity a : p.getActivities()) {
                System.out.println("Activity date :" + a.getDate());
            }
        }
    }
}
