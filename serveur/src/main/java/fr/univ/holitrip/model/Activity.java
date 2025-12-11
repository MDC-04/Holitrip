package fr.univ.holitrip.model;

import java.time.LocalDate;

/**
 * Represents an activity available for booking.
 * 
 * Example: Louvre Museum, Rue de Rivoli 75001 Paris, CULTURE category, 15€
 */

public class Activity {
    private String name;
    private String address;
    private String city;
    private LocalDate date;
    private String category;
    private double price;

    public Activity() {
    }

    public Activity(String name, String address, String city, String category, 
                    LocalDate date, double price) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.date = date;
        this.category = category;
        this.price = price;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Activity{name='" + name + "', category='" + category + 
       "', date=" + date + ", price=" + price + "€}";
    }
}
