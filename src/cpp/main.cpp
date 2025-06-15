// src/cpp/main.cpp

#include <iostream>
#include <fstream>       // For file operations (ifstream, ofstream)
#include <string>
#include <chrono>        // For std::chrono::seconds, milliseconds
#include <thread>        // For std::this_thread::sleep_for
#include <vector>        // For std::vector
#include "CarManager.h"  // Include your CarManager header
#include "Car.h"         // Ensure Car.h is included for Car class definition
#include "json.hpp"      // For JSON parsing/generation (nlohmann/json)

// Alias for convenience (from nlohmann/json library)
using json = nlohmann::json;
using namespace std; 

// Function to read a file's content
string readFile(const string& filename) {
    ifstream file(filename);
    if (!file.is_open()) {
        // cerr << "C++: Warning: File not found or could not be opened: " << filename << endl;
        return ""; // Return empty string if file not found or cannot be opened
    }
    string content((istreambuf_iterator<char>(file)),
                                istreambuf_iterator<char>());
    file.close();
    return content;
}

// Function to write content to a file
void writeFile(const string& filename, const string& content) {
    ofstream file(filename);
    if (file.is_open()) {
        file << content;
        file.close();
    } else {
        cerr << "C++: Error: Could not write to file: " << filename << endl;
    }
}

// Global variable to store the last processed command ID
// This helps prevent processing the same command multiple times if the file isn't cleared instantly
string lastProcessedCommandId = "";

