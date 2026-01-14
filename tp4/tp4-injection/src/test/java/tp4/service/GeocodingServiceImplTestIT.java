package tp4.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tp4.WeatherException;
import tp4.model.Coordinates;

import static org.junit.jupiter.api.Assertions.*;

class GeocodingServiceImplTestIT {
    private GeocodingServiceImpl geocodingService;

    @BeforeEach
    void setUp() {
        geocodingService = new GeocodingServiceImpl();
    }

    @Test
    void should_convert_real_address_to_coordinates() throws WeatherException {
        String address = "ENSEIRB-MATMECA";
        
        Coordinates coords = geocodingService.getCoordinates(address);
        
        assertNotNull(coords);
        assertTrue(coords.getLatitude() > 44.0 && coords.getLatitude() < 45.0, 
            "Latitude should be around Bordeaux area");
        assertTrue(coords.getLongitude() < 0 && coords.getLongitude() > -1.0,
            "Longitude should be around Bordeaux area");
    }

    @Test
    void should_convert_postal_address() throws WeatherException {
        String address = "Talence, France";
        
        Coordinates coords = geocodingService.getCoordinates(address);
        
        assertNotNull(coords);
        assertTrue(coords.getLatitude() > 40.0 && coords.getLatitude() < 50.0,
            "Latitude should be in France");
        assertTrue(coords.getLongitude() > -5.0 && coords.getLongitude() < 10.0,
            "Longitude should be in France");
    }

    @Test
    void should_handle_invalid_address_gracefully() {
        String invalidAddress = "xyzabc123nonexistentplace456thatdoesnotexist";
        
        WeatherException exception = assertThrows(WeatherException.class, 
            () -> geocodingService.getCoordinates(invalidAddress));
        
        assertTrue(exception.getMessage().contains("No coordinates found") ||
                   exception.getMessage().contains("Geocoding API error"));
    }

    @Test
    void should_enforce_rate_limiting_between_requests() throws WeatherException {
        String address1 = "Paris";
        String address2 = "Lyon";
        
        long start = System.currentTimeMillis();
        geocodingService.getCoordinates(address1);
        geocodingService.getCoordinates(address2);
        long duration = System.currentTimeMillis() - start;
        
        assertTrue(duration >= 1000, "Should wait at least 1 second between requests");
    }
}
