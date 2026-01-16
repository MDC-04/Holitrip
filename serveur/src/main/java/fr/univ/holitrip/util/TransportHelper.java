package fr.univ.holitrip.util;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import fr.univ.holitrip.model.Transport;

/**
 * Utility class providing helper methods for transport selection and validation.
 */
public class TransportHelper {

    private TransportHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Calculate the duration of a transport in minutes.
     * Returns Long.MAX_VALUE if dates are missing or invalid.
     */
    public static long transportDurationMinutes(Transport t) {
        if (t == null || t.getDepartureDateTime() == null || t.getArrivalDateTime() == null) {
            return Long.MAX_VALUE;
        }
        try {
            return java.time.Duration.between(t.getDepartureDateTime(), t.getArrivalDateTime()).toMinutes();
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    /**
     * Check if a list of transports represents a multi-leg journey.
     * A multi-leg journey has at least 2 transports where each arrival city
     * matches the next departure city.
     */
    public static boolean isMultiLeg(List<Transport> transports) {
        if (transports == null || transports.size() < 2) {
            return false;
        }
        
        // Verify continuity: each transport's arrival city = next transport's departure city
        for (int i = 0; i < transports.size() - 1; i++) {
            Transport current = transports.get(i);
            Transport next = transports.get(i + 1);
            if (current == null || next == null) {
                return false;
            }
            if (current.getArrivalCity() == null || !current.getArrivalCity().equals(next.getDepartureCity())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validate that all transports in a list match the preferred transport mode.
     * Returns true if no preferred mode is specified or if mode info is unavailable.
     */
    public static boolean validateTransportMode(List<Transport> transports, String preferredMode) {
        if (transports == null || transports.isEmpty()) {
            return false;
        }
        if (preferredMode == null || preferredMode.isBlank()) {
            return true;
        }
        
        // Check if any transport has mode information
        boolean anyWithMode = transports.stream()
                .anyMatch(t -> t != null && t.getMode() != null && !t.getMode().isBlank());
        if (!anyWithMode) {
            return true; // No mode info available -> permissive
        }
        
        // All transports must match preferred mode
        return transports.stream()
                .allMatch(t -> t != null && t.getMode() != null && preferredMode.equalsIgnoreCase(t.getMode()));
    }

    /**
     * Select the best transport from a list based on mode filter and priority.
     * 
     * @param list List of available transports
     * @param preferredMode Transport mode to filter by (e.g., "TRAIN", "PLANE")
     * @param priority Selection priority: "PRICE" for cheapest, "DURATION"/"TIME" for fastest
     * @return Best matching transport, or null if none found
     */
    public static Transport selectBestTransport(List<Transport> list, String preferredMode, String priority) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        
        // If preferredMode provided, prefer transports with that mode
        List<Transport> candidates = list;
        if (preferredMode != null && !preferredMode.isBlank()) {
            List<Transport> filtered = list.stream()
                    .filter(t -> t != null && preferredMode.equalsIgnoreCase(t.getMode()))
                    .collect(Collectors.toList());
            boolean anyWithMode = list.stream()
                    .anyMatch(t -> t != null && t.getMode() != null && !t.getMode().isBlank());
            if (filtered.isEmpty()) {
                if (anyWithMode) {
                    return null; // Mode info exists but none matched -> enforce strictness
                } else {
                    candidates = list; // No mode info available -> don't enforce strictness
                }
            } else {
                candidates = filtered;
            }
        }

        if ("PRICE".equalsIgnoreCase(priority)) {
            return candidates.stream()
                    .min(Comparator.comparingDouble(Transport::getPrice)
                            .thenComparingLong(TransportHelper::transportDurationMinutes))
                    .orElse(candidates.get(0));
        } else if ("DURATION".equalsIgnoreCase(priority) || "TIME".equalsIgnoreCase(priority)) {
            return candidates.stream()
                    .min(Comparator.comparingLong(TransportHelper::transportDurationMinutes)
                            .thenComparingDouble(Transport::getPrice))
                    .orElse(candidates.get(0));
        }
        return candidates.get(0);
    }
}
