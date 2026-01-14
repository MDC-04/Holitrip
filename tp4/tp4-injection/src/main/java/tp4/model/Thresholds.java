package tp4.model;

public class Thresholds {
    private Threshold temperatureMin;
    private Threshold temperatureMax;
    private Threshold windSpeedMin;
    private Threshold windSpeedMax;
    private Threshold rainMin;
    private Threshold rainMax;

    public Threshold getTemperatureMin() {
        return temperatureMin;
    }

    public void setTemperatureMin(Threshold temperatureMin) {
        this.temperatureMin = temperatureMin;
    }

    public Threshold getTemperatureMax() {
        return temperatureMax;
    }

    public void setTemperatureMax(Threshold temperatureMax) {
        this.temperatureMax = temperatureMax;
    }

    public Threshold getWindSpeedMin() {
        return windSpeedMin;
    }

    public void setWindSpeedMin(Threshold windSpeedMin) {
        this.windSpeedMin = windSpeedMin;
    }

    public Threshold getWindSpeedMax() {
        return windSpeedMax;
    }

    public void setWindSpeedMax(Threshold windSpeedMax) {
        this.windSpeedMax = windSpeedMax;
    }

    public Threshold getRainMin() {
        return rainMin;
    }

    public void setRainMin(Threshold rainMin) {
        this.rainMin = rainMin;
    }

    public Threshold getRainMax() {
        return rainMax;
    }

    public void setRainMax(Threshold rainMax) {
        this.rainMax = rainMax;
    }
}
