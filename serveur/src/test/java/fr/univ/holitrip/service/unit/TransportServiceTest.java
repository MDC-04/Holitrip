package fr.univ.holitrip.service.unit;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.univ.holitrip.model.Transport;
import fr.univ.holitrip.service.impl.JsonTransportService;

public class TransportServiceTest {
    private fr.univ.holitrip.service.TransportService transportService;

    @BeforeEach
    void setUp() {
        transportService = new JsonTransportService("data/transports.json");
    }

    @Test
    void testFindTransports_BordeauxParis() {
        //ARRANGE
        String departureCity = "Bordeaux"; 
        String arrivalCity = "Paris";
        LocalDateTime departureDate = LocalDateTime.of(2026, 2, 10, 9, 0);
        String mode = null;
        
        //ACT
        List <Transport> transports = transportService.findTransports(departureCity, arrivalCity, departureDate, mode);

        //ASSERT
        assertNotNull(transports);
        assertFalse(transports.isEmpty());
        for (Transport transport : transports) {
            assertEquals(departureCity, transport.getDepartureCity());
            assertEquals(arrivalCity, transport.getArrivalCity());
            //assertEquals(departureDate, transport.getDepartureDateTime());
            assertTrue(transport.getMode().equalsIgnoreCase("TRAIN") || transport.getMode().equalsIgnoreCase("PLANE"));
        }

        System.out.println("=====SEPARATOR=====");
    }

    @Test
    void testFindTransports_BordeauxParis_TrainOnly() {
        //ARRANGE
        String departureCity = "Bordeaux";
        String arrivalCity = "Paris";
        LocalDateTime departureDate = LocalDateTime.of(2026, 2, 10, 9, 0);
        String mode = "TRAIN";
        
        //ACT
        List<Transport> transports = transportService.findTransports(departureCity, arrivalCity, departureDate, mode);
        
        //ASSERT
        assertNotNull(transports);
        assertFalse(transports.isEmpty());
        for (Transport transport : transports) {
            assertEquals(departureCity, transport.getDepartureCity());
            assertEquals(arrivalCity, transport.getArrivalCity());
            //assertEquals(departureDate, transport.getDepartureDateTime());
            assertEquals(mode, transport.getMode());
        }

        System.out.println("=====SEPARATOR=====");
    }

    @Test
    void testFindTransports_BordeauxParis_PlaneOnly() {
        //ARRANGE
        String departureCity = "Bordeaux";
        String arrivalCity = "Paris";
        LocalDateTime departureDate = LocalDateTime.of(2026, 2, 10, 9, 0);
        String mode = "PLANE";
        
        //ACT
        List<Transport> transports = transportService.findTransports(departureCity, arrivalCity, departureDate, mode);
        
        //ASSERT
        assertNotNull(transports);
        assertFalse(transports.isEmpty());
        for (Transport transport : transports) {
            assertEquals(departureCity, transport.getDepartureCity());
            assertEquals(arrivalCity, transport.getArrivalCity());
            //assertEquals(departureDate, transport.getDepartureDateTime());
            assertEquals(mode, transport.getMode());
        }

        System.out.println("=====SEPARATOR=====");
    }

    @Test
    void testFindTransports_InvalidCity_ShouldReturnEmpty() {
        //ARRANGE
        String departureCity = "Copenhagen";
        String arrivalCity = "Paris";
        LocalDateTime departureDate = LocalDateTime.of(2026, 2, 10, 9, 0);
        String mode = null;
        
        //ACT
        List<Transport> transports = transportService.findTransports(departureCity, arrivalCity, departureDate, mode);
        
        //ASSERT
        assertNotNull(transports);
        assertTrue(transports.isEmpty());

        System.out.println("No results found for " + departureCity);

        System.out.println("=====SEPARATOR=====");
    }

    @Test 
    void testFindTransports_NoAvailableDate_ShouldReturnEmpty() {
        //ARRANGE
        String departureCity = "Bordeaux";
        String arrivalCity = "Paris";
        LocalDateTime departureDate = LocalDateTime.of(2030, 1, 1, 0, 0); 
        String mode = null;
        
        //ACT
        List<Transport> transports = transportService.findTransports(departureCity, arrivalCity, departureDate, mode);

        //ASSERT
        assertNotNull(transports);
        assertTrue(transports.isEmpty());

        System.out.println("=====SEPARATOR=====");
    }

    @Test
    void testFindTransports_CaseInsensitive_ShouldWork() {
        //ARRANGE
        String departureCity = "bordeaux"; // Minuscules
        String arrivalCity = "PARIS";      // Majuscules
        LocalDateTime departureDate = LocalDateTime.of(2026, 2, 10, 9, 0);
        String mode = "train";             // Minuscules
        
        //ACT
        List<Transport> transports = transportService.findTransports(departureCity, arrivalCity, departureDate, mode);
        
        //ASSERT
        assertNotNull(transports);
        assertFalse(transports.isEmpty());

        for (Transport transport : transports) {
            assertEquals("Bordeaux", transport.getDepartureCity());
            assertEquals("Paris", transport.getArrivalCity());
            assertEquals("TRAIN", transport.getMode().toUpperCase());
        }

        System.out.println("=====SEPARATOR=====");
    }

