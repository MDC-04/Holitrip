package tp4.service;

import tp4.WeatherException;
import tp4.model.Coordinates;

public interface GeocodingService {
    Coordinates getCoordinates(String address) throws WeatherException;
}
