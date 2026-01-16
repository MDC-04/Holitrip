package fr.univ.holitrip.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import fr.univ.holitrip.model.Transport;

class TransportHelperTest {

    @Test
    void testTransportDurationMinutes_ValidTransport() {
        // ARRANGE
        Transport t = new Transport();
        t.setDepartureDateTime(LocalDateTime.of(2026, 2, 10, 8, 0));
        t.setArrivalDateTime(LocalDateTime.of(2026, 2, 10, 10, 30));
        
        // ACT
        long duration = TransportHelper.transportDurationMinutes(t);
        
        // ASSERT
        assertEquals(150, duration); // 2h30 = 150 minutes
    }

    @Test
    void testTransportDurationMinutes_NullTransport() {
        // ACT
        long duration = TransportHelper.transportDurationMinutes(null);
        // ASSERT
        assertEquals(Long.MAX_VALUE, duration);
    }

    @Test
    void testTransportDurationMinutes_MissingDates() {
        // ARRANGE
        Transport t = new Transport();
        t.setDepartureDateTime(LocalDateTime.of(2026, 2, 10, 8, 0));
        // Missing arrival date
        
        // ACT
        long duration = TransportHelper.transportDurationMinutes(t);
        // ASSERT
        assertEquals(Long.MAX_VALUE, duration);
    }

    @Test
    void testIsMultiLeg_ValidMultiLeg() {
        // ARRANGE
        Transport t1 = new Transport();
        t1.setDepartureCity("Paris");
        t1.setArrivalCity("Lyon");
        
        Transport t2 = new Transport();
        t2.setDepartureCity("Lyon");
        t2.setArrivalCity("Marseille");
        
        // ASSERT
        assertTrue(TransportHelper.isMultiLeg(Arrays.asList(t1, t2)));
    }

    @Test
    void testIsMultiLeg_SingleTransport() {
        // ARRANGE
        Transport t = new Transport();
        t.setDepartureCity("Paris");
        t.setArrivalCity("Lyon");
        
        // ASSERT
        assertFalse(TransportHelper.isMultiLeg(Collections.singletonList(t)));
    }

    @Test
    void testIsMultiLeg_InvalidContinuity() {
        // ARRANGE
        Transport t1 = new Transport();
        t1.setDepartureCity("Paris");
        t1.setArrivalCity("Lyon");
        
        Transport t2 = new Transport();
        t2.setDepartureCity("Marseille"); // Doesn't match Lyon
        t2.setArrivalCity("Nice");
        
        // ASSERT
        assertFalse(TransportHelper.isMultiLeg(Arrays.asList(t1, t2)));
    }

    @Test
    void testIsMultiLeg_NullTransport() {
        // ASSERT
        assertFalse(TransportHelper.isMultiLeg(null));
    }

    @Test
    void testIsMultiLeg_EmptyList() {
        // ASSERT
        assertFalse(TransportHelper.isMultiLeg(Collections.emptyList()));
    }

    @Test
    void testIsMultiLeg_ContainsNull() {
        // ARRANGE
        Transport t1 = new Transport();
        t1.setDepartureCity("Paris");
        t1.setArrivalCity("Lyon");
        
        // ASSERT
        assertFalse(TransportHelper.isMultiLeg(Arrays.asList(t1, null)));
    }

    @Test
    void testValidateTransportMode_MatchingMode() {
        // ARRANGE
        Transport t1 = new Transport();
        t1.setMode("TRAIN");
        
        Transport t2 = new Transport();
        t2.setMode("TRAIN");
        
        // ASSERT
        assertTrue(TransportHelper.validateTransportMode(Arrays.asList(t1, t2), "TRAIN"));
    }

    @Test
    void testValidateTransportMode_MismatchMode() {
        // ARRANGE
        Transport t1 = new Transport();
        t1.setMode("TRAIN");
        
        Transport t2 = new Transport();
        t2.setMode("PLANE");
        
        // ASSERT
        assertFalse(TransportHelper.validateTransportMode(Arrays.asList(t1, t2), "TRAIN"));
    }

    @Test
    void testValidateTransportMode_NoPreferredMode() {
        // ARRANGE
        Transport t = new Transport();
        t.setMode("TRAIN");
        
        // ACT
        assertTrue(TransportHelper.validateTransportMode(Collections.singletonList(t), null));
        // ASSERT
        assertTrue(TransportHelper.validateTransportMode(Collections.singletonList(t), ""));
    }

    @Test
    void testValidateTransportMode_NoModeInfo() {
        // ARRANGE
        Transport t = new Transport();
        // No mode set
        
        // ASSERT
        assertTrue(TransportHelper.validateTransportMode(Collections.singletonList(t), "TRAIN"));
    }

    @Test
    void testValidateTransportMode_NullList() {
        // ASSERT
        assertFalse(TransportHelper.validateTransportMode(null, "TRAIN"));
    }

    @Test
    void testValidateTransportMode_EmptyList() {
        // ASSERT
        assertFalse(TransportHelper.validateTransportMode(Collections.emptyList(), "TRAIN"));
    }

    @Test
    void testSelectBestTransport_PriceMode() {
        // ARRANGE
        Transport cheap = new Transport();
        cheap.setPrice(50.0);
        cheap.setDepartureDateTime(LocalDateTime.of(2026, 2, 10, 8, 0));
        cheap.setArrivalDateTime(LocalDateTime.of(2026, 2, 10, 10, 0));
        
        Transport expensive = new Transport();
        expensive.setPrice(100.0);
        expensive.setDepartureDateTime(LocalDateTime.of(2026, 2, 10, 8, 0));
        expensive.setArrivalDateTime(LocalDateTime.of(2026, 2, 10, 9, 0));
        
        // ACT
        Transport selected = TransportHelper.selectBestTransport(Arrays.asList(expensive, cheap), null, "PRICE");
        
        // ASSERT
        assertEquals(50.0, selected.getPrice());
    }

