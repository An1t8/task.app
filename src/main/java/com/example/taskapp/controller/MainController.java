package com.example.taskapp.controller;

import com.example.taskapp.storage.UserTask;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MainController {

    private final UserTask taskStorage;


    public MainController(UserTask taskStorage) {
        this.taskStorage = taskStorage;
    }


    @GetMapping("/tasks")
    public List<String> getTasks() {
        return List.of("Udělat myčku", "Prádlo", "Vynést koš", "Umýt zem", "Jít se psem");
    }


    @PostMapping("/complete")
    public void completeTask(@RequestParam String task, HttpSession session) {
        String user = (String) session.getAttribute("user");
        if (user != null) {
            taskStorage.addTask(user, task);
        }
    }

    @GetMapping("/profile")
    public List<String> profileTasks(HttpSession session) {
        String user = (String) session.getAttribute("user");
        return user != null ? taskStorage.getTasksForToday(user) : List.of();
    }

    @GetMapping("/email")
    public String getEmail(HttpSession session) {
        String user = (String) session.getAttribute("user");
        return user != null ? user : "Nepřihlášen";
    }

    @GetMapping("/all-profiles")
    public Map<String, List<String>> getAllProfiles(HttpSession session) {
        String user = (String) session.getAttribute("user");
        if (user != null) {
            return taskStorage.getAllUserTasks();
        }
        return Map.of();
    }


}
