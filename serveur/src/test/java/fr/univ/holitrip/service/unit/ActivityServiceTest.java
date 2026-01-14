package fr.univ.holitrip.service.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.univ.holitrip.model.Activity;
import fr.univ.holitrip.model.Coordinates;
import fr.univ.holitrip.service.impl.JsonActivityService;

class ActivityServiceTest {
    
    @Mock
    private fr.univ.holitrip.service.GeocodingService geocodingService;
    
    @Mock
    private fr.univ.holitrip.service.DistanceService distanceService;
    
    private fr.univ.holitrip.service.ActivityService activityService;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
        
        // Mock geocoding responses for Paris addresses
        when(geocodingService.geocode(contains("Rivoli")))
            .thenReturn(new Coordinates(48.8606, 2.3376)); // Louvre
        when(geocodingService.geocode(contains("Champ de Mars")))
            .thenReturn(new Coordinates(48.8584, 2.2945)); // Eiffel
        
        // Mock distance calculations
        when(distanceService.calculateDistance(any(), any()))
            .thenReturn(2.0); // Default 2km
        
        // Create service with injected dependencies
        activityService = new JsonActivityService(
            "data/activities.json",
            geocodingService,
            distanceService
        );
    }

    @Test
    void testFindActivities_Paris_CultureCategory_ShouldReturnActivities() {
        // ARRANGE
        String city = "Paris";
        List<String> categories = Arrays.asList("CULTURE");
        LocalDate date = LocalDate.of(2025, 1, 16);
        double maxPrice = 100.0;
        Coordinates hotelLocation = new Coordinates(48.8566, 2.3522); // Paris center
        double maxDistance = 10.0; // 10km radius
        
        // ACT
        List<Activity> activities = activityService.findActivities(
            city, categories, date, maxPrice, hotelLocation, maxDistance
        );
        
        // ASSERT
        assertNotNull(activities);
        assertFalse(activities.isEmpty());
        for (Activity activity : activities) {
            assertEquals("Paris", activity.getCity());
            assertEquals("CULTURE", activity.getCategory());
            assertTrue(activity.getPrice() <= maxPrice);
        }
    }

    @Test
    void testFindActivities_InvalidCity_ShouldReturnEmpty() {
        // ARRANGE
        String city = "InvalidCity";
        List<String> categories = Arrays.asList("CULTURE");
        LocalDate date = LocalDate.of(2025, 1, 16);
        double maxPrice = 100.0;
        Coordinates hotelLocation = new Coordinates(48.8566, 2.3522);
        double maxDistance = 10.0;
        
        // ACT
        List<Activity> activities = activityService.findActivities(
            city, categories, date, maxPrice, hotelLocation, maxDistance
        );
        
        // ASSERT
        assertNotNull(activities);
        assertTrue(activities.isEmpty());
    }

    @Test
    void testFindActivities_FreeActivities_ShouldWork() {
        // ARRANGE
        String city = "Lyon";
        List<String> categories = Arrays.asList("SPORT");
        LocalDate date = LocalDate.of(2025, 1, 16);
        double maxPrice = 0.0; // Free activities only
        Coordinates hotelLocation = new Coordinates(45.7640, 4.8357);
        double maxDistance = 10.0;
        
        // ACT
        List<Activity> activities = activityService.findActivities(
            city, categories, date, maxPrice, hotelLocation, maxDistance
        );
        
        // ASSERT
        assertNotNull(activities);
        assertFalse(activities.isEmpty());
        for (Activity activity : activities) {
            assertEquals(0.0, activity.getPrice());
        }
    }
}
