package com.example.taskapp.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.HashMap;

/**
 * REST Controller responsible for handling user authentication.
 * Manages login functionality including user validation and session management.
 * User credentials are loaded from a text file (users.txt) in the resources' folder.
 */
@RestController
@RequestMapping("/api")
public class LoginController {

    /**
     * In-memory storage for user credentials loaded from users.txt file.
     * Key: username (email), Value: password
     */
    private final Map<String, String> users = new HashMap<>();


    /**
     * Constructor that initializes the controller and loads user data from file.
     */
    public LoginController() {
        loadUsers();
    }

    /**
     * Loads user credentials from the users.txt file in the resources' folder.
     * Each line in the file should contain: email,password
     * Invalid lines are logged as errors and skipped.
     */
    private void loadUsers() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("users.txt");
            if (inputStream == null) {
                System.err.println("users.txt file not found");
                return;
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        String username = parts[0].trim();
                        String password = parts[1].trim();
                        users.put(username, password);
                    } else {
                        System.err.println("Invalid line in file: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading users.txt file: " + e.getMessage());
        }
    }

    /**
     * Handles user login requests.
     * Validates email format, checks credentials against loaded users, and creates a user session if authentication is successful.
     * @param username User's email address
     * @param password User's password
     * @param session HTTP session for storing user authentication state
     * @return ResponseEntity with "OK" for successful login,
     *         400 for invalid email format, or 401 for invalid credentials
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username,
                                        @RequestParam String password,
                                        HttpSession session) {
        if (!isValidEmail(username)) {
            return ResponseEntity.badRequest().body("Invalid email format.");
        }
        String storedPassword = users.get(username);
        if (storedPassword != null && storedPassword.equals(password)) {
            session.setAttribute("user", username);
            return ResponseEntity.ok("Login successful.");
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    /**
     * Handles user logout requests.
     * Invalidates the current HTTP session, removing all session data.
     * @param session HTTP session to be invalidated
     */
    @GetMapping("/logout")
    public void logout(HttpSession session) {
        session.invalidate();
    }

    /**
     * Validates if the provided string is a valid email format.
     * Uses a basic regex pattern to check email structure.
     * @param email Email string to validate
     * @return true if email format is valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+[@A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }
}
