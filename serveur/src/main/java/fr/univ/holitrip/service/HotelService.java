package fr.univ.holitrip.service;

import java.util.List;

import fr.univ.holitrip.model.Hotel;

public interface HotelService {
    /**
     * Finds available hotels in a city matching the specified criteria.
     *
     * @param city the city to search in
     * @param minRating the minimum rating (1-5 stars)
     * @param maxPricePerNight the maximum price per night
     * @return a list of hotels matching the criteria
     */
    List<Hotel> findHotels(String city, int minRating, double maxPricePerNight);
}
