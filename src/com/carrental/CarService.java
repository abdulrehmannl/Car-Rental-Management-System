// src/com/carrental/CarService.java

package com.carrental;



import com.carrental.model.Car;

import com.google.gson.Gson;

import com.google.gson.GsonBuilder;

import com.google.gson.reflect.TypeToken;



import java.io.File;

import java.io.FileReader;

import java.io.FileWriter;

import java.io.IOException;

import java.io.BufferedReader;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;

import java.util.Map;

import java.util.UUID;

import java.util.concurrent.TimeUnit;

import java.lang.reflect.Type;



public class CarService {

    private static Process cppProcess;

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();



    private static final String COMMAND_FILE = "command.json";

    private static final String RESULT_FILE = "result.json";



    private static final String CPP_EXECUTABLE_PATH = "D:\\Coding\\Projects\\CarRentalSystem_New\\CarRentalSystem\\src\\cpp\\CarManager.exe";

    private static final String CPP_WORKING_DIRECTORY = "D:\\Coding\\Projects\\CarRentalSystem_New\\CarRentalSystem\\src\\cpp\\";



    static {

// Initialize moved to LoginPage's main method

// Shutdown hook moved to LoginPage's main method

    }



    public static void initialize() {

        if (cppProcess != null && cppProcess.isAlive()) {

            System.out.println("C++ Car Manager process is already running.");

            return;

        }



        File cppWorkingDir = new File(CPP_WORKING_DIRECTORY);

        if (!cppWorkingDir.exists()) {

            System.err.println("Error: C++ working directory not found: " + cppWorkingDir.getAbsolutePath());

            System.exit(1);

        }



        try {

            ProcessBuilder pb = new ProcessBuilder(CPP_EXECUTABLE_PATH);

            pb.directory(cppWorkingDir);

            pb.inheritIO();



            cppProcess = pb.start();

            System.out.println("C++ Car Manager process started successfully.");



// Ensure command and result files are empty/valid JSON for the first run

            writeJsonToFile(COMMAND_FILE, "{}");

            writeJsonToFile(RESULT_FILE, "{}");



        } catch (IOException e) {

            System.err.println("Failed to start C++ Car Manager process: " + e.getMessage());

            e.printStackTrace();

            System.exit(1);

        }

    }



    public static void shutdown() {

        if (cppProcess != null && cppProcess.isAlive()) {

            System.out.println("Terminating C++ Car Manager process...");

            cppProcess.destroy(); // Request graceful termination

            try {

                if (!cppProcess.waitFor(5, TimeUnit.SECONDS)) { // Wait up to 5 seconds

                    System.err.println("C++ process did not terminate gracefully, forcing shutdown.");

                    cppProcess.destroyForcibly(); // Force kill if not terminated

                }

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt(); // Restore interrupted status

                System.err.println("Interrupted while waiting for C++ process to terminate.");

                cppProcess.destroyForcibly();

            } finally {

                cppProcess = null; // Clear the process reference

            }

        }

        System.out.println("C++ Car Manager process shut down.");

    }



    private static void writeJsonToFile(String filename, String jsonContent) throws IOException {

        try (FileWriter file = new FileWriter(new File(CPP_WORKING_DIRECTORY, filename))) { // Use working directory

            file.write(jsonContent);

            file.flush();

        }

    }



    private static String readJsonFromFile(String filename) throws IOException {

        File file = new File(CPP_WORKING_DIRECTORY, filename); // Use working directory

        if (!file.exists() || file.length() == 0) {

            return "{}"; // Return empty JSON object if file doesn't exist or is empty

        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            StringBuilder content = new StringBuilder();

            String line;

            while ((line = reader.readLine()) != null) {

                content.append(line);

            }

            return content.toString();

        }

    }



    private static String sendCommand(Map<String, Object> commandMap) throws IOException {

        String commandId = UUID.randomUUID().toString();

        commandMap.put("id", commandId); // Assign unique command ID



// Ensure "action" field is consistently used if "command" was used previously

        if (commandMap.containsKey("command")) {

            commandMap.put("action", commandMap.remove("command"));

        }



        String jsonCommand = gson.toJson(commandMap);



// Clear result file before sending new command to prevent reading stale data

        writeJsonToFile(RESULT_FILE, "{}");



        writeJsonToFile(COMMAND_FILE, jsonCommand);

        System.out.println("Java sent command (ID: " + commandId + "): " + jsonCommand);



        long startTime = System.currentTimeMillis();

        long timeoutMillis = 10000; // 10-second timeout



        while (System.currentTimeMillis() - startTime < timeoutMillis) {

            String resultContent = readJsonFromFile(RESULT_FILE);

            if (!resultContent.isEmpty() && !resultContent.equals("{}")) {

                try {

// It's safer to check for the ID AFTER parsing.

// If JSON is malformed, it won't parse, so the try-catch is correct.

                    Map<String, Object> responseMap = gson.fromJson(resultContent, new TypeToken<Map<String, Object>>(){}.getType());

                    if (responseMap != null && commandId.equals(responseMap.get("id"))) {

                        System.out.println("Java received response (ID: " + commandId + "): " + resultContent);

// Clear command file after successful response to signal C++

                        writeJsonToFile(COMMAND_FILE, "{}");

                        return resultContent;

                    }

                } catch (Exception e) {

                    System.err.println("Warning: Incomplete or invalid JSON in result.json. Retrying... " + e.getMessage());

// Do not break here, continue waiting/retrying

                }

            }

            try {

                TimeUnit.MILLISECONDS.sleep(100); // Poll every 100ms

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                throw new IOException("Interrupted while waiting for C++ response.", e);

            }

        }



        System.err.println("Timeout: No response received from C++ for command ID: " + commandId);

        writeJsonToFile(COMMAND_FILE, "{}"); // Clear command file even on timeout

        return null;

    }



