// src/cpp/Car.cpp

#include "Car.h"
#include <iostream>  // For potential debugging output
#include <stdexcept> // For std::out_of_range if using .at() on JSON fields

using namespace std;

// Default Constructor
Car::Car() 
    : name(""), available(false), type(""), rating(0.0), imagePath(""), 
      mileage(""), maxSpeed(""), seats(""), transmission(""), 
      vehicleClass(""), price(0.0), releaseDate("") {}

// Parameterized Constructor
Car::Car(string name, bool available, string type, double rating, 
         string imagePath, string mileage, string maxSpeed, 
         string seats, string transmission, string vehicleClass, 
         double price, string releaseDate)
    : name(name), available(available), type(type), rating(rating), 
      imagePath(imagePath), mileage(mileage), maxSpeed(maxSpeed), 
      seats(seats), transmission(transmission), vehicleClass(vehicleClass), 
      price(price), releaseDate(releaseDate) {}

// --- Getters Implementation ---
string Car::getName() const { return name; }
bool Car::isAvailable() const { return available; }
string Car::getType() const { return type; }
double Car::getRating() const { return rating; }
string Car::getImagePath() const { return imagePath; }
string Car::getMileage() const { return mileage; }
string Car::getMaxSpeed() const { return maxSpeed; }
string Car::getSeats() const { return seats; }
string Car::getTransmission() const { return transmission; }
string Car::getVehicleClass() const { return vehicleClass; }
double Car::getPrice() const { return price; }
string Car::getReleaseDate() const { return releaseDate; }

// --- Setters Implementation ---
void Car::setName(const string& name) { this->name = name; }
void Car::setAvailable(bool available) { this->available = available; }
void Car::setType(const string& type) { this->type = type; }
void Car::setRating(double rating) { this->rating = rating; }
void Car::setImagePath(const string& imagePath) { this->imagePath = imagePath; }
void Car::setMileage(const string& mileage) { this->mileage = mileage; }
void Car::setMaxSpeed(const string& maxSpeed) { this->maxSpeed = maxSpeed; }
void Car::setSeats(const string& seats) { this->seats = seats; }
void Car::setTransmission(const string& transmission) { this->transmission = transmission; }
void Car::setVehicleClass(const string& vehicleClass) { this->vehicleClass = vehicleClass; }
void Car::setPrice(double price) { this->price = price; }
void Car::setReleaseDate(const string& releaseDate) { this->releaseDate = releaseDate; }

// --- JSON Conversion Methods Implementation ---
json Car::toJson() const {
    json j;
    j["name"] = name;
    j["available"] = available;
    j["type"] = type;
    j["rating"] = rating;
    j["imagePath"] = imagePath;
    j["mileage"] = mileage;
    j["maxSpeed"] = maxSpeed;
    j["seats"] = seats;
    j["transmission"] = transmission;
    j["vehicleClass"] = vehicleClass;
    j["price"] = price;
    j["releaseDate"] = releaseDate;
    return j;
}

Car Car::fromJson(const json& j) {
    // Using .value() with a default for optional fields, or .at().get<>() for mandatory fields
    return Car(
        j.value("name", ""),
        j.value("available", false),
        j.value("type", ""),
        j.value("rating", 0.0),
        j.value("imagePath", ""),
        j.value("mileage", ""),
        j.value("maxSpeed", ""),
        j.value("seats", ""),
        j.value("transmission", ""),
        j.value("vehicleClass", ""),
        j.value("price", 0.0),
        j.value("releaseDate", "")
    );
}