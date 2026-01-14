package fr.univ.holitrip.service.unit;

import fr.univ.holitrip.model.Activity;
import fr.univ.holitrip.model.Hotel;
import fr.univ.holitrip.model.Package;
import fr.univ.holitrip.model.Transport;
import fr.univ.holitrip.model.Coordinates;
import fr.univ.holitrip.service.impl.PackageBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Mock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PackageBuilderExtraTests {
    @Mock
    private fr.univ.holitrip.service.TransportService transportService;
    @Mock
    private fr.univ.holitrip.service.HotelService hotelService;
    @Mock
    private fr.univ.holitrip.service.ActivityService activityService;
    @Mock
    private fr.univ.holitrip.service.DistanceService distanceService;
    @Mock
    private fr.univ.holitrip.service.GeocodingService geocodingService;

    private PackageBuilder builder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        builder = new PackageBuilder(transportService, hotelService, activityService, distanceService, geocodingService);
    }

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
    void multiLegAcceptedWhenGapSufficient() {
        // two legs A->B (arr 09:00), B->C (dep 10:00) gap 60min
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport a = t("A","B", d, d.plusHours(1), "TRAIN", 50.0);
        Transport b = t("B","C", d.plusHours(2), d.plusHours(3), "TRAIN", 40.0);
        when(transportService.findTransports(eq("A"), eq("C"), any(), any())).thenReturn(Arrays.asList(a,b));
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(10.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        List<Package> res = builder.findPackages("A","C","2025-01-15",3,500.0,null,"PRICE",3,"PRICE",Collections.emptyList(),10.0);
        assertFalse(res.isEmpty());
        assertTrue(res.get(0).getOutboundTrip().getTransports().size() == 2);
    }

    @Test
    void multiLegRejectedWhenGapTooSmall() {
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport a = t("A","B", d, d.plusHours(1), "TRAIN", 50.0);
        // second departs 10 minutes after arrival
        Transport b = t("B","C", d.plusHours(1).plusMinutes(10), d.plusHours(3), "TRAIN", 40.0);
        when(transportService.findTransports(eq("A"), eq("C"), any(), any())).thenReturn(Arrays.asList(a,b));
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(10.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        List<Package> res = builder.findPackages("A","C","2025-01-15",3,500.0,null,"PRICE",3,"PRICE",Collections.emptyList(),10.0);
        assertTrue(res.isEmpty());
    }

    @Test
    void multiLegRejectedWhenModeNotHomogeneousWithPreference() {
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport a = t("A","B", d, d.plusHours(1), "TRAIN", 50.0);
        Transport b = t("B","C", d.plusHours(2), d.plusHours(3), "PLANE", 40.0);
        when(transportService.findTransports(eq("A"), eq("C"), any(), any())).thenReturn(Arrays.asList(a,b));
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(10.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        // user prefers TRAIN -> should reject mixed modes
        List<Package> res = builder.findPackages("A","C","2025-01-15",3,500.0,"TRAIN","PRICE",3,"PRICE",Collections.emptyList(),10.0);
        assertTrue(res.isEmpty());
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

        List<Package> res = builder.findPackages("City1","City2","2025-01-15",3,1000.0,"TRAIN","PRICE",3,"PRICE",Collections.emptyList(),10.0);
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

        List<Package> res = builder.findPackages("S","T","2025-01-15",3,500.0,"TRAIN","PRICE",3,"PRICE",Collections.emptyList(),10.0);
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

        List<Package> res = builder.findPackages("U","V","2025-01-15",3,500.0,null,"PRICE",3,"PRICE",Collections.emptyList(),10.0);
        assertFalse(res.isEmpty());
        boolean usedShort = res.get(0).getOutboundTrip().getTransports().stream().anyMatch(tr -> tr.getArrivalDateTime().equals(tShort.getArrivalDateTime()));
        assertTrue(usedShort);
    }

    @Test
    void geocodingFailureForHotelHandledGracefullyActivitiesIncludedIfDistanceOk() throws Exception {
        // geocoding for hotel throws, activityService still returns activities and distanceService returns small distance
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport direct = t("P","Q", d, d.plusHours(2), "TRAIN", 80.0);
        when(transportService.findTransports(anyString(), anyString(), any(), any())).thenReturn(Collections.singletonList(direct));
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(20.0); h.setAddress("Some addr"); h.setCity("QCity");
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        when(geocodingService.geocode(contains("Some addr"))).thenThrow(new RuntimeException("geo down"));
        Activity a = new Activity(); a.setPrice(10.0); a.setDate(LocalDate.of(2025,1,16));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.singletonList(a));
        when(distanceService.calculateDistance(any(), any())).thenReturn(5.0);

        List<Package> res = builder.findPackages("P","Q","2025-01-15",3,500.0,null,"PRICE",3,"PRICE",Collections.singletonList("CULTURE"),10.0);
        assertFalse(res.isEmpty());
        // activity should be included despite hotel geocode failure because distanceService returned small distance
        assertFalse(res.get(0).getActivities().isEmpty());
    }

    @Test
    void distanceCalculationFailureExcludesActivity() throws Exception {
        LocalDateTime d = LocalDateTime.of(2025,1,15,8,0);
        Transport direct = t("P","Q", d, d.plusHours(2), "TRAIN", 80.0);
        when(transportService.findTransports(anyString(), anyString(), any(), any())).thenReturn(Collections.singletonList(direct));
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(20.0); h.setAddress("Addr"); h.setCity("QCity");
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        when(geocodingService.geocode(anyString())).thenReturn(new Coordinates(48.0,2.0));
        Activity a = new Activity(); a.setPrice(10.0); a.setDate(LocalDate.of(2025,1,16));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.singletonList(a));
        when(distanceService.calculateDistance(any(), any())).thenThrow(new RuntimeException("distance failed"));

        List<Package> res = builder.findPackages("P","Q","2025-01-15",3,500.0,null,"PRICE",3,"PRICE",Collections.singletonList("CULTURE"),10.0);
        assertFalse(res.isEmpty());
        // activity should be excluded because distance calculation failed
        assertTrue(res.get(0).getActivities().isEmpty());
    }

    @Test
    void malformedDepartureDateHandledWithoutException() {
        Transport direct = t("B","C", null, null, "TRAIN", 80.0);
        when(transportService.findTransports(anyString(), anyString(), any(), any())).thenReturn(Collections.singletonList(direct));
        Hotel h = new Hotel(); h.setRating(3); h.setPricePerNight(20.0);
        when(hotelService.findHotels(anyString(), anyInt(), anyDouble())).thenReturn(Collections.singletonList(h));
        when(activityService.findActivities(anyString(), anyList(), any(), anyDouble(), any(), anyDouble())).thenReturn(Collections.emptyList());

        // provide malformed date
        List<Package> res = builder.findPackages("B","C","not-a-date",3,500.0,null,"PRICE",3,"PRICE",Collections.emptyList(),10.0);
        assertNotNull(res);
    }
}
