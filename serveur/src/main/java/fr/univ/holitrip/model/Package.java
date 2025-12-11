package fr.univ.holitrip.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete travel package.
 * 
 * Example: Bordeaux -> Lyon, 3 days, 1000€ budget
 *   - Outbound trip: Bordeaux -> Lyon
 *   - Return trip: Lyon -> Bordeaux
 *   - Hotel: Grand Hotel Lyon (3 nights)
 *   - Activities: Museum, Concert, Restaurant
 *   - Errors: list of encountered errors (if any)
 */

public class Package {
    private Trip outboundTrip;
    private Trip returnTrip;
    private Hotel hotel;
    private List<Activity> activities;
    private List<String> errors;

    public Package() {
        this.activities = new ArrayList<>();
        this.errors = new ArrayList<>();
    }

    public Package(Trip outboundTrip, Trip returnTrip, Hotel hotel, 
                    List<Activity> activities, List<String> errors) {
        this.outboundTrip = outboundTrip;
        this.returnTrip = returnTrip;
        this.hotel = hotel;
        this.activities = activities != null ? activities : new ArrayList<>();
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    public Trip getOutboundTrip() {
        return outboundTrip;
    }

    public void setOutboundTrip(Trip outboundTrip) {
        this.outboundTrip = outboundTrip;
    }

    public Trip getReturnTrip() {
        return returnTrip;
    }

    public void setReturnTrip(Trip returnTrip) {
        this.returnTrip = returnTrip;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    /**
     * Calculates the total price of the package.
     * Includes outbound trip + return trip + hotel (nights) + all activities.
     * 
     * @param nights number of nights at the hotel
     * @return total price in euros
     */
    public double getTotalPrice(int nights) {
        double totalPrice = 0.0;

        if (outboundTrip != null) {
            totalPrice += outboundTrip.getTotalPrice();
        }
        if (returnTrip != null) {
            totalPrice += returnTrip.getTotalPrice();
        }
        if (hotel != null) {
            totalPrice += hotel.getPricePerNight() * nights;
        }
        for (Activity activity : activities) {
            totalPrice += activity.getPrice();
        }

        return totalPrice;
    }

    /**
     * Checks if the package is valid.
     * A package is valid if it has no errors and contains all essential elements.
     * 
     * @return true if valid (no errors, has trips and hotel), false otherwise
     */
    public boolean isValid() {
        return errors.isEmpty() 
            && outboundTrip != null 
            && returnTrip != null 
            && hotel != null;
    }

    /**
     * Adds an error message to the package.
     * Used when something goes wrong during package creation.
     * 
     * @param error the error message to add
     */
    public void addError(String error) {
        this.errors.add(error);
    }

    @Override
    public String toString() {
        return "Package{" +
                "outbound=" + (outboundTrip != null ? outboundTrip.toString() : "null") +
                ", return=" + (returnTrip != null ? returnTrip.toString() : "null") +
                ", hotel=" + (hotel != null ? hotel.getName() : "null") +
                ", activities=" + activities.size() +
                ", errors=" + errors.size() +
                ", valid=" + isValid() +
                '}';
    }
}