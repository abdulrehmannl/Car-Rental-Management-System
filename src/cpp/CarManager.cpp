// src/cpp/CarManager.cpp
#include "CarManager.h"
#include <fstream>
#include <sstream>
#include <algorithm> // For std::transform and std::sort
#include <iostream>  // For std::cerr and std::cout
#include <vector>    // Explicitly include for clarity, though CarManager.h likely has it

using namespace std; 

// Define the file name here (or in CarManager.h as a const static member if preferred)
#ifndef CAR_STORAGE_FILE
#define CAR_STORAGE_FILE "cars_data.txt" // Default file if not defined elsewhere
#endif

CarManager::CarManager() {
    loadCarsFromFile(CAR_STORAGE_FILE);
    if (cars.empty()) { // Initialize with default cars if file is empty/not found
        cout << "C++: Initializing CarManager with default cars as cars_data.txt is empty or missing." << endl;
        cars.push_back({"Haval H6", true, "SUV", 4.2, "src/Images/hav.jpg", "12 km/l", "180 km/h", "5", "Automatic", "SUV", 35000.00, "2023-01-15" });
        cars.push_back({"Fortuner", true, "SUV", 4.6, "src/Images/fort.jpg", "9 km/l", "190 km/h", "7", "Automatic", "SUV", 55000.00, "2022-03-20"});
        cars.push_back({"Toyota Corolla", true, "Sedan", 4.4, "src/Images/tc.jpg", "14 km/l", "195 km/h", "5", "Manual", "Sedan", 28000.00, "2023-05-10"});
        cars.push_back({"Toyota Yaris", false, "Sedan", 4.0, "src/Images/y.jpg", "17 km/l", "170 km/h", "5", "Automatic", "Sedan", 22000.00, "2021-11-01"});
        cars.push_back({"Honda Vezel", true, "SUV", 4.5, "src/Images/v.jpg", "18 km/l", "185 km/h", "5", "Automatic", "Compact SUV", 32000.00, "2024-02-28"});
        cars.push_back({"Honda City", true, "Sedan", 4.3, "src/Images/hcity.jpg", "16 km/l", "175 km/h", "5", "Manual", "Sedan", 24000.00, "2023-07-22"});
        cars.push_back({"Honda Civic", false, "Sedan", 4.7, "src/Images/hcivic.jpg", "13 km/l", "200 km/h", "5", "Automatic", "Sport Sedan", 38000.00, "2024-01-05"});
        cars.push_back({"Changan Alsvin", false, "Sedan", 4.1, "src/Images/alsvin.jpg", "15 km/l", "180 km/h", "5", "Manual", "Subcompact", 18000.00, "2022-09-10"});
        cars.push_back({"Suzuki Swift", true, "Hatchback", 4.0, "src/Images/s_swift.jpg", "19 km/l", "160 km/h", "5", "Manual", "Hatchback", 19000.00, "2023-04-01"});
        cars.push_back({"Hyundai Tucson", true, "SUV", 4.3, "src/Images/h_tucson.jpg", "11 km/l", "185 km/h", "5", "Automatic", "Mid-size SUV", 40000.00, "2023-09-18"});
        cars.push_back({"Kia Sportage", true, "SUV", 4.5, "src/Images/k_sportage.jpg", "10 km/l", "190 km/h", "5", "Automatic", "Mid-size SUV", 42000.00, "2024-03-01"});
        cars.push_back({"Audi A4", true, "Luxury Sedan", 4.8, "src/Images/a_a4.jpg", "10 km/l", "220 km/h", "5", "Automatic", "Luxury Sedan", 65000.00, "2023-11-20"});
        cars.push_back({"BMW X5", false, "Luxury SUV", 4.9, "src/Images/bmw_x5.jpg", "8 km/l", "240 km/h", "5", "Automatic", "Luxury SUV", 85000.00, "2022-07-15"});
        cars.push_back({"Mercedes C-Class", true, "Luxury Sedan", 4.7, "src/Images/merc_c.jpg", "11 km/l", "210 km/h", "5", "Automatic", "Luxury Sedan", 70000.00, "2024-01-10"});
        cars.push_back({"Tesla Model 3", true, "Electric", 4.9, "src/Images/tesla_m3.jpg", "400 km range", "225 km/h", "5", "Automatic", "Electric Sedan", 60000.00, "2023-06-01"});
        cars.push_back({"Ford Mustang", true, "Sports Car", 4.7, "src/Images/Ford_Mustang.jpg", "9 km/l", "250 km/h", "2", "Manual", "Muscle Car", 50000.00, "2023-02-14"});
        saveCarsToFile(CAR_STORAGE_FILE);
    }
}

