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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test d'intégration vérifiant que la priorité DURATION sélectionne
 * le trajet le plus court (même si pas le moins cher).
 */
class DurationPriorityIT {

    @Test
    void durationPrioritySelectsShortestTrip() {
        // Arrange: real services
        JsonTransportService transportService = new JsonTransportService("data/transports.json");
        JsonHotelService hotelService = new JsonHotelService("data/hotels.json");
        HaversineDistanceService distanceService = new HaversineDistanceService();
        TestGeocodingService geocodingService = new TestGeocodingService();
        JsonActivityService activityService = new JsonActivityService("data/activities.json", geocodingService, distanceService);

        PackageBuilder builder = new PackageBuilder(transportService, hotelService, activityService, distanceService, geocodingService);

        // Act: Request with DURATION priority
        List<Package> packages = builder.findPackages(
                "Paris", "Lyon", "2026-02-10", 3, 1000.0,
                null, "DURATION", 1, null,
                Collections.emptyList(), 50.0
        );

        // Assert: Should return at least one package
        assertFalse(packages.isEmpty(), "Expected at least one package with DURATION priority");
        
        Package p = packages.get(0);
        assertNotNull(p.getOutboundTrip(), "Outbound trip should exist");
        
        // Verify that the outbound trip is indeed the shortest available
        List<Transport> outboundTransports = p.getOutboundTrip().getTransports();
        assertFalse(outboundTransports.isEmpty(), "Outbound trip should have at least one transport");
        
        // Calculate total duration of the selected trip
        long totalOutboundDurationMinutes = 0;
        for (Transport t : outboundTransports) {
            LocalDateTime dep = t.getDepartureDateTime();
            LocalDateTime arr = t.getArrivalDateTime();
            totalOutboundDurationMinutes += Duration.between(dep, arr).toMinutes();
        }
        
        // With DURATION priority, we expect the fastest option to be selected
        assertTrue(totalOutboundDurationMinutes > 0, "Trip duration should be positive");
        List<Transport> allTransports = transportService.findTransports("Paris", "Lyon", LocalDateTime.of(2026, 2, 10, 0, 0), null);
        long shortestDuration = Long.MAX_VALUE;
        for (Transport t : allTransports) {
            long duration = Duration.between(t.getDepartureDateTime(), t.getArrivalDateTime()).toMinutes();
            if (duration < shortestDuration) {
                shortestDuration = duration;
            }
        }
        assertEquals(shortestDuration, totalOutboundDurationMinutes, "Selected trip duration should match the shortest available duration");
        System.out.println("Selected outbound trip duration: " + totalOutboundDurationMinutes + " minutes, expected shortest: " + shortestDuration + " minutes");

        // Verify that the return trip is indeed the shortest available
        List<Transport> returnTransports = p.getReturnTrip().getTransports();
        assertFalse(returnTransports.isEmpty(), "Return trip should have at least one transport");
        
        // Calculate total duration of the selected trip
        long totalReturnDurationMinutes = 0;
        for (Transport t : returnTransports) {
            LocalDateTime depBis = t.getDepartureDateTime();
            LocalDateTime arrBis = t.getArrivalDateTime();
            totalReturnDurationMinutes += Duration.between(depBis, arrBis).toMinutes();
        }
        
        // With DURATION priority, we expect the fastest option to be selected
        assertTrue(totalReturnDurationMinutes > 0, "Trip duration should be positive");
        List<Transport> allTransportsBis = transportService.findTransports("Lyon", "Paris", LocalDateTime.of(2026, 2, 13, 0, 0), null);
        long shortestDurationBis = Long.MAX_VALUE;
        for (Transport t : allTransportsBis) {
            long duration = Duration.between(t.getDepartureDateTime(), t.getArrivalDateTime()).toMinutes();
            if (duration < shortestDurationBis) {
                shortestDurationBis = duration;
            }
        }
        assertEquals(shortestDurationBis, totalReturnDurationMinutes, "Selected trip duration should match the shortest available duration");
        System.out.println("Selected return trip duration: " + totalReturnDurationMinutes + " minutes, expected shortest: " + shortestDurationBis + " minutes");
    }
}
