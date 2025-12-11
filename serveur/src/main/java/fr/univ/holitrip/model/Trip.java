package fr.univ.holitrip.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a trip (outbound or return journey).
 * Can contain multiple transports for connections.
 * 
 * Example: Bordeaux -> Rennes via Paris
 *   - Transport 1: Bordeaux -> Paris
 *   - Transport 2: Paris -> Rennes
 */

public class Trip {
    private List<Transport> transports;

    public Trip() {
        this.transports = new ArrayList<>();
    }

    public Trip(List<Transport> transports) {
        this.transports = transports;
    }

    public List<Transport> getTransports() {
        return transports;
    }

    public void setTransports(List<Transport> transports) {
        this.transports = transports;
    }

    /**
     * Calculates the total price of all transports in this trip.
     * 
     * @return total price in euros
     */
    public double getTotalPrice() {
        double totalPrice = 0.0;
        for (Transport transport : transports) {
            totalPrice += transport.getPrice();
        }
        return totalPrice;
    }

    /**
     * Checks if the trip is direct (single transport) or has connections.
     * 
     * @return true if direct (1 transport), false if connections (2+ transports)
     */
    public boolean isDirect() {
        return transports.size() == 1;
    }

    /**
     * Calculates the total duration of the trip in minutes.
     * Duration = time from first departure to last arrival.
     * 
     * @return duration in minutes, or 0 if no transports
     */
    public long getTotalDuration() {
        if (transports.isEmpty()) {
            return 0;
        }
        
        LocalDateTime firstDeparture = transports.get(0).getDepartureDateTime();
        LocalDateTime lastArrival = transports.get(transports.size() - 1).getArrivalDateTime();
        
        return Duration.between(firstDeparture, lastArrival).toMinutes();
    }

    @Override
    public String toString() {
        return "Trip{" +
                "transports=" + transports.size() +
                ", direct=" + isDirect() +
                ", totalPrice=" + getTotalPrice() + "€" +
                ", duration=" + getTotalDuration() + "min" +
                '}';
    }
}
