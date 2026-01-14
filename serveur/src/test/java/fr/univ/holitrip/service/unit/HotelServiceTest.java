package fr.univ.holitrip.service.unit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.univ.holitrip.model.Hotel;
import fr.univ.holitrip.service.impl.JsonHotelService;

class HotelServiceTest {
    private fr.univ.holitrip.service.HotelService hotelService;

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
            assertEquals(city, hotel.getCity());
            assertTrue(hotel.getRating() >= minRating);
            assertTrue(hotel.getPricePerNight() <= maxPricePerNight);
        }
    }

    @Test
    void testFindHotels_Paris_Rating_5_Stars_Only() {
        // ARRANGE
        String city = "Paris";
        int rating = 5;
        double maxPricePerNight = 1500.0;
        
        // ACT
        List<Hotel> hotels = hotelService.findHotels(city, rating, maxPricePerNight);
        
        // ASSERT
        assertNotNull(hotels);
        assertFalse(hotels.isEmpty());
        for (Hotel hotel : hotels) {
            assertEquals(city, hotel.getCity());
            assertEquals(hotel.getRating(), rating);
            assertTrue(hotel.getPricePerNight() <= maxPricePerNight);
        }
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
            assertEquals(city, hotel.getCity());
            assertTrue(hotel.getRating() >= minRating);
            assertTrue(hotel.getPricePerNight() <= maxPricePerNight);
        }
    }

    @Test
    void testFindHotels_InvalidCity_ShouldReturnEmpty() {
        // ARRANGE
        String city = "Rabat";
        int minRating = 1;
        double maxPricePerNight = 1000.0;
        
        // ACT
        List<Hotel> hotels = hotelService.findHotels(city, minRating, maxPricePerNight);
        
        // ASSERT
        assertNotNull(hotels);
        assertTrue(hotels.isEmpty());
    }

    @Test
    void testFindHotels_HighRating_NoAffordableHotels_ShouldReturnEmpty() {
        // ARRANGE
        String city = "Paris";
        int minRating = 5;
        double maxPricePerNight = 50.0;
        
        // ACT
        List<Hotel> hotels = hotelService.findHotels(city, minRating, maxPricePerNight);
        
        // ASSERT
        assertNotNull(hotels);
        assertTrue(hotels.isEmpty());
    }

    @Test
    void testFindHotels_EmptyCity_ShouldReturnEmpty() {
        // ARRANGE
        String city = "";
        int minRating = 1;
        double maxPricePerNight = 1000.0;
        
        // ACT
        List<Hotel> hotels = hotelService.findHotels(city, minRating, maxPricePerNight);
        
        // ASSERT
        assertNotNull(hotels);
        assertTrue(hotels.isEmpty());
    }

    @Test
    void testFindHotels_NegativeOrNullPrice_ShouldReturnEmpty() {
        // ARRANGE
        String city = "Paris";
        int minRating = 1;
        double negativePrice = -100.0;
        double nullPrice = 0.0;
        
        // ACT
        List<Hotel> hotelsNegativePrice = hotelService.findHotels(city, minRating, negativePrice);
        List<Hotel> hotelsNullPrice = hotelService.findHotels(city, minRating, nullPrice);
        
        // ASSERT
        assertNotNull(hotelsNegativePrice);
        assertNotNull(hotelsNullPrice);
        assertTrue(hotelsNegativePrice.isEmpty());
        assertTrue(hotelsNullPrice.isEmpty());
    }

    @Test 
    void testFindHotels_WrongRating() {
        //ARRANGE
        String city = "Bordeaux";
        int minRating = 7;
        double maxPricePerNight = 300.0;

        //ACT
        List<Hotel> hotels = hotelService.findHotels(city, minRating, maxPricePerNight);

        //ASSERT
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
