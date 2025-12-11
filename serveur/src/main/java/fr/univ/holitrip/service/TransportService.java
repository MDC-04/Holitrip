package fr.univ.holitrip.service;

import java.time.LocalDateTime;
import java.util.List;
import fr.univ.holitrip.model.Transport;

public interface TransportService {
    /**
     * Finds available transports matching the specified criteria.
     *
     * @param departureCity the departure city
     * @param arrivalCity the arrival city
     * @param departureDate the desired departure date and time
     * @param mode the transport mode ("TRAIN" or "PLANE"), or null for all modes
     * @return a list of transports matching the criteria
     */
    List<Transport> findTransports(String departureCity, String arrivalCity, LocalDateTime departureDate, String mode);
}
