package fr.univ.holitrip.model;

import java.time.LocalDateTime;

/**
 * Represents a transport (train or plane journey).
 * 
 * Example: Train from Bordeaux to Paris on 2025-01-15 at 08:00, arriving at 10:30, costs 85€
 */

public class Transport {
    private String departureCity;
    private String arrivalCity;
    private LocalDateTime departureDateTime;
    private LocalDateTime arrivalDateTime;
    private String mode; // "TRAIN" or "PLANE"
    private double price;

    public Transport() {
    }

    public Transport(String departureCity, String arrivalCity, 
                    LocalDateTime departureDateTime, LocalDateTime arrivalDateTime,
                    String mode, double price) {
        this.departureCity = departureCity;
        this.arrivalCity = arrivalCity;
        this.departureDateTime = departureDateTime;
        this.arrivalDateTime = arrivalDateTime;
        this.mode = mode;
        this.price = price;
    }

    public String getDepartureCity() {
        return departureCity;
    }

    public void setDepartureCity(String departureCity) {
        this.departureCity = departureCity;
    }

    public String getArrivalCity() {
        return arrivalCity;
    }

    public void setArrivalCity(String arrivalCity) {
        this.arrivalCity = arrivalCity;
    }

    public LocalDateTime getDepartureDateTime() {
        return departureDateTime;
    }

    public void setDepartureDateTime(LocalDateTime departureDateTime) {
        this.departureDateTime = departureDateTime;
    }

    public LocalDateTime getArrivalDateTime() {
        return arrivalDateTime;
    }

    public void setArrivalDateTime(LocalDateTime arrivalDateTime) {
        this.arrivalDateTime = arrivalDateTime;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Transport{" +
                departureCity + " -> " + arrivalCity +
                ", mode=" + mode +
                ", departure=" + departureDateTime +
                ", arrival=" + arrivalDateTime +
                ", price=" + price + "€}";
    }
}