void CarManager::loadCarsFromFile(const string& filename) {
    ifstream file(filename);
    if (!file.is_open()) {
        cerr << "C++: Car data file not found: " << filename << endl;
        return;
    }
    cars.clear();
    string line;
    while (getline(file, line)) {
        stringstream ss(line);
        string segment;
        vector<string> segments;
        while(getline(ss, segment, '|')) { segments.push_back(segment); }
        
        if (segments.size() == 12) {
            Car car; // Create a default Car object
            car.setName(segments[0]);
            car.setAvailable(segments[1] == "1");
            car.setType(segments[2]);
            try { car.setRating(stod(segments[3])); } catch (...) { car.setRating(0.0); }
            car.setImagePath(segments[4]);
            car.setMileage(segments[5]);
            car.setMaxSpeed(segments[6]);
            car.setSeats(segments[7]);
            car.setTransmission(segments[8]);
            car.setVehicleClass(segments[9]);
            try { car.setPrice(stod(segments[10])); } catch (...) { car.setPrice(0.0); }
            car.setReleaseDate(segments[11]);
            cars.push_back(car);
        } else {
            cerr << "C++: Warning: Malformed line in " << filename << ": " << line << " (Expected 12 segments, found " << segments.size() << ")" << endl;
        }
    }
    file.close();
    cout << "C++: Cars loaded from " << filename << ". Total: " << cars.size() << endl;
}

void CarManager::saveCarsToFile(const string& filename) {
    ofstream file(filename);
    if (!file.is_open()) {
        cerr << "C++: Error: Could not open file for saving cars: " << filename << endl;
        return;
    }
    for (const auto& car : cars) {
        // Use getters to retrieve the private member data
        file << car.getName() << "|" << (car.isAvailable() ? "1" : "0") << "|" << car.getType() << "|"
             << car.getRating() << "|" << car.getImagePath() << "|" << car.getMileage() << "|"
             << car.getMaxSpeed() << "|" << car.getSeats() << "|" << car.getTransmission() << "|"
             << car.getVehicleClass() << "|" << car.getPrice() << "|" << car.getReleaseDate() << endl;
    }
    file.close();
    cout << "C++: Cars saved to " << filename << ". Total: " << cars.size() << endl;
}

vector<Car> CarManager::getAllCars() { return cars; }

void CarManager::addCar(const Car& car) { 
    cars.push_back(car); 
    saveCarsToFile(CAR_STORAGE_FILE); 
}

bool CarManager::updateCar(const Car& car) {
    for (auto& existingCar : cars) {
        if (existingCar.getName() == car.getName()) {
            existingCar = car; 
            saveCarsToFile(CAR_STORAGE_FILE);
            return true;
        }
    }
    return false;
}

bool CarManager::deleteCar(const string& name) {
    auto it = remove_if(cars.begin(), cars.end(), [&](const Car& c) { return c.getName() == name; });
    if (it != cars.end()) {
        cars.erase(it, cars.end());
        saveCarsToFile(CAR_STORAGE_FILE);
        return true;
    }
    return false;
}

vector<Car> CarManager::searchCars(const string& query, const string& searchField) {
    vector<Car> results;
    string lowerQuery = query;
    transform(lowerQuery.begin(), lowerQuery.end(), lowerQuery.begin(), ::tolower);
    for (const auto& car : cars) {
        string fieldValue;
        if (searchField == "name") fieldValue = car.getName();
        else if (searchField == "type") fieldValue = car.getType();
        else if (searchField == "vehicleClass") fieldValue = car.getVehicleClass();
        else if (searchField == "transmission") fieldValue = car.getTransmission();
        else if (searchField == "mileage") fieldValue = car.getMileage();
        else if (searchField == "maxSpeed") fieldValue = car.getMaxSpeed();
        else if (searchField == "seats") fieldValue = car.getSeats();
        else if (searchField == "releaseDate") fieldValue = car.getReleaseDate();
        
        transform(fieldValue.begin(), fieldValue.end(), fieldValue.begin(), ::tolower);
        if (fieldValue.find(lowerQuery) != string::npos) { results.push_back(car); }
    }
    return results;
}

vector<Car> CarManager::filterCars(const string& typeFilter, double minRating, double maxPrice, bool availableOnly) {
    vector<Car> filtered;
    for (const auto& car : cars) {
        bool passesType = typeFilter.empty() || (car.getType() == typeFilter);
        bool passesRating = car.getRating() >= minRating;
        bool passesPrice = car.getPrice() <= maxPrice;
        bool passesAvailability = !availableOnly || car.isAvailable();
        if (passesType && passesRating && passesPrice && passesAvailability) { filtered.push_back(car); }
    }
    return filtered;
}

vector<Car> CarManager::sortCars(const string& sortBy, bool ascending) {
    vector<Car> sortedCars = cars; // Create a copy to sort
    sort(sortedCars.begin(), sortedCars.end(), [&](const Car& a, const Car& b) {
        if (sortBy == "name") { 
            return ascending ? (a.getName() < b.getName()) : (a.getName() > b.getName()); 
        }
        else if (sortBy == "price") { 
            return ascending ? (a.getPrice() < b.getPrice()) : (a.getPrice() > b.getPrice()); 
        }
        else if (sortBy == "rating") { 
            return ascending ? (a.getRating() < b.getRating()) : (a.getRating() > b.getRating()); 
        }
        return false; // Default return if sortBy is not recognized
    });
    return sortedCars;
}