package fr.univ.holitrip.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.univ.holitrip.model.Hotel;
import fr.univ.holitrip.service.impl.JsonHotelService;

class HotelServiceTest {
    private HotelService hotelService;

    @BeforeEach
    void setUp() {
        hotelService = new JsonHotelService("data/hotels.json");
    }

    @Test
    void testFindHotels_Paris_MinRating3_ShouldReturnHotels() {
        // ARRANGE
        String city = "Paris";
        int minRating = 3;
        double maxPricePerNight = 500.0;
        
        // ACT
        List<Hotel> hotels = hotelService.findHotels(city, minRating, maxPricePerNight);
        
        // ASSERT
        assertNotNull(hotels);
        assertFalse(hotels.isEmpty());
        for (Hotel hotel : hotels) {
            assertEquals("Paris", hotel.getCity());
            assertTrue(hotel.getRating() >= 3);
            assertTrue(hotel.getPricePerNight() <= 500.0);
        }
    }

    @Test
    void testFindHotels_Paris_Rating5Only_ShouldReturn1() {
        // ARRANGE
        String city = "Paris";
        int minRating = 5;
        double maxPricePerNight = 1000.0;
        
        // ACT
        List<Hotel> hotels = hotelService.findHotels(city, minRating, maxPricePerNight);
        
        // ASSERT
        assertNotNull(hotels);
        assertEquals(1, hotels.size());
        assertEquals(5, hotels.get(0).getRating());
    }

    @Test
    void testFindHotels_Paris_LowBudget_ShouldReturnCheapHotels() {
        // ARRANGE
        String city = "Paris";
        int minRating = 1;
        double maxPricePerNight = 50.0;
        
        // ACT
        List<Hotel> hotels = hotelService.findHotels(city, minRating, maxPricePerNight);
        
        // ASSERT
        assertNotNull(hotels);
        assertFalse(hotels.isEmpty());
        for (Hotel hotel : hotels) {
            assertTrue(hotel.getPricePerNight() <= 50.0);
        }
    }

    @Test
    void testFindHotels_InvalidCity_ShouldReturnEmpty() {
        // ARRANGE
        String city = "InvalidCity";
        int minRating = 1;
        double maxPricePerNight = 1000.0;
        
        // ACT
        List<Hotel> hotels = hotelService.findHotels(city, minRating, maxPricePerNight);
        
        // ASSERT
        assertNotNull(hotels);
        assertTrue(hotels.isEmpty());
    }

    @Test
    void testFindHotels_CaseInsensitive_ShouldWork() {
        // ARRANGE
        String city = "paris"; 
        int minRating = 1;
        double maxPricePerNight = 1000.0;
        
        // ACT
        List<Hotel> hotels = hotelService.findHotels(city, minRating, maxPricePerNight);
        
        // ASSERT
        assertNotNull(hotels);
        assertFalse(hotels.isEmpty());
    }
}
