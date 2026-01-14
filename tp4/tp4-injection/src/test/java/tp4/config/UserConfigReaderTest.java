package tp4.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tp4.model.User;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserConfigReaderTest {
    private UserConfigReader configReader;
    private String testFilePath;

    @BeforeEach
    void setUp() {
        configReader = new UserConfigReader();
        testFilePath = "test-config-" + System.currentTimeMillis() + ".csv";
    }

    @Test
    void should_read_single_user_with_one_address() throws IOException {
        createConfigFile(
            "userId,globalAlertsEnabled,address,addressAlertsEnabled,minTemp,maxTemp,minWind,maxWind,minRain,maxRain",
            "user1,true,Test Address,true,-1.0,30.0,,,,"
        );

        List<User> users = configReader.readUsers(testFilePath);

        assertEquals(1, users.size());
        User user = users.get(0);
        assertEquals("user1", user.getUserId());
        assertTrue(user.isGlobalAlertsEnabled());
        assertEquals(1, user.getAddresses().size());
        assertEquals("Test Address", user.getAddresses().get(0).getPostalAddress());
        
        deleteTestFile();
    }

    @Test
    void should_read_user_with_multiple_addresses() throws IOException {
        createConfigFile(
            "userId,globalAlertsEnabled,address,addressAlertsEnabled,minTemp,maxTemp,minWind,maxWind,minRain,maxRain",
            "user1,true,Address1,true,-1.0,30.0,,,,",
            "user1,true,Address2,true,,,,,0.0,5.0"
        );

        List<User> users = configReader.readUsers(testFilePath);

        assertEquals(1, users.size());
        User user = users.get(0);
        assertEquals(2, user.getAddresses().size());
        
        deleteTestFile();
    }

    @Test
    void should_read_multiple_users() throws IOException {
        createConfigFile(
            "userId,globalAlertsEnabled,address,addressAlertsEnabled,minTemp,maxTemp,minWind,maxWind,minRain,maxRain",
            "user1,true,Address1,true,-1.0,30.0,,,,",
            "user2,false,Address2,true,,,,,0.0,5.0"
        );

        List<User> users = configReader.readUsers(testFilePath);

        assertEquals(2, users.size());
        
        deleteTestFile();
    }

    @Test
    void should_handle_disabled_global_alerts() throws IOException {
        createConfigFile(
            "userId,globalAlertsEnabled,address,addressAlertsEnabled,minTemp,maxTemp,minWind,maxWind,minRain,maxRain",
            "user1,false,Address1,true,-1.0,30.0,,,,"
        );

        List<User> users = configReader.readUsers(testFilePath);

        assertEquals(1, users.size());
        assertFalse(users.get(0).isGlobalAlertsEnabled());
        
        deleteTestFile();
    }

    @Test
    void should_handle_disabled_address_alerts() throws IOException {
        createConfigFile(
            "userId,globalAlertsEnabled,address,addressAlertsEnabled,minTemp,maxTemp,minWind,maxWind,minRain,maxRain",
            "user1,true,Address1,false,-1.0,30.0,,,,"
        );

        List<User> users = configReader.readUsers(testFilePath);

        assertEquals(1, users.size());
        assertFalse(users.get(0).getAddresses().get(0).isAlertsEnabled());
        
        deleteTestFile();
    }

    @Test
    void should_handle_empty_thresholds() throws IOException {
        createConfigFile(
            "userId,globalAlertsEnabled,address,addressAlertsEnabled,minTemp,maxTemp,minWind,maxWind,minRain,maxRain",
            "user1,true,Address1,true,,,,,,"
        );

        List<User> users = configReader.readUsers(testFilePath);

        assertEquals(1, users.size());
                assertNull(users.get(0).getAddresses().get(0).getThresholds().getTemperatureMin());        deleteTestFile();
    }

    private void createConfigFile(String... lines) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(testFilePath))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    private void deleteTestFile() {
        new File(testFilePath).delete();
    }
}
