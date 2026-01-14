package tp4;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tp4.model.*;
import tp4.service.AlertService;
import tp4.service.GeocodingService;
import tp4.service.WeatherService;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TempGuardianTest {
    private WeatherService weatherService;
    private GeocodingService geocodingService;
    private AlertService alertService;
    private TempGuardian tempGuardian;

    @BeforeEach
    void setUp() {
        weatherService = mock(WeatherService.class);
        geocodingService = mock(GeocodingService.class);
        alertService = mock(AlertService.class);
        tempGuardian = new TempGuardian(weatherService, geocodingService, alertService);
    }

    @Test
    void should_send_alert_when_temperature_below_threshold() throws Exception {
        Thresholds thresholds = new Thresholds();
        thresholds.setTemperatureMin(new Threshold(0.0, ThresholdDirection.BELOW));
        Address address = new Address("Test Address", thresholds, true);
        User user = new User("user1", List.of(address), true);

        Coordinates coords = new Coordinates(44.8, -0.6);
        WeatherData weatherData = new WeatherData(-5.0, 0.0, 10.0);

        when(geocodingService.getCoordinates("Test Address")).thenReturn(coords);
        when(weatherService.getWeatherData(coords)).thenReturn(weatherData);

        tempGuardian.checkAlertsForUser(user);

        ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);
        verify(alertService, times(1)).sendAlert(alertCaptor.capture());
        
        Alert alert = alertCaptor.getValue();
        assertEquals("user1", alert.getUserId());
        assertEquals("temperature", alert.getDataType());
        assertEquals(-5.0, alert.getMeasuredValue());
        assertEquals(0.0, alert.getThreshold());
        assertEquals("min", alert.getThresholdType());
    }

    @Test
    void should_send_alert_when_temperature_above_threshold() throws Exception {
        Thresholds thresholds = new Thresholds();
        thresholds.setTemperatureMax(new Threshold(30.0, ThresholdDirection.ABOVE));
        Address address = new Address("Test Address", thresholds, true);
        User user = new User("user1", List.of(address), true);

        Coordinates coords = new Coordinates(44.8, -0.6);
        WeatherData weatherData = new WeatherData(35.0, 0.0, 10.0);

        when(geocodingService.getCoordinates("Test Address")).thenReturn(coords);
        when(weatherService.getWeatherData(coords)).thenReturn(weatherData);

        tempGuardian.checkAlertsForUser(user);

        verify(alertService, times(1)).sendAlert(any(Alert.class));
    }

    @Test
    void should_not_send_alert_when_temperature_within_thresholds() throws Exception {
        Thresholds thresholds = new Thresholds();
        thresholds.setTemperatureMin(new Threshold(0.0, ThresholdDirection.BELOW));
        thresholds.setTemperatureMax(new Threshold(30.0, ThresholdDirection.ABOVE));
        Address address = new Address("Test Address", thresholds, true);
        User user = new User("user1", List.of(address), true);

        Coordinates coords = new Coordinates(44.8, -0.6);
        WeatherData weatherData = new WeatherData(20.0, 0.0, 10.0);

        when(geocodingService.getCoordinates("Test Address")).thenReturn(coords);
        when(weatherService.getWeatherData(coords)).thenReturn(weatherData);

        tempGuardian.checkAlertsForUser(user);

        verify(alertService, never()).sendAlert(any(Alert.class));
    }

    @Test
    void should_not_send_alert_when_global_alerts_disabled() {
        Thresholds thresholds = new Thresholds();
        thresholds.setTemperatureMin(new Threshold(0.0, ThresholdDirection.BELOW));
        Address address = new Address("Test Address", thresholds, true);
        User user = new User("user1", List.of(address), false);

        tempGuardian.checkAlertsForUser(user);

        verifyNoInteractions(geocodingService, weatherService, alertService);
    }

    @Test
    void should_not_send_alert_when_address_alerts_disabled() {
        Thresholds thresholds = new Thresholds();
        thresholds.setTemperatureMin(new Threshold(0.0, ThresholdDirection.BELOW));
        Address address = new Address("Test Address", thresholds, false);
        User user = new User("user1", List.of(address), true);

        tempGuardian.checkAlertsForUser(user);

        verifyNoInteractions(geocodingService, weatherService, alertService);
    }

    @Test
    void should_send_multiple_alerts_when_multiple_thresholds_breached() throws Exception {
        Thresholds thresholds = new Thresholds();
        thresholds.setTemperatureMin(new Threshold(0.0, ThresholdDirection.BELOW));
        thresholds.setRainMax(new Threshold(5.0, ThresholdDirection.ABOVE));
        Address address = new Address("Test Address", thresholds, true);
        User user = new User("user1", List.of(address), true);

        Coordinates coords = new Coordinates(44.8, -0.6);
        WeatherData weatherData = new WeatherData(-5.0, 10.0, 10.0);

        when(geocodingService.getCoordinates("Test Address")).thenReturn(coords);
        when(weatherService.getWeatherData(coords)).thenReturn(weatherData);

        tempGuardian.checkAlertsForUser(user);

        verify(alertService, times(2)).sendAlert(any(Alert.class));
    }

    @Test
    void should_call_services_for_each_address() throws Exception {
        Thresholds thresholds1 = new Thresholds();
        thresholds1.setTemperatureMin(new Threshold(0.0, ThresholdDirection.BELOW));
        Address address1 = new Address("Address 1", thresholds1, true);
        
        Thresholds thresholds2 = new Thresholds();
        thresholds2.setWindSpeedMax(new Threshold(20.0, ThresholdDirection.ABOVE));
        Address address2 = new Address("Address 2", thresholds2, true);
        
        User user = new User("user1", List.of(address1, address2), true);

        Coordinates coords1 = new Coordinates(44.8, -0.6);
        Coordinates coords2 = new Coordinates(45.0, -1.0);
        WeatherData weatherData = new WeatherData(20.0, 0.0, 10.0);

        when(geocodingService.getCoordinates("Address 1")).thenReturn(coords1);
        when(geocodingService.getCoordinates("Address 2")).thenReturn(coords2);
        when(weatherService.getWeatherData(coords1)).thenReturn(weatherData);
        when(weatherService.getWeatherData(coords2)).thenReturn(weatherData);

        tempGuardian.checkAlertsForUser(user);

        verify(geocodingService, times(1)).getCoordinates("Address 1");
        verify(geocodingService, times(1)).getCoordinates("Address 2");
        verify(weatherService, times(1)).getWeatherData(coords1);
        verify(weatherService, times(1)).getWeatherData(coords2);
    }

    @Test
    void should_send_wind_alert_when_threshold_exceeded() throws Exception {
        Thresholds thresholds = new Thresholds();
        thresholds.setWindSpeedMax(new Threshold(15.0, ThresholdDirection.ABOVE));
        Address address = new Address("Test Address", thresholds, true);
        User user = new User("user1", List.of(address), true);

        Coordinates coords = new Coordinates(44.8, -0.6);
        WeatherData weatherData = new WeatherData(20.0, 0.0, 25.0);

        when(geocodingService.getCoordinates("Test Address")).thenReturn(coords);
        when(weatherService.getWeatherData(coords)).thenReturn(weatherData);

        tempGuardian.checkAlertsForUser(user);

        ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);
        verify(alertService, times(1)).sendAlert(alertCaptor.capture());
        
        Alert alert = alertCaptor.getValue();
        assertEquals("wind", alert.getDataType());
        assertEquals(25.0, alert.getMeasuredValue());
    }

    @Test
    void should_send_rain_alert_when_threshold_exceeded() throws Exception {
        Thresholds thresholds = new Thresholds();
        thresholds.setRainMax(new Threshold(5.0, ThresholdDirection.ABOVE));
        Address address = new Address("Test Address", thresholds, true);
        User user = new User("user1", List.of(address), true);

        Coordinates coords = new Coordinates(44.8, -0.6);
        WeatherData weatherData = new WeatherData(20.0, 10.0, 10.0);

        when(geocodingService.getCoordinates("Test Address")).thenReturn(coords);
        when(weatherService.getWeatherData(coords)).thenReturn(weatherData);

        tempGuardian.checkAlertsForUser(user);

        ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);
        verify(alertService, times(1)).sendAlert(alertCaptor.capture());
        
        Alert alert = alertCaptor.getValue();
        assertEquals("rain", alert.getDataType());
        assertEquals(10.0, alert.getMeasuredValue());
    }

    @Test
    void should_continue_processing_when_one_address_fails() throws Exception {
        Thresholds thresholds = new Thresholds();
        thresholds.setTemperatureMax(new Threshold(30.0, ThresholdDirection.ABOVE));
        Address address1 = new Address("Failing Address", thresholds, true);
        Address address2 = new Address("Working Address", thresholds, true);
        User user = new User("user1", List.of(address1, address2), true);

        when(geocodingService.getCoordinates("Failing Address"))
                .thenThrow(new WeatherException("Service error"));
        
        Coordinates coords2 = new Coordinates(45.0, -1.0);
        WeatherData weatherData = new WeatherData(35.0, 0.0, 10.0);
        when(geocodingService.getCoordinates("Working Address")).thenReturn(coords2);
        when(weatherService.getWeatherData(coords2)).thenReturn(weatherData);

        tempGuardian.checkAlertsForUser(user);

        verify(geocodingService, times(1)).getCoordinates("Failing Address");
        verify(geocodingService, times(1)).getCoordinates("Working Address");
        verify(alertService, times(1)).sendAlert(any(Alert.class));
    }

    @Test
    void should_handle_alert_service_exception_gracefully() throws Exception {
        Thresholds thresholds = new Thresholds();
        thresholds.setTemperatureMin(new Threshold(0.0, ThresholdDirection.BELOW));
        Address address = new Address("Test Address", thresholds, true);
        User user = new User("user1", List.of(address), true);

        Coordinates coords = new Coordinates(44.8, -0.6);
        WeatherData weatherData = new WeatherData(-5.0, 0.0, 10.0);

        when(geocodingService.getCoordinates("Test Address")).thenReturn(coords);
        when(weatherService.getWeatherData(coords)).thenReturn(weatherData);
        doThrow(new IOException("Write error")).when(alertService).sendAlert(any(Alert.class));

        assertDoesNotThrow(() -> tempGuardian.checkAlertsForUser(user));
        
        verify(alertService, times(1)).sendAlert(any(Alert.class));
    }

    @Test
    void should_not_send_alert_when_no_thresholds_configured() throws Exception {
        Thresholds thresholds = new Thresholds();
        Address address = new Address("Test Address", thresholds, true);
        User user = new User("user1", List.of(address), true);

        Coordinates coords = new Coordinates(44.8, -0.6);
        WeatherData weatherData = new WeatherData(-5.0, 10.0, 25.0);

        when(geocodingService.getCoordinates("Test Address")).thenReturn(coords);
        when(weatherService.getWeatherData(coords)).thenReturn(weatherData);

        tempGuardian.checkAlertsForUser(user);

        verify(alertService, never()).sendAlert(any(Alert.class));
    }

    @Test
    void should_handle_boundary_temperature_at_threshold() throws Exception {
        Thresholds thresholds = new Thresholds();
        thresholds.setTemperatureMin(new Threshold(0.0, ThresholdDirection.BELOW));
        Address address = new Address("Test Address", thresholds, true);
        User user = new User("user1", List.of(address), true);

        Coordinates coords = new Coordinates(44.8, -0.6);
        WeatherData weatherData = new WeatherData(0.0, 0.0, 10.0);

        when(geocodingService.getCoordinates("Test Address")).thenReturn(coords);
        when(weatherService.getWeatherData(coords)).thenReturn(weatherData);

        tempGuardian.checkAlertsForUser(user);

        verify(alertService, never()).sendAlert(any(Alert.class));
    }
}
