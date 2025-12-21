package fr.univ.holitrip.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.univ.holitrip.model.Transport;
import fr.univ.holitrip.service.impl.JsonTransportService;

public class TransportServiceTest {
    private TransportService transportService;

    @BeforeEach
    void setUp() {
        transportService = new JsonTransportService("data/transports.json");
    }

    @Test
    void testFindTransports_BordeauxParis() {
        //ARRANGE
        String departureCity = "Bordeaux"; 
        String arrivalCity = "Paris";
        LocalDateTime departureDate = LocalDateTime.of(2025, 1, 15, 8, 0);
        String mode = null;
        
        //ACT
        List <Transport> transports = transportService.findTransports(departureCity, arrivalCity, departureDate, mode);

        //ASSERT
        assertNotNull(transports);
        assertFalse(transports.isEmpty());
        for (Transport transport : transports) {
            assertEquals("Bordeaux", transport.getDepartureCity());
            assertEquals("Paris", transport.getArrivalCity());
        }
    }

    @Test
    void testFindTransports_BordeauxParis_TrainOnly_ShouldReturn1() {
        //ARRANGE
        String departureCity = "Bordeaux";
        String arrivalCity = "Paris";
        LocalDateTime departureDate = LocalDateTime.of(2025, 1, 15, 8, 0);
        String mode = "TRAIN";
        
        //ACT
        List<Transport> transports = transportService.findTransports(departureCity, arrivalCity, departureDate, mode);
        
        //ASSERT
        assertNotNull(transports);
        assertEquals(1, transports.size());
        assertEquals("TRAIN", transports.get(0).getMode());
    }

    @Test
    void testFindTransports_BordeauxParis_PlaneOnly_ShouldReturn1() {
        //ARRANGE
        String departureCity = "Bordeaux";
        String arrivalCity = "Paris";
        LocalDateTime departureDate = LocalDateTime.of(2025, 1, 15, 8, 0);
        String mode = "PLANE";
        
        //ACT
        List<Transport> transports = transportService.findTransports(departureCity, arrivalCity, departureDate, mode);
        
        //ASSERT
        assertNotNull(transports);
        assertEquals(1, transports.size());
        assertEquals("PLANE", transports.get(0).getMode());
    }

    @Test
    void testFindTransports_InvalidCity_ShouldReturnEmpty() {
        //ARRANGE
        String departureCity = "InvalidCity";
        String arrivalCity = "Paris";
        LocalDateTime departureDate = LocalDateTime.of(2025, 1, 15, 8, 0);
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
        LocalDateTime departureDate = LocalDateTime.of(2025, 1, 15, 8, 0);
        String mode = "train";             // Minuscules
        
        //ACT
        List<Transport> transports = transportService.findTransports(departureCity, arrivalCity, departureDate, mode);
        
        //ASSERT
        assertNotNull(transports);
        assertFalse(transports.isEmpty());
    }
}

