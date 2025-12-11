package fr.univ.holitrip.model;

/**
 * Represents a hotel available for booking.
 * 
 * Example: Grand Hotel Lyon, 10 Place Bellecour, 4 stars, 120€/night
 */

public class Hotel {
    private String name;
    private String address;
    private String city;
    private int rating;
    private double pricePerNight;

    public Hotel() {
    }

    public Hotel(String name, String address, String city, int rating, double pricePerNight) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.rating = rating;
        this.pricePerNight = pricePerNight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    @Override
    public String toString() {
        return "Hotel{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", rating=" + rating +
                ", pricePerNight=" + pricePerNight +
                '}';
    }
}
