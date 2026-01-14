package fr.univ.holitrip.service.unit;

import static org.junit.jupiter.api.Assertions.*;

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
    }
}