    @SuppressWarnings("unchecked")

    private static <T> List<T> parseResponseData(String responseJson, Type typeOfList) {

        if (responseJson == null) return new ArrayList<>();

        Map<String, Object> responseMap = gson.fromJson(responseJson, new TypeToken<Map<String, Object>>(){}.getType());



        if ("success".equals(responseMap.get("status"))) {

            Object data = responseMap.get("data");

            if (data == null) {

                return new ArrayList<>();

            }



            List<T> parsedList = gson.fromJson(gson.toJson(data), typeOfList);



// --- IMPORTANT FIX: Correct image paths for Car objects ---

            if (typeOfList.equals(new TypeToken<List<Car>>() {}.getType())) {

                List<Car> cars = (List<Car>) parsedList; // Cast to List<Car>

                for (Car car : cars) {

                    String currentPath = car.getImagePath();

                    if (currentPath != null && currentPath.startsWith("src/Images/")) {

// Replace "src/Images/" with "/Images/" for classpath loading

                        car.setImagePath(currentPath.replace("src/Images/", "/Images/"));

                    }

                }

                return (List<T>) cars; // Return the list with corrected paths

            }

// --- END IMPORTANT FIX ---



            return parsedList; // Return as is for other types of lists

        } else {

            System.err.println("C++ Error response: " + responseMap.get("message"));

            return new ArrayList<>();

        }

    }



    private static boolean checkStatus(String responseJson) {

        if (responseJson == null) return false;

        Map<String, Object> responseMap = gson.fromJson(responseJson, new TypeToken<Map<String, Object>>(){}.getType());

        if ("success".equals(responseMap.get("status"))) {

            return true;

        } else {

            System.err.println("C++ Operation failed: " + responseMap.get("message"));

            return false;

        }

    }



// --- Public methods for Car operations ---



    public static List<Car> getAllCars() {

        Map<String, Object> command = new HashMap<>();

        command.put("action", "GET_ALL_CARS");

        try {

            String response = sendCommand(command);

            Type carListType = new TypeToken<List<Car>>() {}.getType();

            return parseResponseData(response, carListType);

        } catch (IOException e) {

            System.err.println("Error getting all cars: " + e.getMessage());

            return new ArrayList<>();

        }

    }



    public static boolean addCar(Car car) {

        Map<String, Object> command = new HashMap<>();

        command.put("action", "ADD_CAR");

// Ensure that when adding a car, its imagePath is correct before sending to C++

// If the Java GUI allows setting paths, you might want to convert them here too

// For now, C++ is the source of truth, so we only convert when receiving.

        command.put("car", gson.fromJson(gson.toJson(car), new TypeToken<Map<String, Object>>(){}.getType()));

        try {

            String response = sendCommand(command);

            return checkStatus(response);

        } catch (IOException e) {

            System.err.println("Error adding car: " + e.getMessage());

            return false;

        }

    }



    public static boolean updateCar(Car car) {

        Map<String, Object> command = new HashMap<>();

        command.put("action", "UPDATE_CAR");

// Same note as addCar regarding path conversion before sending

        command.put("car", gson.fromJson(gson.toJson(car), new TypeToken<Map<String, Object>>(){}.getType()));

        try {

            String response = sendCommand(command);

            return checkStatus(response);

        } catch (IOException e) {

            System.err.println("Error updating car: " + e.getMessage());

            return false;

        }

    }



    public static boolean deleteCar(String carName) {

        Map<String, Object> command = new HashMap<>();

        command.put("action", "DELETE_CAR");

        command.put("carName", carName);

        try {

            String response = sendCommand(command);

            return checkStatus(response);

        } catch (IOException e) {

            System.err.println("Error deleting car: " + e.getMessage());

            return false;

        }

    }



    public static List<Car> searchCars(String query, String searchField) {

        Map<String, Object> command = new HashMap<>();

        command.put("action", "SEARCH_CARS");

        command.put("query", query);

        command.put("searchField", searchField);

        try {

            String response = sendCommand(command);

            Type carListType = new TypeToken<List<Car>>() {}.getType();

            return parseResponseData(response, carListType); // Path correction happens here

        } catch (IOException e) {

            System.err.println("Error searching cars: " + e.getMessage());

            return new ArrayList<>();

        }

    }



    public static List<Car> filterCars(String typeFilter, double minRating, double maxPrice, boolean availableOnly) {

        Map<String, Object> command = new HashMap<>();

        command.put("action", "FILTER_CARS");

        command.put("typeFilter", typeFilter);

        command.put("minRating", minRating);

        command.put("maxPrice", maxPrice);

        command.put("availableOnly", availableOnly);

        try {

            String response = sendCommand(command);

            Type carListType = new TypeToken<List<Car>>() {}.getType();

            return parseResponseData(response, carListType); // Path correction happens here

        } catch (IOException e) {

            System.err.println("Error filtering cars: " + e.getMessage());

            return new ArrayList<>();

        }

    }



    public static List<Car> sortCars(String sortBy, boolean ascending) {

        Map<String, Object> command = new HashMap<>();

        command.put("action", "SORT_CARS");

        command.put("sortBy", sortBy);

        command.put("ascending", ascending);

        try {

            String response = sendCommand(command);

            Type carListType = new TypeToken<List<Car>>() {}.getType();

            return parseResponseData(response, carListType); // Path correction happens here

        } catch (IOException e) {

            System.err.println("Error sorting cars: " + e.getMessage());

            return new ArrayList<>();

        }

    }

}