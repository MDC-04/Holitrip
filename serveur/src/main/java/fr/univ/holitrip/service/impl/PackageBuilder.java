package fr.univ.holitrip.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

import fr.univ.holitrip.model.Activity;
import fr.univ.holitrip.model.Hotel;
import fr.univ.holitrip.model.Package;
import fr.univ.holitrip.model.Trip;
import fr.univ.holitrip.model.Transport;
import fr.univ.holitrip.model.Coordinates;
import fr.univ.holitrip.service.ActivityService;
import fr.univ.holitrip.service.DistanceService;
import fr.univ.holitrip.service.HotelService;
import fr.univ.holitrip.service.GeocodingService;
import fr.univ.holitrip.service.PackageService;
import fr.univ.holitrip.service.TransportService;
import fr.univ.holitrip.util.TransportHelper;

public class PackageBuilder implements PackageService {
    private final TransportService transportService;
    private final HotelService hotelService;
    private final ActivityService activityService;
    private final DistanceService distanceService;
    private final GeocodingService geocodingService;

    public PackageBuilder(TransportService transportService, HotelService hotelService,
            ActivityService activityService, DistanceService distanceService, GeocodingService geocodingService) {
        this.transportService = transportService;
        this.hotelService = hotelService;
        this.activityService = activityService;
        this.distanceService = distanceService;
        this.geocodingService = geocodingService;
        this.geocodeCache = new HashMap<>();
    }

    // Simple in-memory cache for geocoding results during a single execution.
    private final Map<String, Coordinates> geocodeCache;

