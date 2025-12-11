package fr.univ.holitrip.exception;

/**
 * Exception thrown when geocoding fails (e.g., invalid address, API error).
 */
public class GeocodingException extends Exception {
    public GeocodingException(String message) {
        super(message);
    }

    public GeocodingException(String message, Throwable cause) {
        super(message, cause);
    }
}
