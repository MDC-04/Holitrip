package tp4.model;

import java.util.List;

public class User {
    private final String userId;
    private final List<Address> addresses;
    private final boolean globalAlertsEnabled;

    public User(String userId, List<Address> addresses, boolean globalAlertsEnabled) {
        this.userId = userId;
        this.addresses = addresses;
        this.globalAlertsEnabled = globalAlertsEnabled;
    }

    public String getUserId() {
        return userId;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public boolean isGlobalAlertsEnabled() {
        return globalAlertsEnabled;
    }
}
