package com.carrental;
import java.util.HashMap;
import java.util.Map;

public class UserStorage {

    // In-memory storage for users
    private static final Map<String, String> users = new HashMap<>();

    // Adds a new user to the in-memory storage
    public static boolean addUser(String email, String password) {
        if (users.containsKey(email)) {
            return false; // User already exists
        }
        users.put(email, password); // Add user to the storage
        return true;
    }

    // Validates login credentials
    public static boolean validateUser(String email, String password) {
        return users.containsKey(email) && users.get(email).equals(password);
    }

    // Check if the user exists
    public static boolean isUserExist(String email) {
        return users.containsKey(email);
    }
}
