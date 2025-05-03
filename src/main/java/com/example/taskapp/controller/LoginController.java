package com.example.taskapp.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;


@RestController
public class LoginController {

    private static final Map<String, String> USERS = Map.of(
            "tomeckova.alena@gmail.com", "alena1",
            "miroslav.tomecek@gmail.com", "miro3",
            "sameltomecek10@gmail.com", "samy7",
            "karolina.tomeckova@gamil.com", "kaja2",
            "anee.tomeckova@gmail.com", "anee10"
    );

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username,
                                        @RequestParam String password,
                                        HttpSession session) {
        String correct = USERS.get(username);

        if (correct != null && correct.equals(password)) {
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
}