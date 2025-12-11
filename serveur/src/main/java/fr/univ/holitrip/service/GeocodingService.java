package fr.univ.holitrip.service;

import fr.univ.holitrip.model.Coordinates;
import fr.univ.holitrip.exception.GeocodingException;

/**
 * Service for converting addresses to GPS coordinates.
 * This allows calculating distances between cities for transport routing.
 */
public interface GeocodingService {
    /**
     * Converts an address (e.g., "Paris, France") to GPS coordinates.
     *
     * @param address the address to geocode
     * @return the GPS coordinates (latitude, longitude)
     * @throws GeocodingException if the address cannot be geocoded
     */
    Coordinates geocode(String address) throws GeocodingException;
}
