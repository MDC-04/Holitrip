package tp4.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tp4.model.Alert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AlertServiceImplTest {
    private String testFilePath;
    private AlertServiceImpl alertService;

    @BeforeEach
    void setUp() {
        testFilePath = "test-alerts-" + System.currentTimeMillis() + ".csv";
        alertService = new AlertServiceImpl(testFilePath);
    }

    @Test
    void should_write_alert_to_file() throws IOException {
        LocalDateTime timestamp = LocalDateTime.of(2025, 11, 24, 10, 30);
        Alert alert = new Alert("user1", "Test Address", "temperature", 
            -5.0, 0.0, "min", timestamp);

        alertService.sendAlert(alert);

        File file = new File(testFilePath);
        assertTrue(file.exists());

        try (BufferedReader reader = new BufferedReader(new FileReader(testFilePath))) {
            String header = reader.readLine();
            assertNotNull(header);
            assertTrue(header.contains("userId"));
            
            String line = reader.readLine();
            assertNotNull(line);
            assertTrue(line.contains("user1"));
            assertTrue(line.contains("Test Address"));
            assertTrue(line.contains("temperature"));
        } finally {
            file.delete();
        }
    }

    @Test
    void should_append_multiple_alerts() throws IOException {
        LocalDateTime timestamp = LocalDateTime.now();
        Alert alert1 = new Alert("user1", "Address1", "temperature", -5.0, 0.0, "min", timestamp);
        Alert alert2 = new Alert("user2", "Address2", "wind", 25.0, 20.0, "max", timestamp);

        alertService.sendAlert(alert1);
        alertService.sendAlert(alert2);

        File file = new File(testFilePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(testFilePath))) {
            String header = reader.readLine();
            assertNotNull(header);
            
            String line1 = reader.readLine();
            String line2 = reader.readLine();
            assertNotNull(line1);
            assertNotNull(line2);
            assertTrue(line1.contains("user1"));
            assertTrue(line2.contains("user2"));
        } finally {
            file.delete();
        }
    }
}
