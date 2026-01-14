package tp4.service;

import tp4.model.Alert;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class AlertServiceImpl implements AlertService {
    private final String alertsFilePath;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AlertServiceImpl(String alertsFilePath) {
        this.alertsFilePath = alertsFilePath;
    }

    @Override
    public void sendAlert(Alert alert) throws IOException {
        File file = new File(alertsFilePath);
        boolean writeHeader = !file.exists() || file.length() == 0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(alertsFilePath, true))) {
            if (writeHeader) {
                writer.write("userId,address,dataType,measuredValue,threshold,thresholdType,timestamp");
                writer.newLine();
            }

            String line = String.format("%s,%s,%s,%.2f,%.2f,%s,%s",
                alert.getUserId(),
                alert.getAddress(),
                alert.getDataType(),
                alert.getMeasuredValue(),
                alert.getThreshold(),
                alert.getThresholdType(),
                alert.getTimestamp().format(FORMATTER));
            writer.write(line);
            writer.newLine();
        }
    }
}
