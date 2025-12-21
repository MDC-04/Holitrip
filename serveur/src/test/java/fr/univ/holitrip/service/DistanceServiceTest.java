package fr.univ.holitrip.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import fr.univ.holitrip.model.Coordinates;
import fr.univ.holitrip.service.impl.HaversineDistanceService;

class DistanceServiceTest {

    @Test
    void testCalculateDistance_BordeauxParis_ShouldReturnApproximately500km() {
        // ARRANGE 
        Coordinates bordeaux = new Coordinates(44.8378, -0.5792);
        Coordinates paris = new Coordinates(48.8566, 2.3522);
        DistanceService service = new HaversineDistanceService();
        
        // ACT 
        double distance = service.calculateDistance(bordeaux, paris);
        
        // ASSERT 
        assertEquals(500, distance, 10); // ~500km ± 10km de tolérance
    }
    
    @Test
    void testCalculateDistance_SameLocation_ShouldReturnZero() {
        // ARRANGE
        Coordinates paris = new Coordinates(48.8566, 2.3522);
        DistanceService service = new HaversineDistanceService();
        
        // ACT
        double distance = service.calculateDistance(paris, paris);
        
        // ASSERT
        assertEquals(0, distance, 0.1);
    }

    // @Test
    // void testCalculateDistance_BordeauxToulouse_ShouldReturnApproximately245km() {
    //     //ARRANGE
    //     Coordinates bordeaux = new Coordinates(44.8378, -0.5792);
    //     Coordinates toulouse = new Coordinates(43.6047, 1.4442);
    //     DistanceService service = new HaversineDistanceService();

    //     //ACT
    //     double distance = service.calculateDistance(bordeaux, toulouse);

    //     //ASSERT
    //     assertEquals(245, distance, 10); // ~245km ± 10km
    // }
}