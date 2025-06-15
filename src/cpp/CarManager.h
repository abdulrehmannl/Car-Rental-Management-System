// src/cpp/CarManager.h
#ifndef CAR_MANAGER_H
#define CAR_MANAGER_H

#include "Car.h"
#include <vector>
#include <string>

class CarManager {
public:
    CarManager();
    void loadCarsFromFile(const std::string& filename);
    void saveCarsToFile(const std::string& filename);

    std::vector<Car> getAllCars();
    void addCar(const Car& car);
    bool updateCar(const Car& car);
    bool deleteCar(const std::string& name);

    std::vector<Car> searchCars(const std::string& query, const std::string& searchField);
    std::vector<Car> filterCars(const std::string& typeFilter, double minRating, double maxPrice, bool availableOnly);
    std::vector<Car> sortCars(const std::string& sortBy, bool ascending);

private:
    std::vector<Car> cars;
    const std::string CAR_STORAGE_FILE = "cars_data.txt"; // This will store C++'s data
};

#endif // CAR_MANAGER_H