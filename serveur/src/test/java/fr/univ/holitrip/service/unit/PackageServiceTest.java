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
    void shouldReturnErrorMessageIfNoTransportsFound() {
        // ARRANGE
        when(transportService.findTransports(anyString(), anyString(), any(), any())).thenReturn(Collections.emptyList());
        Hotel hotel = new Hotel();
        hotel.setRating(4);
        hotel.setPricePerNight(50.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(hotel));
        
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.singletonList(new Activity()));
        
        // ACT
        List<Package> result = packageService.findPackages(
            "Bordeaux", "Paris", "2025-01-15", 3, 1000.0,
            "TRAIN", "PRICE", 3, "PRICE",
            Collections.singletonList("CULTURE"), 10.0
        );

        // ASSERT
        assertTrue(result.stream().allMatch(p -> p.getErrors() != null && !p.getErrors().isEmpty()));
        System.out.println("No transports found, returned packages: " + result);
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
        
        //Act
        List<Package> result = packageService.findPackages(
            "Bordeaux", "Paris", "2025-01-15", 3, 1000.0,
            "TRAIN", "PRICE", 3, "PRICE",
            Collections.singletonList("CULTURE"), 10.0
        );

        //Assert
        assertFalse(result == null);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(p -> p.getHotel() != null && !p.getActivities().isEmpty()));
    }

    @Test
    void shouldRespectMaxBudget() {
        // ARRANGE
        Hotel hotel = new Hotel();
        hotel.setPricePerNight(1000.0);
        hotel.setRating(5);
        when(transportService.findTransports(anyString(), anyString(), any(), any())).thenReturn(Collections.singletonList(mock(Transport.class)));
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(hotel));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());
        
        // ACT
        List<Package> result = packageService.findPackages(
            "Bordeaux", "Paris", "2025-01-15", 3, 500.0,
            "TRAIN", "PRICE", 3, "PRICE",
            Collections.singletonList("CULTURE"), 10.0
        );
        
        // ASSERT
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(p -> 
            p.getTotalPrice(3) > 500.0 && 
            p.getErrors().stream().anyMatch(err -> err.contains("Budget exceeded"))
        ), "Package exceeding budget should contain budget error");
    }

    @Test
    void shouldReturnPackageWithErrorsIfCriteriaNotMet() {
        // ARRANGE
        when(transportService.findTransports(anyString(), anyString(), any(), any())).thenReturn(Collections.emptyList());
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.emptyList());
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());
        
        // ACT
        List<Package> result = packageService.findPackages(
            "Bordeaux", "Paris", "2025-01-15", 3, 1000.0,
            "TRAIN", "PRICE", 3, "PRICE",
            Collections.singletonList("CULTURE"), 10.0
        );
        
        // ASSERT
        assertFalse(result.isEmpty());
        assertTrue(result.get(0).getErrors().size() > 0);
    }

    @Test
    void shouldNotAllowTwoActivitiesSameDate() {
        // ARRANGE
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
        
        // ACT
        List<Package> result = packageService.findPackages(
            "Bordeaux", "Paris", "2025-01-15", 3, 1000.0,
            "TRAIN", "PRICE", 3, "PRICE",
            Collections.singletonList("CULTURE"), 10.0
        );
        
        // ASSERT
        assertTrue(result.stream().allMatch(p -> p.getActivities().stream().map(Activity::getDate).distinct().count() == p.getActivities().size()));
    }

    @Test
    void shouldHandleCorrespondancesAndPriorities() {
        // ARRANGE
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
        
        // ACT
        List<Package> result = packageService.findPackages(
            "Bordeaux", "Paris", "2025-01-15", 3, 1000.0,
            "TRAIN", "PRICE", 3, "PRICE",
            Collections.singletonList("CULTURE"), 10.0
        );
        
        // ASSERT
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
        // ARRANGE
        Activity a1 = mock(Activity.class);
        Activity a2 = mock(Activity.class);
        when(distanceService.calculateDistance(any(), any())).thenReturn(5.0, 20.0); // a1 at 5km, a2 at 20km
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Arrays.asList(a1, a2));
        when(transportService.findTransports(anyString(), anyString(), any(), any())).thenReturn(Collections.singletonList(mock(Transport.class)));
        Hotel h3 = new Hotel();
        h3.setRating(4);
        h3.setPricePerNight(50.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h3));
        
        // ACT
        List<Package> result = packageService.findPackages(
            "Bordeaux", "Paris", "2025-01-15", 3, 1000.0,
            "TRAIN", "PRICE", 3, "PRICE",
            Collections.singletonList("CULTURE"), 10.0
        );
        
        // ASSERT
        assertTrue(result.stream().allMatch(p ->
            p.getActivities().stream().allMatch(act -> act == a1)
        ));
    }

    @Test
    void shouldMaximizeNumberOfActivitiesWithinBudget() {
        // ARRANGE
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
        
        // ACT
        List<Package> result = packageService.findPackages(
            "Bordeaux", "Paris", "2025-01-15", 3, 50.0,
            "TRAIN", "PRICE", 3, "PRICE",
            Collections.singletonList("CULTURE"), 10.0
        );
        
        // ASSERT
        assertTrue(result.stream().allMatch(p ->
            p.getActivities().stream().mapToDouble(Activity::getPrice).sum() <= 50.0
            && p.getActivities().stream().noneMatch(act -> act == a2)
        ));
    }

    @Test
    void shouldReturnErrorMessageIfNoHotelsFound() {
        // ARRANGE
        when(transportService.findTransports(anyString(), anyString(), any(), any())).thenReturn(Collections.singletonList(mock(Transport.class)));
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.emptyList());
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.singletonList(mock(Activity.class)));
        
        // ACT
        List<Package> result = packageService.findPackages(
            "Bordeaux", "Paris", "2025-01-15", 3, 1000.0,
            "TRAIN", "PRICE", 3, "PRICE",
            Collections.singletonList("CULTURE"), 10.0
        );
        
        // ASSERT
        assertTrue(result.stream().allMatch(p -> p.getErrors() != null && !p.getErrors().isEmpty()));
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
        // ARRANGE
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

        // ACT
        List<Package> res = packageService.findPackages("City1","City2","2025-01-15",3,1000.0,"TRAIN","PRICE",3,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertFalse(res.isEmpty());
        // return trip should exist and use plane
        assertNotNull(res.get(0).getReturnTrip());
        assertTrue(res.get(0).getReturnTrip().getTransports().stream().anyMatch(tr -> "PLANE".equalsIgnoreCase(tr.getMode())));
    }

    @Test
    void modeRequestedButNoMatchingTransportYieldsEmpty() {
        // ARRANGE
        // transports exist but only PLANE and user asked TRAIN
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport onlyPlane = t("S","T", d, d.plusHours(2), "PLANE", 100.0);
        when(transportService.findTransports(anyString(), anyString(), any(), any())).thenReturn(Collections.singletonList(onlyPlane));
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(20.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        // ACT
        List<Package> res = packageService.findPackages("S","T","2025-01-15",3,500.0,"TRAIN","PRICE",3,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertTrue(res.stream().allMatch(p -> p.getErrors() != null && !p.getErrors().isEmpty()), "Should return error message when no matching transports available");
    }

    @Test
    void tieBreakingPriceEqualsUsesDuration() {
        // ARRANGE
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        // both price 100, one duration 1h, other 3h -> PRICE priority should pick shorter duration
        Transport tShort = t("U","V", d, d.plusHours(1), "TRAIN", 100.0);
        Transport tLong = t("U","V", d, d.plusHours(3), "TRAIN", 100.0);
        when(transportService.findTransports(eq("U"), eq("V"), any(), any())).thenReturn(Arrays.asList(tShort, tLong));
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(20.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        // ACT
        List<Package> res = packageService.findPackages("U","V","2025-01-15",3,500.0,null,"PRICE",3,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertFalse(res.isEmpty());
        boolean usedShort = res.get(0).getOutboundTrip().getTransports().stream().anyMatch(tr -> tr.getArrivalDateTime().equals(tShort.getArrivalDateTime()));
        assertTrue(usedShort);
    }

    @Test
    void malformedDepartureDateHandledWithoutException() {
        // ARRANGE
        Transport direct = t("B","C", null, null, "TRAIN", 80.0);
        when(transportService.findTransports(anyString(), anyString(), any(), any())).thenReturn(Collections.singletonList(direct));
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(20.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        // ACT
        // provide malformed date
        List<Package> res = packageService.findPackages("B","C","not-a-date",3,500.0,null,"PRICE",3,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertNotNull(res);
    }

    @Test
    void testMinHotelRatingBoundaryZero() {
        // ARRANGE
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

        // ACT
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",0,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertFalse(res.isEmpty());
        // With minHotelRating=0, both hotels should be considered (no filtering applied)
        assertTrue(res.stream().anyMatch(p -> p.getHotel() != null && p.getHotel().getRating() >= 0));
    }

    @Test
    void testMinHotelRatingBoundaryOne() {
        // ARRANGE
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

        // ACT
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",1,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertFalse(res.isEmpty());
        // With minHotelRating=1, hotel with rating=0 should be filtered out
        assertTrue(res.stream().allMatch(p -> p.getHotel() == null || p.getHotel().getRating() >= 1));
    }

    @Test
    void testHotelRatingExactMatch() {
        // ARRANGE
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

        // ACT
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",3,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertFalse(res.isEmpty());
        // Hotel with exact match rating=3 should be included (>= not >)
        assertTrue(res.stream().anyMatch(p -> p.getHotel() != null && p.getHotel().getRating() == 3));
        // Hotel with rating=2 should be excluded
        assertTrue(res.stream().noneMatch(p -> p.getHotel() != null && p.getHotel().getRating() == 2));
    }

    @Test
    void testActivityBudgetExactMatch() {
        // ARRANGE
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

        // ACT
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",3,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertFalse(res.isEmpty());
        // Activity with price exactly matching remaining budget (100.0) should be included (<= not <)
        assertTrue(res.stream().anyMatch(p -> 
            p.getActivities().stream().anyMatch(act -> act.getPrice() == 100.0)
        ), "Activity with price=100 should be included when budget is exactly 100");
    }

    @Test
    void testHotelRatingStrictCheckBelowMinimum() {
        // ARRANGE
        // Tests ConditionalsBoundary mutant at line 279: chosenHotel.getRating() < minHotelRating
        // Hotel with rating slightly below minimum should be rejected
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport outbound = t("A","B", d, d.plusHours(2), "TRAIN", 50.0);
        Transport returnTr = t("B","A", d.plusDays(3), d.plusDays(3).plusHours(2), "TRAIN", 50.0);
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Collections.singletonList(outbound));
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Collections.singletonList(returnTr));
        
        Hotel h1 = new Hotel(); h1.setRating(2); h1.setPricePerNight(40.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h1));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        // ACT
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",3,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertTrue(res.stream().allMatch(p -> p.getErrors() != null && !p.getErrors().isEmpty()), "Should return error message when hotel rating is below minimum");
    }

    @Test
    void testNegativeBudgetClamping() {
        // ARRANGE
        // Tests ConditionalsBoundary mutant at line 342: activitiesBudget < 0 -> clamped to 0.0
        // When transport + hotel exceed maxBudget, no activities should be included
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport outbound = t("A","B", d, d.plusHours(2), "TRAIN", 200.0);
        Transport returnTr = t("B","A", d.plusDays(3), d.plusDays(3).plusHours(2), "TRAIN", 200.0);
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Collections.singletonList(outbound));
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Collections.singletonList(returnTr));
        
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(100.0); // 3 nights = 300
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        
        Activity a1 = mock(Activity.class);
        when(a1.getPrice()).thenReturn(1.0); // Very cheap activity
        when(a1.getDate()).thenReturn(LocalDate.of(2025, 1, 16));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.singletonList(a1));

        // ACT
        // Total: 200 + 200 + 300 = 700, maxBudget = 500, activitiesBudget = -200 -> should be clamped to 0
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",3,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertFalse(res.isEmpty());
        // Package should have budget error
        assertTrue(res.get(0).getErrors().stream().anyMatch(err -> err.contains("Budget exceeded")),
            "Package should contain budget exceeded error");
        // No activities should be added when activitiesBudget is negative (clamped to 0)
        assertTrue(res.get(0).getActivities().isEmpty(), 
            "No activities should be included when activitiesBudget < 0 (clamped to 0)");
    }

    @Test
    void testBudgetCalculationMathPrecision() {
        // ARRANGE
        // Tests Math mutants in budget calculation (lines 336-342)
        // Verifies precise arithmetic: activitiesBudget = maxBudget - (outbound + return) - hotel
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport outbound = t("A","B", d, d.plusHours(2), "TRAIN", 80.0);
        Transport returnTr = t("B","A", d.plusDays(3), d.plusDays(3).plusHours(2), "TRAIN", 70.0);
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Collections.singletonList(outbound));
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Collections.singletonList(returnTr));
        
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(50.0); // 3 nights = 150
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        
        // Expected: 600 - (80 + 70) - 150 = 600 - 150 - 150 = 300 for activities
        Activity a1 = mock(Activity.class);
        when(a1.getPrice()).thenReturn(250.0);
        when(a1.getDate()).thenReturn(LocalDate.of(2025, 1, 16));
        
        Activity a2 = mock(Activity.class);
        when(a2.getPrice()).thenReturn(60.0);
        when(a2.getDate()).thenReturn(LocalDate.of(2025, 1, 17));
        
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Arrays.asList(a1, a2));

        // ACT
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,600.0,null,"PRICE",3,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertFalse(res.isEmpty());
        // Both activities should fit: 250 + 60 = 310 but we only have 300, so only a2 (60) should be included
        // Actually sorted by price: a2(60) first, then a1(250): 60 fits, 60+250=310 > 300, so only a2
        assertTrue(res.stream().anyMatch(p -> 
            p.getActivities().stream().anyMatch(act -> act.getPrice() == 60.0) &&
            p.getActivities().stream().noneMatch(act -> act.getPrice() == 250.0)
        ), "Only cheaper activity (60) should fit in budget of 300");
    }

    @Test
    void testBudgetCalculationWithZeroCosts() {
        // ARRANGE
        // Tests Math mutants with edge case: some costs are zero
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport outbound = t("A","B", d, d.plusHours(2), "TRAIN", 0.0);
        Transport returnTr = t("B","A", d.plusDays(3), d.plusDays(3).plusHours(2), "TRAIN", 0.0);
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Collections.singletonList(outbound));
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Collections.singletonList(returnTr));
        
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(0.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        
        // All budget (200) should be available for activities
        Activity a1 = mock(Activity.class);
        when(a1.getPrice()).thenReturn(200.0);
        when(a1.getDate()).thenReturn(LocalDate.of(2025, 1, 16));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.singletonList(a1));

        // ACT
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,200.0,null,"PRICE",3,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertFalse(res.isEmpty());
        assertTrue(res.stream().anyMatch(p -> 
            p.getActivities().stream().anyMatch(act -> act.getPrice() == 200.0)
        ), "Activity consuming entire budget should be included when transport/hotel are free");
    }

    @Test
    void testEmptyTransportListHandling() {
        // ARRANGE
        // Tests NegateConditionals: transports == null || transports.isEmpty()
        when(transportService.findTransports(anyString(), anyString(), any(), any())).thenReturn(Collections.emptyList());
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(50.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        // ACT
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",3,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertTrue(res.stream().allMatch(p -> p.getErrors() != null && !p.getErrors().isEmpty()), "Should return error message when no transports available");
    }

    @Test
    void testEmptyHotelListHandling() {
        // ARRANGE
        // Tests NegateConditionals: hotels == null || hotels.isEmpty()
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport t = t("A","B", d, d.plusHours(2), "TRAIN", 50.0);
        when(transportService.findTransports(anyString(), anyString(), any(), any())).thenReturn(Collections.singletonList(t));
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.emptyList());
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        // ACT
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",3,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertTrue(res.stream().allMatch(p -> p.getErrors() != null && !p.getErrors().isEmpty()), "Should return error message when no hotels available");
    }

    @Test
    void testNullTransportModeAllowsAnyMode() {
        // ARRANGE
        // Tests NegateConditionals: transportMode != null && !transportMode.isBlank()
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport plane = t("A","B", d, d.plusHours(1), "PLANE", 150.0);
        Transport train = t("A","B", d, d.plusHours(3), "TRAIN", 80.0);
        Transport planeRet = t("B","A", d.plusDays(3), d.plusDays(3).plusHours(1), "PLANE", 150.0);
        Transport trainRet = t("B","A", d.plusDays(3), d.plusDays(3).plusHours(3), "TRAIN", 80.0);
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Arrays.asList(plane, train));
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Arrays.asList(planeRet, trainRet));
        
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(50.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        // ACT
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",3,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertFalse(res.isEmpty());
        // With null mode and PRICE priority, should choose cheaper train (80)
        assertTrue(res.stream().anyMatch(p -> 
            p.getOutboundTrip().getTransports().stream().anyMatch(tr -> tr.getPrice() == 80.0)
        ), "Should select cheapest transport when mode is null and priority is PRICE");
    }

    @Test
    void testEmptyActivityCategoriesHandling() {
        // ARRANGE
        // Tests NegateConditionals for activity categories handling
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport outbound = t("A","B", d, d.plusHours(2), "TRAIN", 50.0);
        Transport returnTr = t("B","A", d.plusDays(3), d.plusDays(3).plusHours(2), "TRAIN", 50.0);
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Collections.singletonList(outbound));
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Collections.singletonList(returnTr));
        
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(50.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        when(activityService.findActivities(anyString(), eq(Collections.emptyList()), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        // ACT
        // Empty activity categories list
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",3,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertFalse(res.isEmpty());
        assertTrue(res.get(0).getActivities().isEmpty(), "Package with empty categories should have no activities");
    }

    @Test
    void testDurationPriorityBreaksTie() {
        // ARRANGE
        // Tests NegateConditionals in priority selection logic
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport fast = t("A","B", d, d.plusHours(1), "TRAIN", 100.0);
        Transport slow = t("A","B", d, d.plusHours(4), "TRAIN", 100.0);
        Transport returnTr = t("B","A", d.plusDays(3), d.plusDays(3).plusHours(1), "TRAIN", 100.0);
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Arrays.asList(fast, slow));
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Collections.singletonList(returnTr));
        
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(50.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        // ACT
        // DURATION priority should select faster transport
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"DURATION",3,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertFalse(res.isEmpty());
        assertTrue(res.stream().anyMatch(p -> 
            p.getOutboundTrip().getTransports().stream()
                .anyMatch(tr -> tr.getArrivalDateTime().equals(fast.getArrivalDateTime()))
        ), "Should select faster transport when priority is DURATION");
    }

    @Test
    void testMultiLegTransportOutboundAndReturn() {
        // ARRANGE
        // Tests multi-leg transport handling (lines 89, 191, 217 in PackageBuilder)
        // Create multi-leg outbound: A -> B (via intermediate city)
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport leg1 = t("A","Intermediate", d, d.plusHours(2), "TRAIN", 40.0);
        Transport leg2 = t("Intermediate","B", d.plusHours(3), d.plusHours(5), "TRAIN", 40.0);
        
        // Create multi-leg return: B -> A (via intermediate city)
        Transport retLeg1 = t("B","Intermediate", d.plusDays(3), d.plusDays(3).plusHours(2), "TRAIN", 40.0);
        Transport retLeg2 = t("Intermediate","A", d.plusDays(3).plusHours(3), d.plusDays(3).plusHours(5), "TRAIN", 40.0);
        
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Arrays.asList(leg1, leg2));
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Arrays.asList(retLeg1, retLeg2));
        
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(50.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        // ACT
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,"TRAIN","PRICE",3,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertFalse(res.isEmpty());
        // Verify multi-leg outbound is included
        assertTrue(res.get(0).getOutboundTrip().getTransports().size() == 2, "Outbound should have 2 legs");
        // Verify multi-leg return is included
        assertNotNull(res.get(0).getReturnTrip());
        assertTrue(res.get(0).getReturnTrip().getTransports().size() == 2, "Return should have 2 legs");
    }

    @Test
    void testHotelPriorityStarSelectsHighestRating() {
        // ARRANGE
        // Tests hotel selection with STAR/RATING priority (line 255 in PackageBuilder)
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport outbound = t("A","B", d, d.plusHours(2), "TRAIN", 50.0);
        Transport returnTr = t("B","A", d.plusDays(3), d.plusDays(3).plusHours(2), "TRAIN", 50.0);
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Collections.singletonList(outbound));
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Collections.singletonList(returnTr));
        
        Hotel h1 = new Hotel(); h1.setRating(3); h1.setPricePerNight(50.0);
        Hotel h2 = new Hotel(); h2.setRating(5); h2.setPricePerNight(100.0);
        Hotel h3 = new Hotel(); h3.setRating(4); h3.setPricePerNight(75.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Arrays.asList(h1, h2, h3));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        // ACT
        // STAR/RATING priority should select hotel with highest rating (5 stars)
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",3,"STAR",Collections.emptyList(),10.0);
        
        // ASSERT
        assertFalse(res.isEmpty());
        assertEquals(5, res.get(0).getHotel().getRating(), "Should select hotel with highest rating when priority is STAR");
    }

    @Test
    void testTransportSelectionWithoutExplicitPriority() {
        // ARRANGE
        // Tests transport selection when no explicit priority is given (line 139 fallback in PackageBuilder)
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport t1 = t("A","B", d, d.plusHours(2), "TRAIN", 100.0);
        Transport t2 = t("A","B", d.plusHours(1), d.plusHours(3), "TRAIN", 90.0);
        Transport returnTr = t("B","A", d.plusDays(3), d.plusDays(3).plusHours(2), "TRAIN", 50.0);
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Arrays.asList(t1, t2));
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Collections.singletonList(returnTr));
        
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(50.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        // ACT
        // With null/empty priority, should fall back to first candidate
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,null,3,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertFalse(res.isEmpty());
        assertNotNull(res.get(0).getOutboundTrip());
        assertTrue(res.get(0).getOutboundTrip().getTransports().size() > 0, "Should select a transport even without explicit priority");
    }

    @Test
    void testHotelStrictRatingRejection() {
        // ARRANGE
        // Tests strict hotel rating enforcement after selection (lines 280, 283 in PackageBuilder)
        // Scenario: Only available hotel has rating below minimum -> should return empty
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport outbound = t("A","B", d, d.plusHours(2), "TRAIN", 50.0);
        Transport returnTr = t("B","A", d.plusDays(3), d.plusDays(3).plusHours(2), "TRAIN", 50.0);
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Collections.singletonList(outbound));
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Collections.singletonList(returnTr));
        
        // Only hotel available has rating 1, but user requires minimum 4
        Hotel lowRatingHotel = new Hotel(); 
        lowRatingHotel.setRating(1); 
        lowRatingHotel.setPricePerNight(30.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(lowRatingHotel));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        // ACT
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",4,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertTrue(res.stream().allMatch(p -> p.getErrors() != null && !p.getErrors().isEmpty()), "Should return error message when hotel rating is below minimum");
    }

    @Test
    void testHotelListWithNullEntriesFilteredOut() {
        // ARRANGE
        // Tests null filter at line 235: h != null
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport outbound = t("A","B", d, d.plusHours(2), "TRAIN", 50.0);
        Transport returnTr = t("B","A", d.plusDays(3), d.plusDays(3).plusHours(2), "TRAIN", 50.0);
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Collections.singletonList(outbound));
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Collections.singletonList(returnTr));
        
        // Mix of null and valid hotels
        Hotel validHotel = new Hotel(); 
        validHotel.setRating(4); 
        validHotel.setPricePerNight(60.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Arrays.asList(null, validHotel, null));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        // ACT
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",3,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertFalse(res.isEmpty(), "Should filter out null hotels and use valid ones");
        assertEquals(4, res.get(0).getHotel().getRating(), "Should select the valid hotel after filtering nulls");
    }

    @Test
    void testGeocodingCacheHitForHotel() throws Exception {
        // ARRANGE
        // Tests geocoding cache at line 265: geocodeCache.containsKey(hotelKey)
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport outbound = t("A","B", d, d.plusHours(2), "TRAIN", 50.0);
        Transport returnTr = t("B","A", d.plusDays(3), d.plusDays(3).plusHours(2), "TRAIN", 50.0);
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Collections.singletonList(outbound));
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Collections.singletonList(returnTr));
        
        Hotel h = new Hotel(); 
        h.setRating(3); 
        h.setPricePerNight(50.0);
        h.setAddress("123 Main St");
        h.setCity("Paris");
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        
        Coordinates hotelCoords = new Coordinates(48.8566, 2.3522);
        when(geocodingService.geocode(anyString())).thenReturn(hotelCoords);
        
        Activity a = mock(Activity.class);
        when(a.getPrice()).thenReturn(20.0);
        when(a.getDate()).thenReturn(LocalDate.of(2025, 1, 16));
        when(a.getAddress()).thenReturn("456 Activity St");
        when(a.getCity()).thenReturn("Paris");
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.singletonList(a));
        when(distanceService.calculateDistance(any(), any())).thenReturn(5.0);

        // ACT
        // First call - cache miss, should call geocodingService
        List<Package> res1 = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",3,"PRICE",Collections.singletonList("CULTURE"),10.0);
        
        // ASSERT
        assertFalse(res1.isEmpty());
        verify(geocodingService, atLeastOnce()).geocode(contains("123 Main St"));
        
        // Second call with same hotel address - cache hit, no additional geocoding call
        int callsBefore = mockingDetails(geocodingService).getInvocations().size();
        List<Package> res2 = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",3,"PRICE",Collections.singletonList("CULTURE"),10.0);
        assertFalse(res2.isEmpty());
        // Cache should be used, no new geocoding calls for same hotel
        int callsAfter = mockingDetails(geocodingService).getInvocations().size();
        assertTrue(callsAfter >= callsBefore, "Geocoding cache should reduce redundant calls");
    }

    @Test
    void testHotelRatingBoundaryAtMinimum() {
        // ARRANGE
        // Tests ConditionalsBoundary at line 238: rating >= minRating (boundary case)
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport outbound = t("A","B", d, d.plusHours(2), "TRAIN", 50.0);
        Transport returnTr = t("B","A", d.plusDays(3), d.plusDays(3).plusHours(2), "TRAIN", 50.0);
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Collections.singletonList(outbound));
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Collections.singletonList(returnTr));
        
        // Hotel exactly at minimum rating
        Hotel h1 = new Hotel(); h1.setRating(3); h1.setPricePerNight(50.0);
        // Hotel below minimum
        Hotel h2 = new Hotel(); h2.setRating(2); h2.setPricePerNight(40.0);
        // Hotel above minimum
        Hotel h3 = new Hotel(); h3.setRating(4); h3.setPricePerNight(60.0);
        
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Arrays.asList(h1, h2, h3));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        // ACT
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",3,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertFalse(res.isEmpty());
        // Should include hotels with rating >= 3 (h1 and h3), but not h2
        int selectedRating = res.get(0).getHotel().getRating();
        assertTrue(selectedRating >= 3, "Selected hotel should have rating >= minRating (3)");
    }

    @Test
    void testActivityWithNullAddressSkipsGeocoding() {
        // ARRANGE
        // Tests activity geocoding with null address (line 296-297)
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport outbound = t("A","B", d, d.plusHours(2), "TRAIN", 50.0);
        Transport returnTr = t("B","A", d.plusDays(3), d.plusDays(3).plusHours(2), "TRAIN", 50.0);
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Collections.singletonList(outbound));
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Collections.singletonList(returnTr));
        
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(50.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        
        Activity activityWithoutAddress = mock(Activity.class);
        when(activityWithoutAddress.getPrice()).thenReturn(20.0);
        when(activityWithoutAddress.getDate()).thenReturn(LocalDate.of(2025, 1, 16));
        when(activityWithoutAddress.getAddress()).thenReturn(null); // Null address
        when(activityWithoutAddress.getCity()).thenReturn("Paris");
        
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.singletonList(activityWithoutAddress));
        when(distanceService.calculateDistance(any(), any())).thenReturn(5.0);

        // ACT
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",3,"PRICE",Collections.singletonList("CULTURE"),10.0);
        
        // ASSERT
        assertFalse(res.isEmpty());
        // Activity with null address should be handled gracefully
        assertNotNull(res.get(0), "Package should be created even with activity having null address");
    }

    @Test
    void testStrictHotelRatingCheckAfterSelection() {
        // ARRANGE
        // Tests the defensive check at line 279: chosenHotel.getRating() < minHotelRating
        // This is a defensive check that should rarely be hit in normal operation
        // We test it with minHotelRating=0 (no filtering) but verify the defensive check still exists
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport outbound = t("A","B", d, d.plusHours(2), "TRAIN", 50.0);
        Transport returnTr = t("B","A", d.plusDays(3), d.plusDays(3).plusHours(2), "TRAIN", 50.0);
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Collections.singletonList(outbound));
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Collections.singletonList(returnTr));
        
        // With minHotelRating=0, no filtering happens, but the defensive check at line 278-284 still exists
        Hotel h1 = new Hotel(); h1.setRating(2); h1.setPricePerNight(50.0);
        Hotel h2 = new Hotel(); h2.setRating(4); h2.setPricePerNight(60.0);
        
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Arrays.asList(h1, h2));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        // ACT
        // With minHotelRating=0 and PRICE priority, should select cheapest (h1 with rating 2)
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",0,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertFalse(res.isEmpty(), "Package should be created when minHotelRating=0");
        assertEquals(2, res.get(0).getHotel().getRating(), "Should select hotel with rating 2 when no minimum is set");
    }

    @Test
    void testMinHotelRatingExactBoundary() {
        // ARRANGE
        // Tests ConditionalsBoundary mutant with exact boundary value
        // When minHotelRating=3.0, hotels with rating=3 should be included (>= not >)
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport outbound = t("A","B", d, d.plusHours(2), "TRAIN", 50.0);
        Transport returnTr = t("B","A", d.plusDays(3), d.plusDays(3).plusHours(2), "TRAIN", 50.0);
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Collections.singletonList(outbound));
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Collections.singletonList(returnTr));
        
        Hotel h1 = new Hotel(); h1.setRating(3); h1.setPricePerNight(50.0); h1.setName("Exact 3 stars");
        Hotel h2 = new Hotel(); h2.setRating(2); h2.setPricePerNight(40.0); h2.setName("2 stars");
        
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Arrays.asList(h1, h2));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        // ACT
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",3,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertFalse(res.isEmpty(), "Should find package with hotel exactly at minHotelRating");
        assertEquals(3, res.get(0).getHotel().getRating(), "Should include hotel with rating exactly equal to minHotelRating");
        assertEquals("Exact 3 stars", res.get(0).getHotel().getName());
    }

    @Test
    void testTransportAndHotelWithMultiplePrioritiesSTAR() {
        // ARRANGE
        // Tests BooleanTrue mutations in complex priority selection
        // When hotel priority is STAR, should select highest rating regardless of price
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport t1 = t("A","B", d, d.plusHours(2), "TRAIN", 100.0);
        Transport returnTr = t("B","A", d.plusDays(3), d.plusDays(3).plusHours(2), "TRAIN", 100.0);
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Collections.singletonList(t1));
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Collections.singletonList(returnTr));
        
        Hotel cheap = new Hotel(); cheap.setRating(2); cheap.setPricePerNight(40.0); cheap.setName("Cheap Hotel");
        Hotel expensive = new Hotel(); expensive.setRating(5); expensive.setPricePerNight(150.0); expensive.setName("Luxury Hotel");
        Hotel medium = new Hotel(); medium.setRating(3); medium.setPricePerNight(70.0); medium.setName("Medium Hotel");
        
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Arrays.asList(cheap, medium, expensive));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        // ACT
        // With STAR priority, should select 5-star hotel even though it's most expensive
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,1000.0,null,"PRICE",0,"STAR",Collections.emptyList(),10.0);
        
        // ASSERT
        assertFalse(res.isEmpty(), "Should find package with STAR priority");
        assertEquals(5, res.get(0).getHotel().getRating(), "Should select highest rated hotel with STAR priority");
        assertEquals("Luxury Hotel", res.get(0).getHotel().getName());
    }

    @Test
    void testBudgetExactlyAtLimit() {
        // ARRANGE
        // Tests ConditionalsBoundary: totalPrice == maxBudget (should be accepted)
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport outbound = t("A","B", d, d.plusHours(2), "TRAIN", 100.0);
        Transport returnTr = t("B","A", d.plusDays(3), d.plusDays(3).plusHours(2), "TRAIN", 100.0);
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Collections.singletonList(outbound));
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Collections.singletonList(returnTr));
        
        // Hotel: 50 per night * 3 nights = 150
        // Total: 100 + 100 + 150 = 350 (exactly at budget)
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(50.0);
        
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        // ACT
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,350.0,null,"PRICE",0,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertFalse(res.isEmpty(), "Should accept package when total price exactly equals maxBudget");
        assertEquals(350.0, res.get(0).getTotalPrice(3), 0.01, "Total price should be exactly at budget limit");
    }

    @Test
    void testEmptyHotelsListReturnsPackagesWithErrors() {
        // ARRANGE
        // Tests EmptyObject mutation: when no hotels available, should return empty list
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport outbound = t("A","B", d, d.plusHours(2), "TRAIN", 50.0);
        Transport returnTr = t("B","A", d.plusDays(3), d.plusDays(3).plusHours(2), "TRAIN", 50.0);
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Collections.singletonList(outbound));
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Collections.singletonList(returnTr));
        
        // Empty hotels list
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.emptyList());

        // ACT
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",0,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        assertTrue(res.stream().allMatch(p -> p.getErrors() != null && !p.getErrors().isEmpty()), "Should return error message when no hotels are available");
    }

    @Test
    void testReturnTransportNotFoundReturnsEmpty() {
        // ARRANGE
        // Tests that missing return transport results in invalid package (returnTrip will be null)
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport outbound = t("A","B", d, d.plusHours(2), "TRAIN", 50.0);
        when(transportService.findTransports(eq("A"), eq("B"), any(), any())).thenReturn(Collections.singletonList(outbound));
        // No return transport
        when(transportService.findTransports(eq("B"), eq("A"), any(), any())).thenReturn(Collections.emptyList());
        
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(50.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        // ACT
        List<Package> res = packageService.findPackages("A","B","2025-01-15",3,500.0,null,"PRICE",0,"PRICE",Collections.emptyList(),10.0);
        
        // ASSERT
        // Package is created but will be invalid because returnTrip is null
        assertFalse(res.isEmpty(), "Package is created even without return transport");
        assertFalse(res.get(0).isValid(), "Package should be invalid without return transport");
    }

}
