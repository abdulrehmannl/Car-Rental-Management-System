// src/com/carrental/model/Car.java
package com.carrental.model;

import java.io.Serializable;
import java.awt.image.BufferedImage; // Assuming you might load image from path
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Car implements Serializable {
    private static final long serialVersionUID = 1L;
    public String name;
    public boolean available;
    public String type;
    public double rating;
    public String imagePath;
    public String mileage;
    public String maxSpeed;
    public String seats;
    public String transmission;
    public String vehicleClass;
    public double price;
    public String releaseDate;

    public Car(String name, boolean available, String type, double rating, String imagePath, String mileage, String maxSpeed, String seats, String transmission, String vehicleClass, double price, String releaseDate) {
        this.name = name;
        this.available = available;
        this.type = type;
        this.rating = rating;
        this.imagePath = imagePath;
        this.mileage = mileage;
        this.maxSpeed = maxSpeed;
        this.seats = seats;
        this.transmission = transmission;
        this.vehicleClass = vehicleClass;
        this.price = price;
        this.releaseDate = releaseDate;
    }

    // Getters and Setters (as in your original LoginPage.Car)
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public String getMileage() { return mileage; }
    public void setMileage(String mileage) { this.mileage = mileage; }
    public String getMaxSpeed() { return maxSpeed; }
    public void setMaxSpeed(String maxSpeed) { this.maxSpeed = maxSpeed; }
    public String getSeats() { return seats; }
    public void setSeats(String seats) { this.seats = seats; }
    public String getTransmission() { return transmission; }
    public void setTransmission(String transmission) { this.transmission = transmission; }
    public String getVehicleClass() { return vehicleClass; }
    public void setVehicleClass(String vehicleClass) { this.vehicleClass = vehicleClass; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public String getAvailabilityString() { return available ? "Yes" : "No"; }
}