    @Test
    void testFindTransports_MultiLeg_BordeauxToNice() {
        // Test multi-leg journey: Bordeaux -> Paris -> Nice
        // Should find connections through Paris with sufficient connection time
        LocalDateTime departureDate = LocalDateTime.of(2026, 2, 10, 8, 0);
        
        List<Transport> transports = transportService.findTransports("Bordeaux", "Nice", departureDate, null);
        
        assertNotNull(transports);
        // If multi-leg journeys exist, they should have valid connection times (>= 60 minutes)
        // This tests the BFS algorithm in JsonTransportService
        assertFalse(transports.isEmpty());
        assertTrue(transports.size() >= 2, "Expected multi-leg journey with at least one connection");
        
        for (Transport t : transports) {
            System.out.println(t.getDepartureCity() + " -> " + t.getArrivalCity() + " at " + 
            t.getDepartureDateTime() + "and arrives at " + t.getArrivalDateTime());
        }
        System.out.println("Minimal gap between connections is "+
            (transports.size() > 1 ? 
            Duration.between(
                transports.get(0).getArrivalDateTime(), 
                transports.get(1).getDepartureDateTime()
            ).toMinutes() : "N/A") + " minutes");

        System.out.println("=====SEPARATOR=====");
    }

    @Test
    void testFindTransports_MultiLeg_WithModePreference() {
        // Test multi-leg with mode preference TRAIN
        // All legs should be of the same mode when mode is specified
        LocalDateTime departureDate = LocalDateTime.of(2026, 2, 10, 8, 0);
        
        List<Transport> transports = transportService.findTransports("Tours", "Nice", departureDate, "TRAIN");
        
        assertNotNull(transports);
        // When mode is specified, all transports should match that mode
        assertFalse(transports.isEmpty());
        assertTrue(transports.size() >= 2, "Expected multi-leg journey with at least one connection");

        for (Transport t : transports) {
            assertTrue(t.getMode().equalsIgnoreCase("TRAIN"), 
                "When mode TRAIN is requested, all returned transports should be TRAIN");
            System.out.println(t.getDepartureCity() + " -> " + t.getArrivalCity() + " at " + 
            t.getDepartureDateTime() + " and arrives at " + t.getArrivalDateTime());
        }

        System.out.println("Minimal gap between connections is "+
            (transports.size() > 1 ? 
            Duration.between(
                transports.get(0).getArrivalDateTime(), 
                transports.get(1).getDepartureDateTime()
            ).toMinutes() : "N/A") + " minutes");

        System.out.println("=====SEPARATOR=====");
    }

    @Test
    void testFindTransports_ConnectionTimeValidation() {
        // Test that multi-leg journeys respect minimum connection time (60 minutes)
        // This is a regression test for the BFS algorithm
        LocalDateTime departureDate = LocalDateTime.of(2026, 2, 10, 8, 0);
        
        List<Transport> transports = transportService.findTransports("Lille", "Eindhoven", 
        departureDate, null);
        
        assertNotNull(transports);
        // The BFS algorithm should only return journeys with >= 60 min connection time
        // If no valid connection exists, result could be empty
        assertFalse(transports.isEmpty());
        assertTrue(transports.size() >= 2, "Expected multi-leg journey with at least one connection");

        for (Transport t : transports) {
            System.out.println(t.getDepartureCity() + " -> " + t.getArrivalCity() + " at " + 
            t.getDepartureDateTime() + " and arrives at " + t.getArrivalDateTime());
        }
        
        // Validate connection times
        for (int i = 0; i < transports.size() - 1; i++) {
            Transport firstLeg = transports.get(i);
            Transport secondLeg = transports.get(i + 1);
            long connectionTime = Duration.between(firstLeg.getArrivalDateTime(), secondLeg.getDepartureDateTime()).toMinutes();
            assertTrue(connectionTime >= 60, "Connection time should be at least 60 minutes");
        }

        System.out.println("Minimal gap between connections is "+
            (transports.size() > 1 ? 
            Duration.between(
                transports.get(0).getArrivalDateTime(), 
                transports.get(1).getDepartureDateTime()
            ).toMinutes() : "N/A") + " minutes");

        System.out.println("=====SEPARATOR=====");
    }

    //The data set for this test is constructed such that no valid connections with sufficient 
    // time exist
    @Test
    void testFindTransports_NoSuficcientConnectionTime_ShouldReturnEmpty() {
        // Test that journeys with insufficient connection time are not returned
        // Using a departure time that would lead to tight connections
        LocalDateTime departureDate = LocalDateTime.of(2026, 2, 18, 12, 0);
        
        List<Transport> transports = transportService.findTransports("Lille", "Eindhoven", 
        departureDate, null);
        
        assertNotNull(transports);
        // If no valid connections with sufficient time exist, result should be empty
        assertTrue(transports.isEmpty(), "Expected no journeys due to insufficient connection time");

        System.out.println("=====SEPARATOR=====");
    }
}