    @Test
    void testSelectBestTransport_DurationMode() {
        // ARRANGE
        Transport fast = new Transport();
        fast.setPrice(100.0);
        fast.setDepartureDateTime(LocalDateTime.of(2026, 2, 10, 8, 0));
        fast.setArrivalDateTime(LocalDateTime.of(2026, 2, 10, 9, 0));
        
        Transport slow = new Transport();
        slow.setPrice(50.0);
        slow.setDepartureDateTime(LocalDateTime.of(2026, 2, 10, 8, 0));
        slow.setArrivalDateTime(LocalDateTime.of(2026, 2, 10, 11, 0));
        
        // ACT
        Transport selected = TransportHelper.selectBestTransport(Arrays.asList(slow, fast), null, "DURATION");
        
        // ASSERT
        assertEquals(100.0, selected.getPrice());
    }

    @Test
    void testSelectBestTransport_TimeMode() {
        // ARRANGE
        Transport fast = new Transport();
        fast.setPrice(100.0);
        fast.setDepartureDateTime(LocalDateTime.of(2026, 2, 10, 8, 0));
        fast.setArrivalDateTime(LocalDateTime.of(2026, 2, 10, 9, 0));
        
        Transport slow = new Transport();
        slow.setPrice(50.0);
        slow.setDepartureDateTime(LocalDateTime.of(2026, 2, 10, 8, 0));
        slow.setArrivalDateTime(LocalDateTime.of(2026, 2, 10, 11, 0));
        
        // ACT
        Transport selected = TransportHelper.selectBestTransport(Arrays.asList(slow, fast), null, "TIME");
        
        // ASSERT
        assertEquals(100.0, selected.getPrice());
    }

    @Test
    void testSelectBestTransport_WithModeFilter() {
        // ARRANGE
        Transport train = new Transport();
        train.setMode("TRAIN");
        train.setPrice(50.0);
        train.setDepartureDateTime(LocalDateTime.of(2026, 2, 10, 8, 0));
        train.setArrivalDateTime(LocalDateTime.of(2026, 2, 10, 10, 0));
        
        Transport plane = new Transport();
        plane.setMode("PLANE");
        plane.setPrice(100.0);
        plane.setDepartureDateTime(LocalDateTime.of(2026, 2, 10, 8, 0));
        plane.setArrivalDateTime(LocalDateTime.of(2026, 2, 10, 9, 0));
        
        // ACT
        Transport selected = TransportHelper.selectBestTransport(Arrays.asList(plane, train), "TRAIN", "PRICE");
        
        // ASSERT
        assertEquals("TRAIN", selected.getMode());
    }

    @Test
    void testSelectBestTransport_ModeNotFound_WithModeInfo() {
        // ARRANGE
        Transport plane = new Transport();
        plane.setMode("PLANE");
        plane.setPrice(100.0);
        plane.setDepartureDateTime(LocalDateTime.of(2026, 2, 10, 8, 0));
        plane.setArrivalDateTime(LocalDateTime.of(2026, 2, 10, 9, 0));
        
        // ACT
        Transport selected = TransportHelper.selectBestTransport(Collections.singletonList(plane), "TRAIN", "PRICE");
        
        // ASSERT
        assertNull(selected);
    }

    @Test
    void testSelectBestTransport_ModeNotFound_NoModeInfo() {
        // ARRANGE
        Transport t = new Transport();
        t.setPrice(50.0);
        t.setDepartureDateTime(LocalDateTime.of(2026, 2, 10, 8, 0));
        t.setArrivalDateTime(LocalDateTime.of(2026, 2, 10, 10, 0));
        // No mode set
        
        // ACT
        Transport selected = TransportHelper.selectBestTransport(Collections.singletonList(t), "TRAIN", "PRICE");
        
        // ASSERT
        assertNotNull(selected);
        assertEquals(50.0, selected.getPrice());
    }

    @Test
    void testSelectBestTransport_NullList() {
        // ACT
        assertNull(TransportHelper.selectBestTransport(null, null, "PRICE"));
    }

    @Test
    void testSelectBestTransport_EmptyList() {
        // ASSERT
        assertNull(TransportHelper.selectBestTransport(Collections.emptyList(), null, "PRICE"));
    }

    @Test
    void testSelectBestTransport_DefaultPriority() {
        // ARRANGE
        Transport t1 = new Transport();
        t1.setPrice(50.0);
        t1.setDepartureDateTime(LocalDateTime.of(2026, 2, 10, 8, 0));
        t1.setArrivalDateTime(LocalDateTime.of(2026, 2, 10, 10, 0));
        
        Transport t2 = new Transport();
        t2.setPrice(100.0);
        t2.setDepartureDateTime(LocalDateTime.of(2026, 2, 10, 8, 0));
        t2.setArrivalDateTime(LocalDateTime.of(2026, 2, 10, 9, 0));
        
        // ACT
        Transport selected = TransportHelper.selectBestTransport(Arrays.asList(t1, t2), null, null);
        
        // Should return first one when no priority specified
        // ASSERT
        assertEquals(50.0, selected.getPrice());
    }
}
