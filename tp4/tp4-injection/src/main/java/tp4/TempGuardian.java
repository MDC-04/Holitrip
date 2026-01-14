package tp4;

import tp4.model.*;
import tp4.service.AlertService;
import tp4.service.GeocodingService;
import tp4.service.WeatherService;
import tp4.model.ThresholdDirection;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TempGuardian {
    private final WeatherService weatherService;
    private final GeocodingService geocodingService;
    private final AlertService alertService;

    public TempGuardian(WeatherService weatherService, GeocodingService geocodingService, AlertService alertService) {
        this.weatherService = weatherService;
        this.geocodingService = geocodingService;
        this.alertService = alertService;
    }

    public void checkAlertsForUser(User user) {
        if (!user.isGlobalAlertsEnabled()) {
            return;
        }

        for (Address address : user.getAddresses()) {
            processAddress(user.getUserId(), address);
        }
    }

    private void processAddress(String userId, Address address) {
        if (!address.isAlertsEnabled()) {
            return;
        }

        try {
            Coordinates coords = geocodingService.getCoordinates(address.getPostalAddress());
            WeatherData weatherData = weatherService.getWeatherData(coords);
            
            List<Alert> alerts = checkThresholds(userId, address, weatherData);
            
            for (Alert alert : alerts) {
                alertService.sendAlert(alert);
            }
        } catch (Exception e) {
            System.err.println("Error processing address '" + address.getPostalAddress() + 
                    "' for user " + userId + ": " + e.getMessage());
        }
    }

    private List<Alert> checkThresholds(String userId, Address address, WeatherData weatherData) {
        List<Alert> alerts = new ArrayList<>();
        Thresholds thresholds = address.getThresholds();
        LocalDateTime now = LocalDateTime.now();

        checkAndAddAlert(alerts, userId, address.getPostalAddress(), "temperature",
                weatherData.getTemperature(), thresholds.getTemperatureMin(), now);
        checkAndAddAlert(alerts, userId, address.getPostalAddress(), "temperature",
                weatherData.getTemperature(), thresholds.getTemperatureMax(), now);

        checkAndAddAlert(alerts, userId, address.getPostalAddress(), "wind",
                weatherData.getWindSpeed(), thresholds.getWindSpeedMin(), now);
        checkAndAddAlert(alerts, userId, address.getPostalAddress(), "wind",
                weatherData.getWindSpeed(), thresholds.getWindSpeedMax(), now);

        checkAndAddAlert(alerts, userId, address.getPostalAddress(), "rain",
                weatherData.getRain(), thresholds.getRainMin(), now);
        checkAndAddAlert(alerts, userId, address.getPostalAddress(), "rain",
                weatherData.getRain(), thresholds.getRainMax(), now);

        return alerts;
    }

    private void checkAndAddAlert(List<Alert> alerts, String userId, String address, String dataType,
                                   double measuredValue, Threshold threshold, LocalDateTime timestamp) {
        if (threshold != null && threshold.isExceeded(measuredValue)) {
            String thresholdType = threshold.getDirection() == ThresholdDirection.BELOW ? "min" : "max";
            alerts.add(new Alert(userId, address, dataType, measuredValue, 
                    threshold.getValue(), thresholdType, timestamp));
        }
    }
}
