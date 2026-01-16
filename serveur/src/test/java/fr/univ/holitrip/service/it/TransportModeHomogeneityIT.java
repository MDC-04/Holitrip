package fr.univ.holitrip.service.it;

import fr.univ.holitrip.model.Package;
import fr.univ.holitrip.model.Transport;
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

class TransportModeHomogeneityIT {

    @Test
    void planeModePackageCreation() {
        //Arrange: real services
        JsonTransportService transportService = new JsonTransportService("data/transports.json");
        JsonHotelService hotelService = new JsonHotelService("data/hotels.json");
        HaversineDistanceService distanceService = new HaversineDistanceService();
        TestGeocodingService geocodingService = new TestGeocodingService();
        JsonActivityService activityService = new JsonActivityService("data/activities.json", geocodingService, distanceService);

        PackageBuilder builder = new PackageBuilder(transportService, hotelService, activityService, distanceService, geocodingService);

        // Act: Request with PLANE and TRAIN mode
        List<Package> packages1 = builder.findPackages(
                "Bordeaux", "Paris", "2026-02-10", 3, 1000.0,
                "PLANE", "PRICE", 1, "PRICE",
                Collections.emptyList(), 50.0
        );

        List<Package> packages2 = builder.findPackages(
                "Paris", "Marseille", "2026-02-10", 3, 1000.0,
                "TRAIN", null, 1, null,
                Collections.emptyList(), 50.0
        );

        // Assert: Should return package with PLANE transport
        assertFalse(packages1.isEmpty(), "Expected at least one package for PLANE mode");
        assertFalse(packages2.isEmpty(), "Expected at least one package for TRAIN mode");

        for (Package p : packages1) {
                assertNotNull(p.getOutboundTrip(), "Outbound trip should exist");
                assertNotNull(p.getReturnTrip(), "Return trip should exist");
                assertTrue(p.getOutboundTrip().getTransports().stream()
                        .allMatch(t -> "PLANE".equals(t.getMode())), "All outbound transports should be PLANE");
                assertTrue(p.getReturnTrip().getTransports().stream()
                        .allMatch(t -> "PLANE".equals(t.getMode())), "All return transports should be PLANE");

                for (Transport t : p.getOutboundTrip().getTransports()) {
                        System.out.println("Outbound transport mode: " + t.getMode());
                }
                for (Transport t : p.getReturnTrip().getTransports()) {
                        System.out.println("Return transport mode: " + t.getMode());
                }
        }
        for (Package p : packages2) {
                assertNotNull(p.getOutboundTrip(), "Outbound trip should exist");
                assertNotNull(p.getReturnTrip(), "Return trip should exist");
                assertTrue(p.getOutboundTrip().getTransports().stream()
                        .allMatch(t -> "TRAIN".equals(t.getMode())), "All outbound transports should be TRAIN");
                assertTrue(p.getReturnTrip().getTransports().stream()
                        .allMatch(t -> "TRAIN".equals(t.getMode())), "All return transports should be TRAIN");

                for (Transport t : p.getOutboundTrip().getTransports()) {
                        System.out.println("Outbound transport mode: " + t.getMode());
                }
                for (Transport t : p.getReturnTrip().getTransports()) {
                        System.out.println("Return transport mode: " + t.getMode());
                }
        }   
    }
}
