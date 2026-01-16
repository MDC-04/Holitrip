package fr.univ.holitrip.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class PackageTest {

    @Test
    void testGetTotalPriceWithAllComponents() {
        // Tests getTotalPrice with all components present (kills Math mutations)
        Transport outbound = createTransport(100.0);
        Transport return1 = createTransport(150.0);
        Hotel hotel = createHotel(80.0);
        Activity act1 = createActivity(20.0);
        Activity act2 = createActivity(30.0);
        
        Trip outboundTrip = new Trip(Collections.singletonList(outbound));
        Trip returnTrip = new Trip(Collections.singletonList(return1));
        
        Package pkg = new Package(outboundTrip, returnTrip, hotel, Arrays.asList(act1, act2), new ArrayList<>());
        
        // Total = 100 (outbound) + 150 (return) + 80*3 (hotel 3 nights) + 20 + 30 (activities) = 540
        assertEquals(540.0, pkg.getTotalPrice(3), 0.01);
    }

    @Test
    void testGetTotalPriceWithNullOutboundTrip() {
        // Tests getTotalPrice branch when outboundTrip is null (kills NullReturn mutations)
        Transport return1 = createTransport(150.0);
        Hotel hotel = createHotel(80.0);
        Activity act = createActivity(20.0);
        
        Trip returnTrip = new Trip(Collections.singletonList(return1));
        
        Package pkg = new Package(null, returnTrip, hotel, Collections.singletonList(act), new ArrayList<>());
        
        // Total = 0 (no outbound) + 150 (return) + 80*2 (hotel 2 nights) + 20 (activity) = 330
        assertEquals(330.0, pkg.getTotalPrice(2), 0.01);
    }

    @Test
    void testGetTotalPriceWithNullReturnTrip() {
        // Tests getTotalPrice branch when returnTrip is null
        Transport outbound = createTransport(100.0);
        Hotel hotel = createHotel(80.0);
        
        Trip outboundTrip = new Trip(Collections.singletonList(outbound));
        
        Package pkg = new Package(outboundTrip, null, hotel, new ArrayList<>(), new ArrayList<>());
        
        // Total = 100 (outbound) + 0 (no return) + 80*3 (hotel 3 nights) + 0 (no activities) = 340
        assertEquals(340.0, pkg.getTotalPrice(3), 0.01);
    }

    @Test
    void testGetTotalPriceWithNullHotel() {
        // Tests getTotalPrice branch when hotel is null
        Transport outbound = createTransport(100.0);
        Transport return1 = createTransport(150.0);
        Activity act = createActivity(25.0);
        
        Trip outboundTrip = new Trip(Collections.singletonList(outbound));
        Trip returnTrip = new Trip(Collections.singletonList(return1));
        
        Package pkg = new Package(outboundTrip, returnTrip, null, Collections.singletonList(act), new ArrayList<>());
        
        // Total = 100 (outbound) + 150 (return) + 0 (no hotel) + 25 (activity) = 275
        assertEquals(275.0, pkg.getTotalPrice(5), 0.01);
    }

    @Test
    void testGetTotalPriceWithEmptyActivities() {
        // Tests getTotalPrice with empty activities list
        Transport outbound = createTransport(50.0);
        Transport return1 = createTransport(60.0);
        Hotel hotel = createHotel(100.0);
        
        Trip outboundTrip = new Trip(Collections.singletonList(outbound));
        Trip returnTrip = new Trip(Collections.singletonList(return1));
        
        Package pkg = new Package(outboundTrip, returnTrip, hotel, new ArrayList<>(), new ArrayList<>());
        
        // Total = 50 + 60 + 100*2 + 0 = 310
        assertEquals(310.0, pkg.getTotalPrice(2), 0.01);
    }

    @Test
    void testGetTotalPriceWithZeroNights() {
        // Tests getTotalPrice with 0 nights (boundary condition)
        Transport outbound = createTransport(100.0);
        Transport return1 = createTransport(100.0);
        Hotel hotel = createHotel(80.0);
        
        Trip outboundTrip = new Trip(Collections.singletonList(outbound));
        Trip returnTrip = new Trip(Collections.singletonList(return1));
        
        Package pkg = new Package(outboundTrip, returnTrip, hotel, new ArrayList<>(), new ArrayList<>());
        
        // Total = 100 + 100 + 80*0 + 0 = 200
        assertEquals(200.0, pkg.getTotalPrice(0), 0.01);
    }

    @Test
    void testIsValidWithValidPackage() {
        // Tests isValid returns true when package has all components and no errors
        Transport outbound = createTransport(100.0);
        Transport return1 = createTransport(100.0);
        Hotel hotel = createHotel(80.0);
        
        Trip outboundTrip = new Trip(Collections.singletonList(outbound));
        Trip returnTrip = new Trip(Collections.singletonList(return1));
        
        Package pkg = new Package(outboundTrip, returnTrip, hotel, new ArrayList<>(), new ArrayList<>());
        
        assertTrue(pkg.isValid(), "Package should be valid with all trips and hotel");
    }

    @Test
    void testIsValidWithErrors() {
        // Tests isValid returns false when package has errors (kills BooleanTrue mutation)
        Transport outbound = createTransport(100.0);
        Transport return1 = createTransport(100.0);
        Hotel hotel = createHotel(80.0);
        
        Trip outboundTrip = new Trip(Collections.singletonList(outbound));
        Trip returnTrip = new Trip(Collections.singletonList(return1));
        
        Package pkg = new Package(outboundTrip, returnTrip, hotel, new ArrayList<>(), new ArrayList<>());
        pkg.addError("Test error");
        
        assertFalse(pkg.isValid(), "Package should be invalid when it has errors");
    }

    @Test
    void testIsValidWithNullOutboundTrip() {
        // Tests isValid returns false when outboundTrip is null
        Transport return1 = createTransport(100.0);
        Hotel hotel = createHotel(80.0);
        
        Trip returnTrip = new Trip(Collections.singletonList(return1));
        
        Package pkg = new Package(null, returnTrip, hotel, new ArrayList<>(), new ArrayList<>());
        
        assertFalse(pkg.isValid(), "Package should be invalid without outbound trip");
    }

    @Test
    void testIsValidWithNullReturnTrip() {
        // Tests isValid returns false when returnTrip is null
        Transport outbound = createTransport(100.0);
        Hotel hotel = createHotel(80.0);
        
        Trip outboundTrip = new Trip(Collections.singletonList(outbound));
        
        Package pkg = new Package(outboundTrip, null, hotel, new ArrayList<>(), new ArrayList<>());
        
        assertFalse(pkg.isValid(), "Package should be invalid without return trip");
    }

    @Test
    void testIsValidWithNullHotel() {
        // Tests isValid returns false when hotel is null
        Transport outbound = createTransport(100.0);
        Transport return1 = createTransport(100.0);
        
        Trip outboundTrip = new Trip(Collections.singletonList(outbound));
        Trip returnTrip = new Trip(Collections.singletonList(return1));
        
        Package pkg = new Package(outboundTrip, returnTrip, null, new ArrayList<>(), new ArrayList<>());
        
        assertFalse(pkg.isValid(), "Package should be invalid without hotel");
    }

    @Test
    void testAddError() {
        // Tests addError method functionality
        Package pkg = new Package();
        
        assertTrue(pkg.getErrors().isEmpty(), "New package should have no errors");
        
        pkg.addError("First error");
        assertEquals(1, pkg.getErrors().size());
        assertEquals("First error", pkg.getErrors().get(0));
        
        pkg.addError("Second error");
        assertEquals(2, pkg.getErrors().size());
        assertEquals("Second error", pkg.getErrors().get(1));
    }

    @Test
    void testToString() {
        // Tests toString method (improves coverage of Package class)
        Transport outbound = createTransport(100.0);
        Transport return1 = createTransport(100.0);
        Hotel hotel = createHotel(80.0);
        hotel.setName("Grand Hotel");
        
        Trip outboundTrip = new Trip(Collections.singletonList(outbound));
        Trip returnTrip = new Trip(Collections.singletonList(return1));
        
        Package pkg = new Package(outboundTrip, returnTrip, hotel, new ArrayList<>(), new ArrayList<>());
        
        String str = pkg.toString();
        assertNotNull(str);
        assertTrue(str.contains("Package"));
        assertTrue(str.contains("Grand Hotel"));
        assertTrue(str.contains("valid=true"));
    }

    // Helper methods
    private Transport createTransport(double price) {
        LocalDateTime now = LocalDateTime.now();
        Transport t = new Transport();
        t.setDepartureCity("CityA");
        t.setArrivalCity("CityB");
        t.setDepartureDateTime(now);
        t.setArrivalDateTime(now.plusHours(2));
        t.setMode("TRAIN");
        t.setPrice(price);
        return t;
    }

    private Hotel createHotel(double pricePerNight) {
        Hotel h = new Hotel();
        h.setName("Test Hotel");
        h.setAddress("123 Test St");
        h.setCity("TestCity");
        h.setRating(4);
        h.setPricePerNight(pricePerNight);
        return h;
    }

    private Activity createActivity(double price) {
        Activity a = new Activity();
        a.setName("Test Activity");
        a.setAddress("456 Activity Ave");
        a.setCity("TestCity");
        a.setCategory("CULTURE");
        a.setPrice(price);
        return a;
    }
}
