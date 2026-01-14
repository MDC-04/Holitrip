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

public class IntegrationMultiLegTransportIT {
    
    @Test
    void testTwoLegTransportScenario() {
        // Arrange: real services
        JsonTransportService transportService = new JsonTransportService("data/transports.json");
        JsonHotelService hotelService = new JsonHotelService("data/hotels.json");
        HaversineDistanceService distanceService = new HaversineDistanceService();
        TestGeocodingService geocodingService = new TestGeocodingService();
        JsonActivityService activityService = new JsonActivityService("data/activities.json", geocodingService, distanceService);

        PackageBuilder builder = new PackageBuilder(transportService, hotelService, activityService, distanceService, geocodingService);

        // Act: Request a destination with no direct transport from departure city
        List<Package> packages = builder.findPackages(
                "Tours", "Nice", "2026-02-10", 3, 1000.0,
                null, "DURATION", 1, null,
                Collections.emptyList(), 50.0
        );

        //Assert: Should return a package with multi-leg transport
        assertFalse(packages.isEmpty(), "Expected at least one package");
        
        Package pkg = packages.get(0);
        assertNotNull(pkg.getOutboundTrip(), "Expected outbound trip");
        
        List<Transport> outboundTransports = pkg.getOutboundTrip().getTransports();
        assertNotNull(outboundTransports, "Expected outbound transports");
        assertEquals(2, outboundTransports.size(), "Expected 2 transports (multi-leg)");
        
        // Verify correspondence properties
        Transport firstLeg = outboundTransports.get(0);
        Transport secondLeg = outboundTransports.get(1);
        
        // Check city continuity: arrival city of first leg = departure city of second leg
        assertEquals(firstLeg.getArrivalCity(), secondLeg.getDepartureCity(), 
            "Arrival city of first leg must match departure city of second leg");
        
        // Check connection time (minimum 30 minutes)
        LocalDateTime firstArrival = firstLeg.getArrivalDateTime();
        LocalDateTime secondDeparture = secondLeg.getDepartureDateTime();
        assertNotNull(firstArrival, "First leg arrival time should not be null");
        assertNotNull(secondDeparture, "Second leg departure time should not be null");
        
        long connectionMinutes = Duration.between(firstArrival, secondDeparture).toMinutes();
        assertTrue(connectionMinutes >= 60, 
            "Connection time should be at least 60 minutes, but was: " + connectionMinutes + " minutes");
        
        // Check mode homogeneity
        assertEquals(firstLeg.getMode(), secondLeg.getMode(), 
            "Transport modes should be homogeneous (same mode for both legs)");
        
        System.out.println("Multi-leg correspondence verified:");
        System.out.println("  " + firstLeg.getDepartureCity() + " -> " + firstLeg.getArrivalCity() + 
            " (" + firstLeg.getMode() + ", " + firstLeg.getPrice() + "€)");
        System.out.println("  Connection time: " + connectionMinutes + " minutes at " + firstLeg.getArrivalCity());
        System.out.println("  " + secondLeg.getDepartureCity() + " -> " + secondLeg.getArrivalCity() + 
            " (" + secondLeg.getMode() + ", " + secondLeg.getPrice() + "€)");
    }
    
    @Test
    void testThreeLegTransportScenario() {
        // Arrange: real services
        JsonTransportService transportService = new JsonTransportService("data/transports.json");
        JsonHotelService hotelService = new JsonHotelService("data/hotels.json");
        HaversineDistanceService distanceService = new HaversineDistanceService();
        TestGeocodingService geocodingService = new TestGeocodingService();
        JsonActivityService activityService = new JsonActivityService("data/activities.json", geocodingService, distanceService);

        PackageBuilder builder = new PackageBuilder(transportService, hotelService, activityService, distanceService, geocodingService);

        // Act: Request Bordeaux -> Nice which requires 3 legs (Bordeaux -> Toulouse -> Marseille -> Nice)
        List<Package> packages = builder.findPackages(
                "Bordeaux", "Nice", "2026-02-15", 3, 2000.0,
                "TRAIN", "PRICE", 1, null,
                Collections.emptyList(), 50.0
        );

        // Assert: Should return a package with 3-leg transport
        assertFalse(packages.isEmpty(), "Expected at least one package");
        
        Package pkg = packages.get(0);
        assertNotNull(pkg.getOutboundTrip(), "Expected outbound trip");
        
        List<Transport> outboundTransports = pkg.getOutboundTrip().getTransports();
        assertNotNull(outboundTransports, "Expected outbound transports");
        assertEquals(3, outboundTransports.size(), "Expected 3 transports (3-leg journey)");
        
        // Verify correspondence properties
        Transport firstLeg = outboundTransports.get(0);
        Transport secondLeg = outboundTransports.get(1);
        Transport thirdLeg = outboundTransports.get(2);
        
        // Check city continuity
        assertEquals(firstLeg.getArrivalCity(), secondLeg.getDepartureCity(), 
            "First leg arrival must match second leg departure");
        assertEquals(secondLeg.getArrivalCity(), thirdLeg.getDepartureCity(), 
            "Second leg arrival must match third leg departure");
        
        // Check connection times (minimum 60 minutes each)
        LocalDateTime firstArrival = firstLeg.getArrivalDateTime();
        LocalDateTime secondDeparture = secondLeg.getDepartureDateTime();
        LocalDateTime secondArrival = secondLeg.getArrivalDateTime();
        LocalDateTime thirdDeparture = thirdLeg.getDepartureDateTime();
        
        long connection1 = Duration.between(firstArrival, secondDeparture).toMinutes();
        long connection2 = Duration.between(secondArrival, thirdDeparture).toMinutes();
        
        assertTrue(connection1 >= 60, 
            "First connection should be at least 60 minutes, but was: " + connection1 + " minutes");
        assertTrue(connection2 >= 60, 
            "Second connection should be at least 60 minutes, but was: " + connection2 + " minutes");
        
        // Check mode homogeneity (all TRAIN)
        assertEquals(firstLeg.getMode(), secondLeg.getMode(), "All modes should be identical");
        assertEquals(secondLeg.getMode(), thirdLeg.getMode(), "All modes should be identical");
        
        System.out.println("3-leg correspondence verified:");
        System.out.println("  " + firstLeg.getDepartureCity() + " -> " + firstLeg.getArrivalCity() + 
            " (" + firstLeg.getMode() + ", " + firstLeg.getPrice() + "€)");
        System.out.println("  Connection: " + connection1 + " minutes at " + firstLeg.getArrivalCity());
        System.out.println("  " + secondLeg.getDepartureCity() + " -> " + secondLeg.getArrivalCity() + 
            " (" + secondLeg.getMode() + ", " + secondLeg.getPrice() + "€)");
        System.out.println("  Connection: " + connection2 + " minutes at " + secondLeg.getArrivalCity());
        System.out.println("  " + thirdLeg.getDepartureCity() + " -> " + thirdLeg.getArrivalCity() + 
            " (" + thirdLeg.getMode() + ", " + thirdLeg.getPrice() + "€)");
    }
}
