package fr.univ.holitrip.service.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
// import java.lang.ProcessBuilder.Redirect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
//import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
//import java.util.Map;
import java.util.Queue;
import java.util.Set;
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
 * Supports multi-leg journeys using BFS algorithm.
 */
public class JsonTransportService implements TransportService {
    private String jsonFilePath;
    private static final int MAX_LEGS = 3; // Maximum number of legs allowed
    private static final long MIN_CONNECTION_MINUTES = 60; // Minimum connection time

    public JsonTransportService(String jsonFilePath) {
        this.jsonFilePath = jsonFilePath;
    }
    
    /**
     * Represents a path in the journey search (for BFS algorithm).
     */
    private static class JourneyPath {
        String currentCity;
        LocalDateTime arrivalTime;
        List<Transport> transports;
        Set<String> visitedCities;
        String mode; // Track mode for homogeneity
        
        JourneyPath(String city, LocalDateTime time, List<Transport> transports, Set<String> visited, String mode) {
            this.currentCity = city;
            this.arrivalTime = time;
            this.transports = new ArrayList<>(transports);
            this.visitedCities = new HashSet<>(visited);
            this.mode = mode;
        }
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

            // 3. Search for direct transports
            List<Transport> directTransports = allTransports.stream()
                .filter(t -> departureCity == null || t.getDepartureCity().equalsIgnoreCase(departureCity))
                .filter(t -> arrivalCity == null || t.getArrivalCity().equalsIgnoreCase(arrivalCity))
                .filter(t -> departureDate == null || t.getDepartureDateTime().toLocalDate().isEqual(departureDate.toLocalDate()))
                .filter(t -> mode == null || t.getMode().equalsIgnoreCase(mode))
                .collect(Collectors.toList());

            // If direct transports found, return them
            if (!directTransports.isEmpty()) {
                return directTransports;
            }

            // 4. If no direct transport, search for multi-leg journey using BFS
            if (departureCity != null && arrivalCity != null) {
                return findMultiLegJourney(allTransports, departureCity, arrivalCity, departureDate, mode);
            }

            return new ArrayList<>();

        } catch (Exception e) {
            // In case of error: return empty list
            return new ArrayList<>();
        }
    }
    
    /**
     * Find multi-leg journey using BFS algorithm.
     * Ensures mode homogeneity and minimum connection time.
     */
    private List<Transport> findMultiLegJourney(List<Transport> allTransports, String departureCity, 
                                                 String arrivalCity, LocalDateTime departureDate, String mode) {
        Queue<JourneyPath> queue = new LinkedList<>();
        
        // Initialize with first leg options
        for (Transport firstTransport : allTransports) {
            if (!firstTransport.getDepartureCity().equalsIgnoreCase(departureCity)) {
                continue;
            }
            if (departureDate != null && !firstTransport.getDepartureDateTime().toLocalDate().isEqual(departureDate.toLocalDate())) {
                continue;
            }
            if (mode != null && !firstTransport.getMode().equalsIgnoreCase(mode)) {
                continue;
            }
            
            Set<String> visited = new HashSet<>();
            visited.add(departureCity);
            visited.add(firstTransport.getArrivalCity());
            
            List<Transport> path = new ArrayList<>();
            path.add(firstTransport);
            
            queue.add(new JourneyPath(
                firstTransport.getArrivalCity(),
                firstTransport.getArrivalDateTime(),
                path,
                visited,
                firstTransport.getMode()
            ));
        }
        
        // BFS to find path to destination
        while (!queue.isEmpty()) {
            JourneyPath current = queue.poll();
            
            // Check if we reached destination
            if (current.currentCity.equalsIgnoreCase(arrivalCity)) {
                return current.transports;
            }
            
            // Check if we exceeded max legs
            if (current.transports.size() >= MAX_LEGS) {
                continue;
            }
            
            // Explore next legs
            for (Transport nextTransport : allTransports) {
                // Must depart from current city
                if (!nextTransport.getDepartureCity().equalsIgnoreCase(current.currentCity)) {
                    continue;
                }
                
                // Avoid cycles
                if (current.visitedCities.contains(nextTransport.getArrivalCity())) {
                    continue;
                }
                
                // Check mode homogeneity
                if (!nextTransport.getMode().equalsIgnoreCase(current.mode)) {
                    continue;
                }
                
                // Check connection time (minimum 60 minutes)
                if (!nextTransport.getDepartureDateTime().isAfter(current.arrivalTime.plusMinutes(MIN_CONNECTION_MINUTES))) {
                    continue;
                }
                
                // Create new path
                Set<String> newVisited = new HashSet<>(current.visitedCities);
                newVisited.add(nextTransport.getArrivalCity());
                
                List<Transport> newPath = new ArrayList<>(current.transports);
                newPath.add(nextTransport);
                
                queue.add(new JourneyPath(
                    nextTransport.getArrivalCity(),
                    nextTransport.getArrivalDateTime(),
                    newPath,
                    newVisited,
                    current.mode
                ));
            }
        }
        
        // No path found
        return new ArrayList<>();
    }

}
