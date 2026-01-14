package fr.univ.holitrip.testhelpers.stubs;

import fr.univ.holitrip.model.Coordinates;
import fr.univ.holitrip.service.GeocodingService;
import fr.univ.holitrip.exception.GeocodingException;

/**
 * Small deterministic geocoding stub for integration tests.
 * Returns coordinates based on address content.
 */
public class TestGeocodingService implements GeocodingService {
    @Override
    public Coordinates geocode(String address) throws GeocodingException {
        if (address == null) throw new GeocodingException("Null address");
        String addr = address.toLowerCase();
        if (addr.contains("1 rue de test") || addr.contains("test hotel")) {
            return new Coordinates(48.8566, 2.3522); // Paris center
        }
        if (addr.contains("2 rue de test") || addr.contains("museum test")) {
            return new Coordinates(48.8570, 2.3525); // very close
        }
        // Far address -> ~200 km away (coordinates near Lyon to be far enough)
        if (addr.contains("100 far road")) {
            return new Coordinates(45.7640, 4.8357); // Lyon
        }
        // Default: return Paris center
        return new Coordinates(48.8566, 2.3522);
    }
}