int main() {
    CarManager manager; // Create an instance of your CarManager

    // --- Initial setup for CarManager and data loading ---
    if (!ifstream("cars_data.txt").good()) {
        cout << "C++: cars_data.txt not found. Initializing with default cars." << endl;
        // Add some default cars using the new Car constructor
        manager.addCar(Car("Toyota Camry", true, "Sedan", 4.5, "/img/camry.png", "50k km", "200 km/h", "5 seats", "Automatic", "Mid-size", 50.0, "2023-01-15"));
        manager.addCar(Car("Honda Civic", true, "Sedan", 4.2, "/img/civic.png", "30k km", "180 km/h", "5 seats", "Automatic", "Compact", 45.0, "2022-03-20"));
        manager.addCar(Car("Ford Escape", false, "SUV", 4.0, "/img/escape.png", "60k km", "190 km/h", "5 seats", "Automatic", "Compact SUV", 60.0, "2021-07-10"));
        manager.saveCarsToFile("cars_data.txt"); // Save these initial cars
    } else {
        cout << "C++: Loading cars from cars_data.txt." << endl;
        manager.loadCarsFromFile("cars_data.txt");
    }

    // Ensure command.json and result.json exist initially
    // Write empty JSON object to them
    writeFile("command.json", "{}");
    writeFile("result.json", "{}");

    cout << "C++ Backend is running. Waiting for commands in command.json..." << endl;
    cout << "Press Ctrl+C to stop." << endl;

    while (true) {
        // Read command.json
        string commandContent = readFile("command.json");
        
        json command;
        try {
            if (!commandContent.empty() && commandContent != "{}") { // Also check for empty JSON object
                command = json::parse(commandContent);
            } else {
                // If file is empty or just "{}", treat as no new command and continue.
                this_thread::sleep_for(chrono::milliseconds(500)); // Small delay for empty file
                continue; 
            }
        } catch (const json::parse_error& e) {
            cerr << "C++: JSON parsing error in command.json: " << e.what() << endl;
            // Clear the command file to avoid re-parsing the error
            writeFile("command.json", "{}"); 
            this_thread::sleep_for(chrono::milliseconds(1000));
            continue;
        }

        // Check for a unique command ID to prevent re-processing the same command
        // The Java GUI should provide a unique 'id' for each new command
        string currentCommandId = command.value("id", "");
        if (currentCommandId.empty() || currentCommandId == lastProcessedCommandId) {
            // No new command or command already processed
            this_thread::sleep_for(chrono::milliseconds(500)); 
            continue;
        }

        string action = command.value("action", ""); // Get the "action" field from JSON

        json response;
        response["id"] = currentCommandId; // Echo the command ID in the response
        response["status"] = "error";       // Default status

        if (action == "GET_ALL_CARS") {
            cout << "C++: Received GET_ALL_CARS command." << endl;
            vector<Car> cars = manager.getAllCars();
            json carArray = json::array();
            for (const auto& car : cars) { 
                carArray.push_back(car.toJson()); // Use Car::toJson() directly
            }
            response["status"] = "success";
            response["data"] = carArray;

        } else if (action == "ADD_CAR") {
            cout << "C++: Received ADD_CAR command." << endl;
            try {
                Car newCar = Car::fromJson(command["car"]); // Use Car::fromJson() directly
                bool exists = false;
                for (const auto& c : manager.getAllCars()) {
                    if (c.getName() == newCar.getName()) { // Use getter for comparison
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    manager.addCar(newCar);
                    manager.saveCarsToFile("cars_data.txt"); // Save after adding/modifying
                    response["status"] = "success";
                    response["message"] = "Car added successfully.";
                } else {
                    response["status"] = "error";
                    response["message"] = "Car with this name already exists.";
                }
            } catch (const exception& e) {
                response["status"] = "error";
                response["message"] = "Error adding car: " + string(e.what());
            }
        } 
        // --- Add more command handlers here for UPDATE_CAR, DELETE_CAR, SEARCH, FILTER, SORT ---
        else if (action == "UPDATE_CAR") {
            cout << "C++: Received UPDATE_CAR command." << endl;
            try {
                Car updatedCar = Car::fromJson(command["car"]);
                if (manager.updateCar(updatedCar)) {
                    manager.saveCarsToFile("cars_data.txt");
                    response["status"] = "success";
                    response["message"] = "Car updated successfully.";
                } else {
                    response["status"] = "error";
                    response["message"] = "Car not found for update.";
                }
            } catch (const exception& e) {
                response["status"] = "error";
                response["message"] = "Error updating car: " + string(e.what());
            }
        }
        else if (action == "DELETE_CAR") {
            cout << "C++: Received DELETE_CAR command." << endl;
            string carNameToDelete = command.value("carName", "");
            if (carNameToDelete.empty()) {
                response["status"] = "error";
                response["message"] = "Car name for deletion cannot be empty.";
            } else if (manager.deleteCar(carNameToDelete)) {
                manager.saveCarsToFile("cars_data.txt");
                response["status"] = "success";
                response["message"] = "Car deleted successfully.";
            } else {
                response["status"] = "error";
                response["message"] = "Car not found for deletion.";
            }
        }
        else if (action == "SEARCH_CARS") {
            cout << "C++: Received SEARCH_CARS command." << endl;
            string query = command.value("query", "");
            string searchField = command.value("searchField", "");
            vector<Car> results = manager.searchCars(query, searchField);
            json resultArray = json::array();
            for (const auto& car : results) { resultArray.push_back(car.toJson()); }
            response["status"] = "success";
            response["data"] = resultArray;
        }
        else if (action == "FILTER_CARS") {
            cout << "C++: Received FILTER_CARS command." << endl;
            string typeFilter = command.value("typeFilter", "");
            double minRating = command.value("minRating", 0.0);
            double maxPrice = command.value("maxPrice", 1000000.0); // Use a large default
            bool availableOnly = command.value("availableOnly", false);
            vector<Car> results = manager.filterCars(typeFilter, minRating, maxPrice, availableOnly);
            json resultArray = json::array();
            for (const auto& car : results) { resultArray.push_back(car.toJson()); }
            response["status"] = "success";
            response["data"] = resultArray;
        }
        else if (action == "SORT_CARS") {
            cout << "C++: Received SORT_CARS command." << endl;
            string sortBy = command.value("sortBy", "");
            bool ascending = command.value("ascending", true);
            vector<Car> results = manager.sortCars(sortBy, ascending);
            json resultArray = json::array();
            for (const auto& car : results) { resultArray.push_back(car.toJson()); }
            response["status"] = "success";
            response["data"] = resultArray;
        }
        else {
            cerr << "C++: Unknown command action: '" << action << "'" << endl;
            response["message"] = "Unknown action or invalid command structure.";
        }
        
        // Write the response to result.json
        writeFile("result.json", response.dump(4)); // dump(4) makes it pretty-printed JSON
        cout << "C++: Processed command '" << action << "' with ID '" << currentCommandId << "'" << endl;

        // Mark this command as processed by clearing command.json
        lastProcessedCommandId = currentCommandId;
        writeFile("command.json", "{}"); 
        
        // Small pause to prevent busy-waiting
        this_thread::sleep_for(chrono::milliseconds(100)); 
    }

    return 0;
}
