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
import java.time.LocalDateTime;
import fr.univ.holitrip.model.Coordinates;

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

    // Helper method to create transports easily
    private Transport t(String depCity, String arrCity, LocalDateTime dep, LocalDateTime arr, String mode, double price) {
        Transport tr = new Transport();
        tr.setDepartureCity(depCity);
        tr.setArrivalCity(arrCity);
        tr.setDepartureDateTime(dep);
        tr.setArrivalDateTime(arr);
        tr.setMode(mode);
        tr.setPrice(price);
        return tr;
    }

    @Test
    void outboundTrainReturnPlaneAllowedWhenPreferenceProvidedForOutboundOnly() {
        // outbound available TRAIN, return only PLANE; ensure package still built
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport outbound = t("X","Y", d, d.plusHours(2), "TRAIN", 80.0);
        when(transportService.findTransports(eq("City1"), eq("City2"), any(), any())).thenReturn(Collections.singletonList(outbound));
        // return transports only plane
        Transport ret = t("City2","City1", d.plusDays(3).withHour(18), d.plusDays(3).withHour(20), "PLANE", 100.0);
        when(transportService.findTransports(eq("City2"), eq("City1"), any(), any())).thenReturn(Collections.singletonList(ret));
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(20.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        List<Package> res = packageService.findPackages("City1","City2","2025-01-15",3,1000.0,"TRAIN","PRICE",3,"PRICE",Collections.emptyList(),10.0);
        assertFalse(res.isEmpty());
        // return trip should exist and use plane
        assertNotNull(res.get(0).getReturnTrip());
        assertTrue(res.get(0).getReturnTrip().getTransports().stream().anyMatch(tr -> "PLANE".equalsIgnoreCase(tr.getMode())));
    }

    @Test
    void modeRequestedButNoMatchingTransportYieldsEmpty() {
        // transports exist but only PLANE and user asked TRAIN
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport onlyPlane = t("S","T", d, d.plusHours(2), "PLANE", 100.0);
        when(transportService.findTransports(anyString(), anyString(), any(), any())).thenReturn(Collections.singletonList(onlyPlane));
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(20.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        List<Package> res = packageService.findPackages("S","T","2025-01-15",3,500.0,"TRAIN","PRICE",3,"PRICE",Collections.emptyList(),10.0);
        assertTrue(res.isEmpty());
    }

    @Test
    void tieBreakingPriceEqualsUsesDuration() {
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        // both price 100, one duration 1h, other 3h -> PRICE priority should pick shorter duration
        Transport tShort = t("U","V", d, d.plusHours(1), "TRAIN", 100.0);
        Transport tLong = t("U","V", d, d.plusHours(3), "TRAIN", 100.0);
        when(transportService.findTransports(eq("U"), eq("V"), any(), any())).thenReturn(Arrays.asList(tShort, tLong));
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(20.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        List<Package> res = packageService.findPackages("U","V","2025-01-15",3,500.0,null,"PRICE",3,"PRICE",Collections.emptyList(),10.0);
        assertFalse(res.isEmpty());
        boolean usedShort = res.get(0).getOutboundTrip().getTransports().stream().anyMatch(tr -> tr.getArrivalDateTime().equals(tShort.getArrivalDateTime()));
        assertTrue(usedShort);
    }

    @Test
    void malformedDepartureDateHandledWithoutException() {
        Transport direct = t("B","C", null, null, "TRAIN", 80.0);
        when(transportService.findTransports(anyString(), anyString(), any(), any())).thenReturn(Collections.singletonList(direct));
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(20.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        // provide malformed date
        List<Package> res = packageService.findPackages("B","C","not-a-date",3,500.0,null,"PRICE",3,"PRICE",Collections.emptyList(),10.0);
        assertNotNull(res);
    }

    @Test
    void testMinHotelRatingBoundaryZero() {
        // Tests ConditionalsBoundary mutant at line 228: minHotelRating > 0 vs >= 0
        // When minHotelRating=0, all hotels should be included (no filtering)
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport outbound = t("A","B", d, d.plusHours(2), "TRAIN", 50.0);
        Transport returnTr = t("B","A", d.plusDays(3), d.plusDays(3).plusHours(2), "TRAIN", 50.0);
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Collections.singletonList(outbound));
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Collections.singletonList(returnTr));
        
        Hotel h1 = new Hotel(); h1.setRating(0); h1.setPricePerNight(30.0);
        Hotel h2 = new Hotel(); h2.setRating(3); h2.setPricePerNight(40.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Arrays.asList(h1, h2));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",0,"PRICE",Collections.emptyList(),10.0);
        assertFalse(res.isEmpty());
        // With minHotelRating=0, both hotels should be considered (no filtering applied)
        assertTrue(res.stream().anyMatch(p -> p.getHotel() != null && p.getHotel().getRating() >= 0));
    }

    @Test
    void testMinHotelRatingBoundaryOne() {
        // Tests ConditionalsBoundary mutant at line 228: minHotelRating > 0 vs >= 0
        // When minHotelRating=1, hotels with rating=0 should be excluded
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport outbound = t("A","B", d, d.plusHours(2), "TRAIN", 50.0);
        Transport returnTr = t("B","A", d.plusDays(3), d.plusDays(3).plusHours(2), "TRAIN", 50.0);
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Collections.singletonList(outbound));
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Collections.singletonList(returnTr));
        
        Hotel h1 = new Hotel(); h1.setRating(0); h1.setPricePerNight(30.0);
        Hotel h2 = new Hotel(); h2.setRating(3); h2.setPricePerNight(40.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Arrays.asList(h1, h2));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",1,"PRICE",Collections.emptyList(),10.0);
        assertFalse(res.isEmpty());
        // With minHotelRating=1, hotel with rating=0 should be filtered out
        assertTrue(res.stream().allMatch(p -> p.getHotel() == null || p.getHotel().getRating() >= 1));
    }

    @Test
    void testHotelRatingExactMatch() {
        // Tests ConditionalsBoundary mutant at line 232: rating >= minRating vs > minRating
        // Hotel with rating=3 and minRating=3 should be included (>= 3)
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport outbound = t("A","B", d, d.plusHours(2), "TRAIN", 50.0);
        Transport returnTr = t("B","A", d.plusDays(3), d.plusDays(3).plusHours(2), "TRAIN", 50.0);
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Collections.singletonList(outbound));
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Collections.singletonList(returnTr));
        
        Hotel h1 = new Hotel(); h1.setRating(3); h1.setPricePerNight(40.0);
        Hotel h2 = new Hotel(); h2.setRating(2); h2.setPricePerNight(30.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Arrays.asList(h1, h2));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",3,"PRICE",Collections.emptyList(),10.0);
        assertFalse(res.isEmpty());
        // Hotel with exact match rating=3 should be included (>= not >)
        assertTrue(res.stream().anyMatch(p -> p.getHotel() != null && p.getHotel().getRating() == 3));
        // Hotel with rating=2 should be excluded
        assertTrue(res.stream().noneMatch(p -> p.getHotel() != null && p.getHotel().getRating() == 2));
    }

    @Test
    void testActivityBudgetExactMatch() {
        // Tests ConditionalsBoundary mutant at line 346: sum + price <= budget vs < budget
        // When activity price exactly matches remaining budget, it should be included
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport outbound = t("A","B", d, d.plusHours(2), "TRAIN", 50.0);
        Transport returnTr = t("B","A", d.plusDays(3), d.plusDays(3).plusHours(2), "TRAIN", 50.0);
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Collections.singletonList(outbound));
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Collections.singletonList(returnTr));
        
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(100.0); // 3 nights = 300
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        
        // Total budget: 500, transport: 50 outbound + 50 return = 100, hotel: 300, remaining for activities: 100
        Activity a1 = mock(Activity.class);
        when(a1.getPrice()).thenReturn(100.0); // Exactly matches remaining budget
        when(a1.getDate()).thenReturn(LocalDate.of(2025, 1, 16));
        
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.singletonList(a1));

        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",3,"PRICE",Collections.emptyList(),10.0);
        assertFalse(res.isEmpty());
        // Activity with price exactly matching remaining budget (100.0) should be included (<= not <)
        assertTrue(res.stream().anyMatch(p -> 
            p.getActivities().stream().anyMatch(act -> act.getPrice() == 100.0)
        ), "Activity with price=100 should be included when budget is exactly 100");
    }

}
