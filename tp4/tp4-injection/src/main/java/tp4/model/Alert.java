package tp4.model;

import java.time.LocalDateTime;

public class Alert {
    private final String userId;
    private final String address;
    private final String dataType;
    private final double measuredValue;
    private final double threshold;
    private final String thresholdType;
    private final LocalDateTime timestamp;

    public Alert(String userId, String address, String dataType, double measuredValue, 
                 double threshold, String thresholdType, LocalDateTime timestamp) {
        this.userId = userId;
        this.address = address;
        this.dataType = dataType;
        this.measuredValue = measuredValue;
        this.threshold = threshold;
        this.thresholdType = thresholdType;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public String getAddress() {
        return address;
    }

    public String getDataType() {
        return dataType;
    }

    public double getMeasuredValue() {
        return measuredValue;
    }

    public double getThreshold() {
        return threshold;
    }

    public String getThresholdType() {
        return thresholdType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
