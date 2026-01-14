package tp4.config;

import tp4.model.Address;
import tp4.model.Threshold;
import tp4.model.ThresholdDirection;
import tp4.model.Thresholds;
import tp4.model.User;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserConfigReader {
    
    public List<User> readUsers(String filePath) throws IOException {
        Map<String, UserBuilder> userBuilders = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine();
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                
                if (parts.length < 10) {
                    continue;
                }
                
                String userId = parts[0].trim();
                boolean globalAlertsEnabled = Boolean.parseBoolean(parts[1].trim());
                String address = parts[2].trim();
                boolean addressAlertsEnabled = Boolean.parseBoolean(parts[3].trim());
                
                Thresholds thresholds = new Thresholds();
                if (!parts[4].isEmpty()) thresholds.setTemperatureMin(
                        new Threshold(Double.parseDouble(parts[4]), ThresholdDirection.BELOW));
                if (!parts[5].isEmpty()) thresholds.setTemperatureMax(
                        new Threshold(Double.parseDouble(parts[5]), ThresholdDirection.ABOVE));
                if (!parts[6].isEmpty()) thresholds.setWindSpeedMin(
                        new Threshold(Double.parseDouble(parts[6]), ThresholdDirection.BELOW));
                if (!parts[7].isEmpty()) thresholds.setWindSpeedMax(
                        new Threshold(Double.parseDouble(parts[7]), ThresholdDirection.ABOVE));
                if (!parts[8].isEmpty()) thresholds.setRainMin(
                        new Threshold(Double.parseDouble(parts[8]), ThresholdDirection.BELOW));
                if (!parts[9].isEmpty()) thresholds.setRainMax(
                        new Threshold(Double.parseDouble(parts[9]), ThresholdDirection.ABOVE));
                
                UserBuilder builder = userBuilders.computeIfAbsent(userId, 
                    k -> new UserBuilder(userId, globalAlertsEnabled));
                
                builder.addAddress(new Address(address, thresholds, addressAlertsEnabled));
            }
        }
        
        List<User> users = new ArrayList<>();
        for (UserBuilder builder : userBuilders.values()) {
            users.add(builder.build());
        }
        
        return users;
    }
    
    private static class UserBuilder {
        private final String userId;
        private final boolean globalAlertsEnabled;
        private final List<Address> addresses = new ArrayList<>();
        
        UserBuilder(String userId, boolean globalAlertsEnabled) {
            this.userId = userId;
            this.globalAlertsEnabled = globalAlertsEnabled;
        }
        
        void addAddress(Address address) {
            addresses.add(address);
        }
        
        User build() {
            return new User(userId, addresses, globalAlertsEnabled);
        }
    }
}
