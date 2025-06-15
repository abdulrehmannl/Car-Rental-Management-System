// src/cpp/Car.h
#ifndef CAR_H
#define CAR_H

#include <string>
#include <vector> // Potentially needed if you have list-like members in Car
#include "json.hpp" // For JSON conversion

// Alias for convenience (from nlohmann/json library)
using json = nlohmann::json;

class Car {
private:
    std::string name;
    bool available;
    std::string type;
    double rating;
    std::string imagePath;
    std::string mileage;     // Assuming string like "10000 km"
    std::string maxSpeed;    // Assuming string like "200 km/h"
    std::string seats;       // Assuming string like "4 seats"
    std::string transmission;// "Manual" or "Automatic"
    std::string vehicleClass;// "Sedan", "SUV", etc.
    double price;            // Price per day/rental period
    std::string releaseDate; // Date string

public:
    // Default constructor
    Car();

    // Parameterized constructor
    Car(std::string name, bool available, std::string type, double rating, 
        std::string imagePath, std::string mileage, std::string maxSpeed, 
        std::string seats, std::string transmission, std::string vehicleClass, 
        double price, std::string releaseDate);

    // --- Getters ---
    std::string getName() const;
    bool isAvailable() const;
    std::string getType() const;
    double getRating() const;
    std::string getImagePath() const;
    std::string getMileage() const;
    std::string getMaxSpeed() const;
    std::string getSeats() const;
    std::string getTransmission() const;
    std::string getVehicleClass() const;
    double getPrice() const;
    std::string getReleaseDate() const;

    // --- Setters (optional, but good for modifying objects after creation) ---
    void setName(const std::string& name);
    void setAvailable(bool available);
    void setType(const std::string& type);
    void setRating(double rating);
    void setImagePath(const std::string& imagePath);
    void setMileage(const std::string& mileage);
    void setMaxSpeed(const std::string& maxSpeed);
    void setSeats(const std::string& seats);
    void setTransmission(const std::string& transmission);
    void setVehicleClass(const std::string& vehicleClass);
    void setPrice(double price);
    void setReleaseDate(const std::string& releaseDate);

    // --- JSON Conversion Methods ---
    json toJson() const; // Converts Car object to JSON
    static Car fromJson(const json& j); // Creates Car object from JSON from a JSON object
};

#endif // CAR_H