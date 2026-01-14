package tp4.model;

public class Threshold {
    private final double value;
    private final ThresholdDirection direction;

    public Threshold(double value, ThresholdDirection direction) {
        this.value = value;
        this.direction = direction;
    }

    public double getValue() {
        return value;
    }

    public ThresholdDirection getDirection() {
        return direction;
    }

    public boolean isExceeded(double actualValue) {
        return direction == ThresholdDirection.ABOVE ? actualValue > value : actualValue < value;
    }
}
