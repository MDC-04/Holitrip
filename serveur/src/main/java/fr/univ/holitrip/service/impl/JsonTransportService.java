package fr.univ.holitrip.service.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import fr.univ.holitrip.model.Transport;
import fr.univ.holitrip.service.TransportService;

/**
 * JSON-based implementation of TransportService.
 * Reads transport data from a JSON file and filters based on criteria.
 */
public class JsonTransportService implements TransportService {
    private String jsonFilePath;

    public JsonTransportService(String jsonFilePath) {
        this.jsonFilePath = jsonFilePath;
    }

    @Override
    public List<Transport> findTransports(String departureCity, String arrivalCity, LocalDateTime departureDate, String mode) {
        try {
            // 1. Read JSON file
            InputStream is = getClass().getClassLoader().getResourceAsStream(jsonFilePath);
            if (is == null) {
                return new ArrayList<>();
            }

            // 2. Parse JSON with Gson (custom adapter for LocalDateTime)
            Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, 
                    (JsonDeserializer<LocalDateTime>) (json, type, context) -> 
                        LocalDateTime.parse(json.getAsString()))
                .create();
            java.lang.reflect.Type listType = new TypeToken<List<Transport>>() {}.getType();
            List<Transport> allTransports = gson.fromJson(new InputStreamReader(is), listType);

            // 3. Filter by criteria
            return allTransports.stream()
                .filter(t -> t.getDepartureCity().equalsIgnoreCase(departureCity))
                .filter(t -> t.getArrivalCity().equalsIgnoreCase(arrivalCity))
                .filter(t -> mode == null || t.getMode().equalsIgnoreCase(mode))
                .collect(Collectors.toList());
        } catch (Exception e) {
            // In case of error: return empty list
            return new ArrayList<>();
        }
    }

}
