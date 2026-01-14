package tp4;

import tp4.config.UserConfigReader;
import tp4.model.User;
import tp4.service.AlertServiceImpl;
import tp4.service.GeocodingServiceImpl;
import tp4.service.WeatherServiceImpl;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java Main <config_file> <alerts_file>");
            System.exit(1);
        }

        String configFile = args[0];
        String alertsFile = args[1];

        try {
            UserConfigReader configReader = new UserConfigReader();
            List<User> users = configReader.readUsers(configFile);

            TempGuardian guardian = new TempGuardian(
                new WeatherServiceImpl(),
                new GeocodingServiceImpl(),
                new AlertServiceImpl(alertsFile)
            );

            for (User user : users) {
                guardian.checkAlertsForUser(user);
            }

            System.out.println("Alert checks completed successfully");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
