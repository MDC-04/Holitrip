package fr.univ.holitrip.service.it;

import fr.univ.holitrip.exception.GeocodingException;
import fr.univ.holitrip.model.Coordinates;
import fr.univ.holitrip.service.impl.ApiGeocodingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for ApiGeocodingService.
 * This test makes real calls to the Nominatim OpenStreetMap API.
 * Warning: These tests depend on the availability of the external API.
 */
public class ApiGeocodingServiceIT {

    private ApiGeocodingService service;

    @BeforeEach
    void setUp() {
        // Use real HttpClient (no mocks)
        service = new ApiGeocodingService();
    }

    @Test
    void testGeocodeRealAddress_Bordeaux() throws Exception {
        // Act: Real call to Nominatim API with actual address
        Coordinates coords = service.geocode("Place de la Bourse, 33000 Bordeaux, France");

        // Assert: Verify coordinates are in Bordeaux area
        // Place de la Bourse is approximately at 44.84°N, -0.57°E
        assertNotNull(coords);
        assertTrue(coords.getLatitude() > 44.0 && coords.getLatitude() < 45.5,
                "Bordeaux latitude should be between 44.0 and 45.5, got: " + coords.getLatitude());
        assertTrue(coords.getLongitude() > -1.5 && coords.getLongitude() < 0.0,
                "Bordeaux longitude should be between -1.5 and 0.0, got: " + coords.getLongitude());

        System.out.println("<Bordeaux Place de la Bourse> Coordinates: " + coords);
    }

    @Test
    void testGeocodeRealAddress_Paris() throws Exception {
        // Act: Real call to Nominatim API with actual address
        Coordinates coords = service.geocode("Avenue des Champs-Élysées, 75008 Paris, France");

        // Assert: Verify coordinates are in Paris area
        // Champs-Élysées is approximately at 48.87°N, 2.31°E
        assertNotNull(coords);
        assertTrue(coords.getLatitude() > 48.0 && coords.getLatitude() < 49.5,
                "Paris latitude should be between 48.0 and 49.5, got: " + coords.getLatitude());
        assertTrue(coords.getLongitude() > 1.5 && coords.getLongitude() < 3.0,
                "Paris longitude should be between 1.5 and 3.0, got: " + coords.getLongitude());

        System.out.println("<Paris Avenue des Champs-Élysées> Coordinates: " + coords);
    }

    @Test
    void testGeocodeRealAddress_Lyon() throws Exception {
        // Act: Real call to Nominatim API with actual address
        Coordinates coords = service.geocode("Place Bellecour, 69002 Lyon, France");

        // Assert: Verify coordinates are in Lyon area
        // Place Bellecour is approximately at 45.76°N, 4.83°E
        assertNotNull(coords);
        assertTrue(coords.getLatitude() > 45.0 && coords.getLatitude() < 46.5,
                "Lyon latitude should be between 45.0 and 46.5, got: " + coords.getLatitude());
        assertTrue(coords.getLongitude() > 4.0 && coords.getLongitude() < 5.5,
                "Lyon longitude should be between 4.0 and 5.5, got: " + coords.getLongitude());

        System.out.println("<Lyon Place Bellecour> Coordinates: " + coords);
    }

    @Test
    void testGeocodeRealAddress_EiffelTower() throws Exception {
        // Act: Real call to Nominatim API with famous landmark address
        Coordinates coords = service.geocode("5 Avenue Anatole France, 75007 Paris, France");

        // Assert: Verify coordinates are close to Eiffel Tower location
        // Eiffel Tower is at approximately 48.858°N, 2.294°E
        assertNotNull(coords);
        assertTrue(coords.getLatitude() > 48.85 && coords.getLatitude() < 48.87,
                "Eiffel Tower latitude should be around 48.858, got: " + coords.getLatitude());
        assertTrue(coords.getLongitude() > 2.28 && coords.getLongitude() < 2.31,
                "Eiffel Tower longitude should be around 2.294, got: " + coords.getLongitude());

        System.out.println("<Eiffel Tower> Coordinates: " + coords);
    }

    @Test
    void testGeocodeInvalidAddress_ThrowsException() {
        // Act & Assert: An invalid address should throw an exception
        assertThrows(GeocodingException.class, () -> {
            service.geocode("NonExistentAddress123XYZ999");
        }, "An invalid address should throw GeocodingException");
    }

    @Test
    void testGeocodeEmptyString_ThrowsException() {
        // Act & Assert: An empty string should throw an exception
        assertThrows(GeocodingException.class, () -> {
            service.geocode("");
        }, "An empty address should throw GeocodingException");
    }
}
