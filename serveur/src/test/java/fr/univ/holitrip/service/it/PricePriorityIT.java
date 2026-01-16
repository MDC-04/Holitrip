package fr.univ.holitrip.service.it;

import fr.univ.holitrip.model.Hotel;
import fr.univ.holitrip.model.Package;
import fr.univ.holitrip.model.Transport;
import fr.univ.holitrip.service.impl.JsonActivityService;
import fr.univ.holitrip.service.impl.JsonHotelService;
import fr.univ.holitrip.service.impl.JsonTransportService;
import fr.univ.holitrip.service.impl.HaversineDistanceService;
import fr.univ.holitrip.service.impl.PackageBuilder;
import fr.univ.holitrip.testhelpers.stubs.TestGeocodingService;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PricePriorityIT {

    @Test
    public void pricePrioritySelectsCheapestTrip() {
        // Arrange: real services
        JsonTransportService transportService = new JsonTransportService("data/transports.json");
        JsonHotelService hotelService = new JsonHotelService("data/hotels.json");
        HaversineDistanceService distanceService = new HaversineDistanceService();
        TestGeocodingService geocodingService = new TestGeocodingService();
        JsonActivityService activityService = new JsonActivityService("data/activities.json", geocodingService, distanceService);

        PackageBuilder builder = new PackageBuilder(transportService, hotelService, activityService, distanceService, geocodingService);

        // Act: Request with PRICE priority
        List<Package> packages = builder.findPackages(
                "Paris", "Lyon", "2026-02-10", 3, 1000.0,
                null, "PRICE", 1, "PRICE",
                Collections.emptyList(), 50.0
        );

        // Assert: Should return at least one package
        assertFalse(packages.isEmpty(), "Expected at least one package with PRICE priority");

        Package p = packages.get(0);
        assertNotNull(p.getOutboundTrip(), "Outbound trip should exist");

        // Verify that the outbound trip is indeed the cheapest available
        List<Transport> outboundTransports = p.getOutboundTrip().getTransports();
        assertFalse(outboundTransports.isEmpty(), "Outbound trip should have at least one transport");

        List<Transport> returnTransports = p.getReturnTrip().getTransports();
        assertFalse(returnTransports.isEmpty(), "Return trip should have at least one transport");

        //Compare price of selected outbound trip to all available trips
        double outboundTransportPrice = 0.0;
        for (Transport t : outboundTransports) {
            outboundTransportPrice += t.getPrice();
        }

        // With PRICE priority, we expect the cheapest option to be selected
        assertTrue(outboundTransportPrice > 0, "Trip price should be positive");
        List<Transport> allTransports = transportService.findTransports("Paris", "Lyon", LocalDateTime.of(2026, 2, 10, 0, 0), null);
        double cheapestPrice = Double.MAX_VALUE;
        for (Transport t : allTransports) {
            double price = t.getPrice();
            if (price < cheapestPrice) {
                cheapestPrice = price;
            }
        }
        assertEquals(cheapestPrice, outboundTransportPrice, "Selected trip price should match the cheapest available price");
        System.out.println("Selected outbound trip price: " + outboundTransportPrice + ", expected cheapest: " + cheapestPrice);

        //Compare price of selected return trip to all available trips
        double returnTransportPrice = 0.0;
        for (Transport t : returnTransports) {
            returnTransportPrice += t.getPrice();
        }

        // With PRICE priority, we expect the cheapest option to be selected
        assertTrue(returnTransportPrice > 0, "Trip price should be positive");
        List<Transport> allTransportsBis = transportService.findTransports("Lyon", "Paris", LocalDateTime.of(2026, 2, 13, 0, 0), null);
        double cheapestPriceBis = Double.MAX_VALUE;
        for (Transport t : allTransportsBis) {
            double price = t.getPrice();
            if (price < cheapestPriceBis) {
                cheapestPriceBis = price;
            }
        }
        assertEquals(cheapestPriceBis, returnTransportPrice, "Selected trip price should match the cheapest available price");
        System.out.println("Selected return trip price: " + returnTransportPrice + ", expected cheapest: " + cheapestPriceBis);

        //Compare price of selected hotel to all available trips
        double hotelPrice = 0.0;
        if (p.getHotel() != null) {
            hotelPrice = p.getHotel().getPricePerNight();
        }

        // With PRICE priority, we expect the cheapest option to be selected
        assertTrue(hotelPrice > 0, "Hotel price should be positive");
        List<Hotel> allHotels = hotelService.findHotels("Lyon", 1, 10000.0);
        double cheapestHotelPrice = Double.MAX_VALUE;
        for (Hotel h : allHotels) {
            double price = h.getPricePerNight();
            if (price < cheapestHotelPrice) {
                cheapestHotelPrice = price;
            }
        }
        assertEquals(cheapestHotelPrice, hotelPrice, "Selected hotel price should match the cheapest available price");
        System.out.println("Selected hotel price: " + hotelPrice + ", expected cheapest: " + cheapestHotelPrice);
    }
}
    