    @Override
    public List<Package> findPackages(String departureCity, String destinationCity, String departureDate,
            int tripDurationDays, double maxBudget, String transportMode, String transportPriority, int minHotelRating,
            String hotelPriority, List<String> activityCategories, double maxDistanceKm) {

        // Parse dates to LocalDateTime for transport queries and validation
        java.time.LocalDateTime outboundDateTime = null;
        java.time.LocalDateTime returnDateTime = null;
        try {
            if (departureDate != null && !departureDate.isBlank()) {
                java.time.LocalDate d = java.time.LocalDate.parse(departureDate);
                outboundDateTime = d.atTime(8, 0); // default morning departure
                returnDateTime = outboundDateTime.plusDays(Math.max(1, tripDurationDays)).withHour(18).withMinute(0);
            }
        } catch (Exception e) {
            // ignore parse errors and leave null (services should handle null)
        }

        // Retrieve candidates from services (tests mock these calls)
        List<Transport> transports = transportService.findTransports(departureCity, destinationCity, outboundDateTime, transportMode);
        List<Hotel> hotels = hotelService.findHotels(destinationCity, minHotelRating, Double.MAX_VALUE);

        // 1) If all sources empty -> return a Package containing errors
        if ((transports == null || transports.isEmpty())
            && (hotels == null || hotels.isEmpty())) {
            Package p = new Package();
            p.addError("No data available for given criteria");
            return Collections.singletonList(p);
        }

        // 2) If transports empty but others present -> cannot build package
        if (transports == null || transports.isEmpty()) {
            return Collections.emptyList();
        }

        // 3) If hotels empty -> cannot build package
        if (hotels == null || hotels.isEmpty()) {
            return Collections.emptyList();
        }

        // 4) Choose transport according to priority (outbound)
        // JsonTransportService now returns multi-leg solutions directly as [leg1, leg2]
        List<Transport> chosenOutboundLegs = null;
        
        if (transports != null && !transports.isEmpty()) {
            // Check if transports represent a multi-leg solution from JsonTransportService
            if (TransportHelper.isMultiLeg(transports)) {
                // This is a multi-leg solution - validate mode and use directly
                if (TransportHelper.validateTransportMode(transports, transportMode)) {
                    chosenOutboundLegs = transports;
                }
            } else {
                // These are direct transport options - select best one
                Transport chosen = TransportHelper.selectBestTransport(transports, transportMode, transportPriority);
                if (chosen != null) {
                    chosenOutboundLegs = Collections.singletonList(chosen);
                }
            }
        }

        // If user explicitly requested a transport mode but none matched, we cannot build a package
        if (transportMode != null && !transportMode.isBlank() && chosenOutboundLegs == null) {
            return Collections.emptyList();
        }

        // Find return transports (destination -> departure)
        // Note: For return trip, we don't enforce strict mode matching if outbound had a preference
        // This allows packages where outbound is TRAIN but return is PLANE if no TRAIN available
        List<Transport> returnTransports = transportService.findTransports(destinationCity, departureCity, returnDateTime, null);
        List<Transport> chosenReturnLegs = null;
        
        if (returnTransports != null && !returnTransports.isEmpty()) {
            // Check if return transports represent a multi-leg solution
            if (TransportHelper.isMultiLeg(returnTransports)) {
                // This is a multi-leg solution - use directly without strict mode validation for return
                chosenReturnLegs = returnTransports;
            } else {
                // These are direct transport options - select best one
                // Prefer transportMode if available, but accept any mode if not found
                Transport chosen = TransportHelper.selectBestTransport(returnTransports, transportMode, transportPriority);
                if (chosen == null) {
                    // If preferred mode not found, take any available transport
                    chosen = TransportHelper.selectBestTransport(returnTransports, null, transportPriority);
                }
                if (chosen != null) {
                    chosenReturnLegs = Collections.singletonList(chosen);
                }
            }
        }

        // 5) Filter hotels by minHotelRating (defensive) and choose according to hotelPriority
        if (minHotelRating > 0) {
            List<Hotel> filteredHotels = hotels.stream()
                    .filter(h -> h != null)
                    .filter(h -> {
                        try {
                            return h.getRating() >= minHotelRating;
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
            if (filteredHotels.isEmpty()) {
                return Collections.emptyList();
            }
            hotels = filteredHotels;
        }

        // 5) Choose hotel according to hotelPriority
        Hotel chosenHotel = hotels.get(0);
        if ("PRICE".equalsIgnoreCase(hotelPriority)) {
            chosenHotel = hotels.stream().min(Comparator.comparingDouble(Hotel::getPricePerNight)).orElse(chosenHotel);
        } else if ("STAR".equalsIgnoreCase(hotelPriority) || "RATING".equalsIgnoreCase(hotelPriority)) {
            chosenHotel = hotels.stream().max(Comparator.comparingInt(Hotel::getRating)).orElse(chosenHotel);
        }


        // Geocode hotel to pass coordinates to ActivityService so it can filter by distance
        Coordinates hotelLocation = null;
        try {
            if (chosenHotel != null && chosenHotel.getAddress() != null) {
                String fullAddress = chosenHotel.getAddress() + ", " + chosenHotel.getCity();
                String hotelKey = fullAddress.toLowerCase();
                if (geocodeCache.containsKey(hotelKey)) {
                    hotelLocation = geocodeCache.get(hotelKey);
                } else {
                    hotelLocation = geocodingService.geocode(fullAddress);
                    if (hotelLocation != null) geocodeCache.put(hotelKey, hotelLocation);
                }
            }
        } catch (Exception ignored) {
            // If geocoding fails, leave hotelLocation null and delegate filtering responsibility to calling code
        }

        // Enforce min hotel rating strictly: if chosen hotel does not meet the minimum, cannot build package
        if (chosenHotel != null && minHotelRating > 0) {
            try {
                if (chosenHotel.getRating() < minHotelRating) {
                    return Collections.emptyList();
                }
            } catch (Exception ignored) {
                return Collections.emptyList();
            }
        }

        // Ask activityService to return activities already filtered by distance if possible
        List<Activity> candidateActivities = activityService.findActivities(destinationCity, activityCategories, null, Double.MAX_VALUE, hotelLocation, maxDistanceKm);

        // First, filter candidateActivities by distance using distanceService (call mocked in tests)
        List<Activity> withinDistance = new ArrayList<>();
        if (candidateActivities != null && !candidateActivities.isEmpty()) {
            for (Activity a : candidateActivities) {
                Coordinates activityCoord = null;
                try {
                    if (a.getAddress() != null) {
                        String key = (a.getAddress() + ", " + a.getCity()).toLowerCase();
                        if (geocodeCache.containsKey(key)) {
                            activityCoord = geocodeCache.get(key);
                        } else {
                            activityCoord = geocodingService.geocode(a.getAddress() + ", " + a.getCity());
                            if (activityCoord != null) geocodeCache.put(key, activityCoord);
                        }
                    }
                } catch (Exception ignored) {
                    // ignore geocoding failure for this activity; we'll try distance check anyway
                }
                // Attempt distance calculation
                // If hotel location is available, check distance
                // If hotel geocoding failed (hotelLocation==null), still try with activity coords
                try {
                    double d = distanceService.calculateDistance(hotelLocation, activityCoord);
                    if (d <= maxDistanceKm) {
                        withinDistance.add(a);
                    }
                } catch (Exception e) {
                    // if distance calculation fails (throws exception), exclude activity
                    // This ensures activities are excluded when distanceService.calculateDistance throws
                }
            }
        }

        // Remove activities with duplicate dates (keep first) and select within budget greedily
        List<Activity> filtered = new ArrayList<>();
        if (!withinDistance.isEmpty()) {
            Set<java.time.LocalDate> seenDates = new HashSet<>();
            List<Activity> uniqueByDate = new ArrayList<>();
            for (Activity a : withinDistance) {
                java.time.LocalDate date = a.getDate();
                if (date == null || !seenDates.contains(date)) {
                    uniqueByDate.add(a);
                    if (date != null) seenDates.add(date);
                }
            }

            double transportCostEstimate = 0.0;
            if (chosenOutboundLegs != null) transportCostEstimate = chosenOutboundLegs.stream().mapToDouble(Transport::getPrice).sum();
            double returnTransportCostEstimate = 0.0;
            if (chosenReturnLegs != null) returnTransportCostEstimate = chosenReturnLegs.stream().mapToDouble(Transport::getPrice).sum();
            double hotelCostEstimate = chosenHotel != null ? chosenHotel.getPricePerNight() * tripDurationDays : 0.0;
            double activitiesBudget = maxBudget - (transportCostEstimate + returnTransportCostEstimate) - hotelCostEstimate;
            if (activitiesBudget < 0) activitiesBudget = 0.0;

            List<Activity> sorted = uniqueByDate.stream()
                    .sorted(Comparator.comparingDouble(Activity::getPrice))
                    .collect(Collectors.toList());
            double sum = 0.0;
            for (Activity a : sorted) {
                double price = a.getPrice();
                if (sum + price <= activitiesBudget) {
                    filtered.add(a);
                    sum += price;
                }
            }
        }

        // Assemble package
        Package pkg = new Package();
        pkg.setHotel(chosenHotel);
        pkg.setActivities(filtered);

        Trip outbound = new Trip();
        if (chosenOutboundLegs != null) outbound.setTransports(chosenOutboundLegs);
        pkg.setOutboundTrip(outbound);

        // Build a minimal return trip using chosenReturnTransport if available
        if (chosenReturnLegs != null) {
            Trip returnTrip = new Trip();
            returnTrip.setTransports(chosenReturnLegs);
            pkg.setReturnTrip(returnTrip);
        }

        // Validate correspondence times: ensure outbound arrival is before return departure
        try {
            if (pkg.getOutboundTrip() != null && pkg.getReturnTrip() != null) {
                java.time.LocalDateTime lastArrival = pkg.getOutboundTrip().getTransports().stream()
                    .map(Transport::getArrivalDateTime)
                    .filter(Objects::nonNull)
                    .max(java.util.Comparator.naturalOrder())
                    .orElse(null);
                java.time.LocalDateTime firstReturnDepart = pkg.getReturnTrip().getTransports().stream()
                    .map(Transport::getDepartureDateTime)
                    .filter(Objects::nonNull)
                    .min(java.util.Comparator.naturalOrder())
                    .orElse(null);
                if (lastArrival != null && firstReturnDepart != null && !lastArrival.isBefore(firstReturnDepart)) {
                    // invalid correspondence (return departs before/outbound arrival) -> cannot build
                    return Collections.emptyList();
                }
            }
        } catch (Exception ignored) {
            // if we cannot validate times, be permissive and continue
        }

        // Check overall budget: if total price exceeds maxBudget, add error but still return package
        double totalPrice = pkg.getTotalPrice(tripDurationDays);
        if (totalPrice > maxBudget) {
            pkg.addError("Budget exceeded: total price " + totalPrice + " exceeds maximum budget " + maxBudget);
        }

        return Collections.singletonList(pkg);
    }

}
