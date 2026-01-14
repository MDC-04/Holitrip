package tp4;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tp4.model.Coordinates;
import tp4.model.Threshold;
import tp4.model.ThresholdDirection;
import tp4.model.Thresholds;
import tp4.model.WeatherData;
import tp4.service.AlertService;
import tp4.service.AlertServiceImpl;
import tp4.service.GeocodingService;
import tp4.service.WeatherService;
import tp4.service.WeatherServiceImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import static org.junit.jupiter.api.Assertions.*;

class TempGuardianTestIT {
    private WeatherService weatherService;
    private String testAlertsFile;

    @BeforeEach
    void setUp() {
        weatherService = new WeatherServiceImpl();
        testAlertsFile = "test-alerts-it-" + System.currentTimeMillis() + ".csv";
    }

    @Test
    void should_get_real_weather_data_for_bordeaux() throws Exception {
        Coordinates bordeaux = new Coordinates(44.837789, -0.57918);
        
        WeatherData data = weatherService.getWeatherData(bordeaux);
        
        assertNotNull(data);
        assertTrue(data.getTemperature() > -50 && data.getTemperature() < 50);
        assertTrue(data.getWindSpeed() >= 0 && data.getWindSpeed() < 200);
        assertTrue(data.getRain() >= 0 && data.getRain() < 100);
    }

    @Test
    void should_get_real_weather_data_for_paris() throws Exception {
        Coordinates paris = new Coordinates(48.8566, 2.3522);
        
        WeatherData data = weatherService.getWeatherData(paris);
        
        assertNotNull(data);
        assertTrue(data.getTemperature() > -50 && data.getTemperature() < 50);
    }

    @Test
    void should_write_alert_when_threshold_breached() throws Exception {
        Coordinates coords = new Coordinates(44.837789, -0.57918);
        WeatherData data = weatherService.getWeatherData(coords);
        AlertService alertService = new AlertServiceImpl(testAlertsFile);
        
        Thresholds thresholds = new Thresholds();
        thresholds.setTemperatureMax(new Threshold(-100.0, ThresholdDirection.ABOVE));
        
        if (thresholds.getTemperatureMax().isExceeded(data.getTemperature())) {
            alertService.sendAlert(new tp4.model.Alert(
                "testuser",
                "Bordeaux",
                "temperature",
                data.getTemperature(),
                -100.0,
                "above",
                java.time.LocalDateTime.now()
            ));
        }

        File alertsFile = new File(testAlertsFile);
        assertTrue(alertsFile.exists());
        
        try (BufferedReader reader = new BufferedReader(new FileReader(testAlertsFile))) {
            String header = reader.readLine();
            assertNotNull(header);
            
            String line = reader.readLine();
            assertNotNull(line);
            assertTrue(line.contains("testuser"));
            assertTrue(line.contains("temperature"));
        } finally {
            alertsFile.delete();
        }
    }

    @Test
    void should_handle_extreme_coordinates() {
        Coordinates yellowknife = new Coordinates(62.45951, -114.39476);
        
        assertDoesNotThrow(() -> {
            WeatherData data = weatherService.getWeatherData(yellowknife);
            assertNotNull(data);
        });
    }
}
