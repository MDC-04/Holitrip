package tp4.service;

import tp4.model.Alert;

import java.io.IOException;

public interface AlertService {
    void sendAlert(Alert alert) throws IOException;
}
