package fr.univ.holitrip.service.unit;

import fr.univ.holitrip.model.Package;
import fr.univ.holitrip.model.Hotel;
import fr.univ.holitrip.model.Activity;
import fr.univ.holitrip.model.Transport;
import fr.univ.holitrip.service.impl.PackageBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

class PackageServiceTest {
    @Mock
    private fr.univ.holitrip.service.TransportService transportService;
    @Mock
    private fr.univ.holitrip.service.HotelService hotelService;
    @Mock
    private fr.univ.holitrip.service.ActivityService activityService;
    @Mock
    private fr.univ.holitrip.service.GeocodingService geocodingService;
    @Mock
    private fr.univ.holitrip.service.DistanceService distanceService;

    private fr.univ.holitrip.service.PackageService packageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        packageService = new PackageBuilder(transportService, hotelService, activityService,
                            distanceService, geocodingService);
    }

    @Test
    void shouldReturnEmptyListIfNoTransportsFound() {
        when(transportService.findTransports(anyString(), anyString(), any(), any())).thenReturn(Collections.emptyList());
        Hotel hotel = new Hotel();
        hotel.setRating(4);
        hotel.setPricePerNight(50.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(hotel));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.singletonList(new Activity()));
        List<Package> result = packageService.findPackages(
            "Bordeaux", "Paris", "2025-01-15", 3, 1000.0,
            "TRAIN", "PRICE", 3, "PRICE",
            Collections.singletonList("CULTURE"), 10.0
        );
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnPackagesMatchingAllCriteria() {
        // Arrange: all services return valid data
        Transport transport = new Transport();
        Hotel hotel = new Hotel();
        hotel.setRating(4);
        hotel.setPricePerNight(50.0);
        Activity activity = new Activity();
        when(transportService.findTransports(anyString(), anyString(), any(), any())).thenReturn(Collections.singletonList(transport));
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(hotel));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.singletonList(activity));
        List<Package> result = packageService.findPackages(
            "Bordeaux", "Paris", "2025-01-15", 3, 1000.0,
            "TRAIN", "PRICE", 3, "PRICE",
            Collections.singletonList("CULTURE"), 10.0
        );
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(p -> p.getHotel() != null && !p.getActivities().isEmpty()));
    }

    @Test
    void shouldRespectMaxBudget() {
        // Arrange: hotel price is too high for the budget
        Hotel hotel = new Hotel();
        hotel.setPricePerNight(1000.0);
        hotel.setRating(5);
        when(transportService.findTransports(anyString(), anyString(), any(), any())).thenReturn(Collections.singletonList(mock(Transport.class)));
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(hotel));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());
        List<Package> result = packageService.findPackages(
            "Bordeaux", "Paris", "2025-01-15", 3, 500.0,
            "TRAIN", "PRICE", 3, "PRICE",
            Collections.singletonList("CULTURE"), 10.0
        );
        assertTrue(result.stream().allMatch(p -> p.getTotalPrice(3) <= 500.0));
    }

    @Test
    void shouldReturnPackageWithErrorsIfCriteriaNotMet() {
        // Arrange: all services return empty lists (no data found)
        when(transportService.findTransports(anyString(), anyString(), any(), any())).thenReturn(Collections.emptyList());
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.emptyList());
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());
        List<Package> result = packageService.findPackages(
            "Bordeaux", "Paris", "2025-01-15", 3, 1000.0,
            "TRAIN", "PRICE", 3, "PRICE",
            Collections.singletonList("CULTURE"), 10.0
        );
        assertFalse(result.isEmpty());
        assertTrue(result.get(0).getErrors().size() > 0);
    }

    @Test
    void shouldNotAllowTwoActivitiesSameDate() {
        // Arrange: two activities with the same date
        Activity a1 = mock(Activity.class);
        Activity a2 = mock(Activity.class);
        when(a1.getDate()).thenReturn(LocalDate.of(2025, 1, 15));
        when(a2.getDate()).thenReturn(LocalDate.of(2025, 1, 15));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Arrays.asList(a1, a2));
        when(transportService.findTransports(anyString(), anyString(), any(), any())).thenReturn(Collections.singletonList(mock(Transport.class)));
        Hotel h = new Hotel();
        h.setRating(4);
        h.setPricePerNight(50.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        List<Package> result = packageService.findPackages(
            "Bordeaux", "Paris", "2025-01-15", 3, 1000.0,
            "TRAIN", "PRICE", 3, "PRICE",
            Collections.singletonList("CULTURE"), 10.0
        );
        assertTrue(result.stream().allMatch(p -> p.getActivities().stream().map(Activity::getDate).distinct().count() == p.getActivities().size()));
    }

    @Test
    void shouldHandleCorrespondancesAndPriorities() {
        // Arrange: several transports with different prices/durations
        Transport t1 = mock(Transport.class);
        Transport t2 = mock(Transport.class);
        when(t1.getPrice()).thenReturn(100.0);
        when(t2.getPrice()).thenReturn(80.0);
        when(transportService.findTransports(anyString(), anyString(), any(), any())).thenReturn(Arrays.asList(t1, t2));
        Hotel h2 = new Hotel();
        h2.setRating(4);
        h2.setPricePerNight(50.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h2));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());
        List<Package> result = packageService.findPackages(
            "Bordeaux", "Paris", "2025-01-15", 3, 1000.0,
            "TRAIN", "PRICE", 3, "PRICE",
            Collections.singletonList("CULTURE"), 10.0
        );
        // Check that the cheapest transport is chosen if priority is PRICE
        // We expect packages to contain transports; verify at least one package uses the cheaper transport (t2)
        assertFalse(result.isEmpty());
        boolean foundCheapest = result.stream().anyMatch(pkg -> {
            try {
                return pkg.getOutboundTrip() != null
                    && pkg.getOutboundTrip().getTransports().stream().anyMatch(tr -> tr.getPrice() == 80.0);
            } catch (Exception e) {
                return false;
            }
        });
        assertTrue(foundCheapest, "No package uses the cheapest transport when priority is PRICE");
    }

    @Test
    void shouldFilterActivitiesByDistance() {
        // Arrange: activities at different distances
        Activity a1 = mock(Activity.class);
        Activity a2 = mock(Activity.class);
        when(distanceService.calculateDistance(any(), any())).thenReturn(5.0, 20.0); // a1 at 5km, a2 at 20km
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Arrays.asList(a1, a2));
        when(transportService.findTransports(anyString(), anyString(), any(), any())).thenReturn(Collections.singletonList(mock(Transport.class)));
        Hotel h3 = new Hotel();
        h3.setRating(4);
        h3.setPricePerNight(50.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h3));
        List<Package> result = packageService.findPackages(
            "Bordeaux", "Paris", "2025-01-15", 3, 1000.0,
            "TRAIN", "PRICE", 3, "PRICE",
            Collections.singletonList("CULTURE"), 10.0
        );
        // Assert: only activities within 10km are included
        assertTrue(result.stream().allMatch(p ->
            p.getActivities().stream().allMatch(act -> act == a1)
        ));
    }

    @Test
    void shouldMaximizeNumberOfActivitiesWithinBudget() {
        // Arrange: several activities, some over budget
        Activity a1 = mock(Activity.class);
        Activity a2 = mock(Activity.class);
        when(a1.getPrice()).thenReturn(10.0);
        when(a2.getPrice()).thenReturn(100.0);
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Arrays.asList(a1, a2));
        when(transportService.findTransports(anyString(), anyString(), any(), any())).thenReturn(Collections.singletonList(mock(Transport.class)));
        Hotel h4 = new Hotel();
        h4.setRating(4);
        h4.setPricePerNight(10.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h4));
        List<Package> result = packageService.findPackages(
            "Bordeaux", "Paris", "2025-01-15", 3, 50.0,
            "TRAIN", "PRICE", 3, "PRICE",
            Collections.singletonList("CULTURE"), 10.0
        );
        // Assert: total activities price per package does not exceed budget and expensive activity is excluded
        assertTrue(result.stream().allMatch(p ->
            p.getActivities().stream().mapToDouble(Activity::getPrice).sum() <= 50.0
            && p.getActivities().stream().noneMatch(act -> act == a2)
        ));
    }

    @Test
    void shouldReturnEmptyListIfNoHotelsFound() {
        // Arrange: no hotels found
        when(transportService.findTransports(anyString(), anyString(), any(), any())).thenReturn(Collections.singletonList(mock(Transport.class)));
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.emptyList());
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.singletonList(mock(Activity.class)));
        List<Package> result = packageService.findPackages(
            "Bordeaux", "Paris", "2025-01-15", 3, 1000.0,
            "TRAIN", "PRICE", 3, "PRICE",
            Collections.singletonList("CULTURE"), 10.0
        );
        assertTrue(result.isEmpty());
    }

}
