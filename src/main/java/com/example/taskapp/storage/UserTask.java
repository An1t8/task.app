package com.example.taskapp.storage;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service component responsible for managing user task storage and retrieval.
 * Handles persistent storage of completed tasks in JSON format, organized by month.
 * Each task record includes user email, completion date, and task description.
 */
@Component
public class UserTask {

    private static final String USER_TASKS_DIRECTORY = "tasks";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy_MM");

    /**
     * Constructor that ensures the tasks directory exists.
     * Creates the directory if it doesn't exist during application startup.
     */
    public UserTask() {
        Path directoryPath = Paths.get(USER_TASKS_DIRECTORY);
        if (!Files.exists(directoryPath)) {
            try {
                Files.createDirectories(directoryPath);
                System.out.println("Directory " + USER_TASKS_DIRECTORY + " was created successfully.");
            } catch (IOException e) {
                System.err.println("Failed to create directory " + e.getMessage());
            }
        }
    }

    /**
     * Generates the filename for the current month's task file.
     * Files are named in format: tasks_YYYY_MM.json
     * @return Complete file path for the current month's task file
     */
    private String getFileName() {
        LocalDate now = LocalDate.now();
        return USER_TASKS_DIRECTORY + File.separator + "tasks_" + MONTH_FORMATTER.format(now) + ".json";
    }

    /**
     * Adds a completed task for a specific user with the current date.
     * Creates a JSON object containing user email, current date, and task description,
     * then appends it to the current month's task file.
     * @param user Email address of the user who completed the task
     * @param task Description of the completed task
     */
    public void addTask(String user, String task) {
        LocalDate now = LocalDate.now();
        String date = DATE_FORMATTER.format(now);
        JSONObject taskJson = new JSONObject();
        taskJson.put("user", user);
        taskJson.put("date", date);
        taskJson.put("task", task);

        JSONArray tasks = loadTasks();
        tasks.put(taskJson);
        saveTasks(tasks);
    }

    /**
     * Saves the tasks array to the current month's JSON file.
     * Writes the JSON with proper formatting (indentation of 2 spaces).
     * @param tasks JSONArray containing all tasks to be saved
     */
    private void saveTasks(JSONArray tasks) {
        String fileName = getFileName();
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(tasks.toString(2));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads all tasks from the current month's JSON file.
     * Creates an empty JSONArray if the file doesn't exist or is empty.
     * @return JSONArray containing all tasks from the current month's file
     */
    private JSONArray loadTasks() {
        String fileName = getFileName();
        File file = new File(fileName);
        if (!file.exists()) {
            return new JSONArray();
        }
        try {
            String content = new String(Files.readAllBytes(Paths.get(fileName)));
            if (content.isEmpty()) {
                return new JSONArray();
            }
            return new JSONArray(content);
        } catch (IOException e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

    /**
     * Retrieves all tasks completed by a specific user today.
     * Filters tasks by user email and current date.
     * @param user Email address of the user
     * @return List of task descriptions completed by the user today
     */
    public List<String> getTasksForToday(String user) {
        LocalDate today = LocalDate.now();
        String todayString = DATE_FORMATTER.format(today);
        JSONArray tasks = loadTasks();
        List<String> userTasks = new ArrayList<>();
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject taskJson = tasks.getJSONObject(i);
            if (taskJson.getString("user").equals(user) && taskJson.getString("date").equals(todayString)) {
                userTasks.add(taskJson.getString("task"));
            }
        }
        return userTasks;
    }

    /**
     * Retrieves all tasks for all users from the current month.
     * Groups tasks by user email and includes completion dates.
     * Used for displaying all user profiles and their completed tasks.
     * @return Map where keys are user emails and values are lists of tasks with completion dates
     */
    public Map<String, List<String>> getAllUserTasks() {
        JSONArray tasks = loadTasks();
        Map<String, List<String>> allUserTasks = new HashMap<>();
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject taskJson = tasks.getJSONObject(i);
            String user = taskJson.getString("user");
            String task = taskJson.getString("task");
            String date = taskJson.getString("date");
            String taskWithDate = task + " (" + date + ")";
            if (!allUserTasks.containsKey(user)) {
                allUserTasks.put(user, new ArrayList<>());
            }
            allUserTasks.get(user).add(taskWithDate);
        }
        return allUserTasks;
    }

    /**
     * Retrieves all tasks ever completed by a specific user from the current month.
     * Used in user profile to show complete task history.
     * @param user Email address of the user
     * @return List of all tasks completed by the user with completion dates
     */
    public List<String> getAllTasksForUser(String user) {
        JSONArray tasks = loadTasks();
        List<String> userAllTasks = new ArrayList<>();
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject taskJson = tasks.getJSONObject(i);
            if (taskJson.getString("user").equals(user)) {
                String task = taskJson.getString("task");
                String date = taskJson.getString("date");
                userAllTasks.add(task + " (splněno: " + date + ")");
            }
        }
        return userAllTasks;
    }

    public boolean removeLastTask(String user) {
        LocalDate today = LocalDate.now();
        String todayString = DATE_FORMATTER.format(today);
        JSONArray tasks = loadTasks();
        for (int i = tasks.length() - 1; i >= 0; i--) {
            JSONObject taskJson = tasks.getJSONObject(i);
            if (taskJson.getString("user").equals(user) && taskJson.getString("date").equals(todayString)) {
                tasks.remove(i);
                saveTasks(tasks);
                return true;
            }
        }
        return false;
    }

}


