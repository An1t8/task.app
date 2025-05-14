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


@RestController
@RequestMapping("/api")
public class LoginController {

    private final Map<String, String> users = new HashMap<>();

    public LoginController() {
        loadUsers();
    }

    private void loadUsers() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("users.txt");
            if (inputStream == null) {
                System.err.println("Soubor users.txt nebyl nalezen!");
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
                        System.err.println("Chybný řádek: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Chyba při čtení users.txt: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username,
                                        @RequestParam String password,
                                        HttpSession session) {
        if (!isValidEmail(username)) {
            return ResponseEntity.badRequest().body("Neaplatný email!");
        }
        String storedPassword = users.get(username);
        if (storedPassword != null && storedPassword.equals(password)) {
            session.setAttribute("user", username);
            return ResponseEntity.ok("OK");
        } else {
            return ResponseEntity.status(401).body("Nesprávné údaje");
        }
    }

    @GetMapping("/logout")
    public void logout(HttpSession session) {
        session.invalidate();
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+[@A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }
}
