package tp4.model;

public class Address {
    private final String postalAddress;
    private final Thresholds thresholds;
    private final boolean alertsEnabled;

    public Address(String postalAddress, Thresholds thresholds, boolean alertsEnabled) {
        this.postalAddress = postalAddress;
        this.thresholds = thresholds;
        this.alertsEnabled = alertsEnabled;
    }

    public String getPostalAddress() {
        return postalAddress;
    }

    public Thresholds getThresholds() {
        return thresholds;
    }

    public boolean isAlertsEnabled() {
        return alertsEnabled;
    }
}
