package fr.univ.holitrip.service;

import fr.univ.holitrip.model.Coordinates;

public interface DistanceService {
    /**
     * Calculates the distance in kilometers between two GPS coordinates.
     *
     * @param coord1 coordinates of the first location
     * @param coord2 coordinates of the second location
     * @return the distance in kilometers
     */
    double calculateDistance(Coordinates coord1, Coordinates coord2);
}
