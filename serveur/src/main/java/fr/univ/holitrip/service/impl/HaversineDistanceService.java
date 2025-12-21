package fr.univ.holitrip.service.impl;

import fr.univ.holitrip.model.Coordinates;
import fr.univ.holitrip.service.DistanceService;

public class HaversineDistanceService implements DistanceService {
    private static final double EARTH_RADIUS_KM = 6371.0;

    @Override
    public double calculateDistance(Coordinates c1, Coordinates c2) {
        double lat1Rad = Math.toRadians(c1.getLatitude());
        double lat2Rad = Math.toRadians(c2.getLatitude());
        double deltaLatRad = Math.toRadians(c2.getLatitude() - c1.getLatitude());
        double deltaLonRad = Math.toRadians(c2.getLongitude() - c1.getLongitude());

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
        
    }
} 
