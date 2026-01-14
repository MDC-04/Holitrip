package tp4.service;

import tp4.WeatherException;
import tp4.model.Coordinates;
import tp4.model.WeatherData;

public interface WeatherService {
    WeatherData getWeatherData(Coordinates coordinates) throws WeatherException;
}
