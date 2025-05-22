package com.example.taskapp.controller;

import com.example.taskapp.storage.UserTask;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * REST Controller responsible for managing the main application functionality.
 * Handles task operations, user profile data, and task completion tracking.
 * All operations require user authentication through HTTP session.
 */
@RestController
@RequestMapping("/api")
public class MainController {

    private final UserTask taskStorage;

    /**
     * Constructor-based dependency injection for UserTask service.
     * @param taskStorage UserTask service for managing task data
     */
    public MainController(UserTask taskStorage) {
        this.taskStorage = taskStorage;
    }


    /**
     * Returns a predefined list of available tasks that users can complete.
     * This is a static list of household/daily tasks.
     * @return List of available task names
     */
    @GetMapping("/tasks")
    public List<String> getTasks() {
        return List.of("Udělat myčku", "Prádlo", "Vynést koš", "Umýt zem", "Jít se psem");
    }


    /**
     * Marks a task as completed for the currently logged-in user.
     * The task completion is recorded with the current date.
     * @param task Name of the task to mark as completed
     * @param session HTTP session containing user authentication information
     */
    @PostMapping("/complete")
    public void completeTask(@RequestParam String task, HttpSession session) {
        String user = (String) session.getAttribute("user");
        if (user != null) {
            taskStorage.addTask(user, task);
        }
    }

    /**
     * Retrieves all tasks completed by the current user today.
     * @param session HTTP session containing user authentication information
     * @return List of task names completed today, or empty list if not authenticated
     */
    @GetMapping("/profile")
    public List<String> profileTasks(HttpSession session) {
        String user = (String) session.getAttribute("user");
        return user != null ? taskStorage.getTasksForToday(user) : List.of();
    }

    /**
     * Returns the email of the currently logged-in user.
     * If no user is authenticated, returns 401 Unauthorized.
     * @param session HTTP session containing user information
     * @return ResponseEntity with email or 401 status
     */
    @GetMapping("/email")
    public ResponseEntity<String> getEmail(HttpSession session) {
        String user = (String) session.getAttribute("user");
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(401).body("Not logged in");
        }
    }

    /**
     * Retrieves task completion data for all users in the system.
     * Tasks are returned with completion dates.
     * @param session HTTP session containing user authentication information
     * @return ResponseEntity with map or 401 Unauthorized if not authenticated
     */
    @GetMapping("/all-profiles")
    public ResponseEntity<Map<String, List<String>>> getAllProfiles(HttpSession session) {
        String user = (String) session.getAttribute("user");
        if (user != null) {
            return ResponseEntity.ok(taskStorage.getAllUserTasks());
        } else {
            return ResponseEntity.status(401).build();
        }
    }

    /**
     * Retrieves all tasks ever completed by the current user.
     * Returns tasks with their completion dates for historical view.
     * @param session HTTP session containing user authentication information
     * @return List of all completed tasks with completion dates, or empty list if not authenticated
     */
    @GetMapping("/profile/all-tasks")
    public List<String> getAllProfileTasks(HttpSession session) {
        String user = (String) session.getAttribute("user");
        return user != null ? taskStorage.getAllTasksForUser(user) : List.of();
    }


    /**
     * Undoes the last completed task for the logged-in user.
     * Returns 400 Bad Request if no user is authenticated or undo fails.
     * @param session HTTP session containing user information
     * @return ResponseEntity 200 OK on success, 400 Bad Request otherwise
     */
    @PostMapping("/undo-last")
    public ResponseEntity<Void> undoLastTask(HttpSession session) {
        String user = (String) session.getAttribute("user");
        if (user != null && taskStorage.removeLastTask(user)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }


}
