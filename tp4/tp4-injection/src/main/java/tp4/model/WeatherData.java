package tp4.model;

public class WeatherData {
    private final double temperature;
    private final double rain;
    private final double windSpeed;

    public WeatherData(double temperature, double rain, double windSpeed) {
        this.temperature = temperature;
        this.rain = rain;
        this.windSpeed = windSpeed;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getRain() {
        return rain;
    }

    public double getWindSpeed() {
        return windSpeed;
    }
}
