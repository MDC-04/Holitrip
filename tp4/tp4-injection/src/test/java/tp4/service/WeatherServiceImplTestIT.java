package tp4.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tp4.WeatherException;
import tp4.model.Coordinates;
import tp4.model.WeatherData;

import static org.junit.jupiter.api.Assertions.*;

class WeatherServiceImplTestIT {
    private WeatherServiceImpl weatherService;

    @BeforeEach
    void setUp() {
        weatherService = new WeatherServiceImpl();
    }

    @Test
    void should_fetch_real_weather_data() throws WeatherException {
        Coordinates coords = new Coordinates(44.80662, -0.60515);
        
        WeatherData weatherData = weatherService.getWeatherData(coords);
        
        assertNotNull(weatherData);
        assertTrue(weatherData.getTemperature() > -50 && weatherData.getTemperature() < 60);
        assertTrue(weatherData.getRain() >= 0);
        assertTrue(weatherData.getWindSpeed() >= 0);
    }

    @Test
    void should_throw_exception_for_invalid_coordinates() {
        Coordinates invalidCoords = new Coordinates(999.0, 999.0);
        
        assertThrows(WeatherException.class, () -> weatherService.getWeatherData(invalidCoords));
    }

    @Test
    void should_handle_multiple_requests() throws WeatherException {
        Coordinates coords1 = new Coordinates(44.80662, -0.60515);
        Coordinates coords2 = new Coordinates(48.8566, 2.3522);
        
        WeatherData data1 = weatherService.getWeatherData(coords1);
        WeatherData data2 = weatherService.getWeatherData(coords2);
        
        assertNotNull(data1);
        assertNotNull(data2);
    }
}
