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

@Component
public class UserTask {

    private static final String USER_TASKS_DIRECTORY = "tasks";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy_MM");

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

    private String getFileName() {
        LocalDate now = LocalDate.now();
        return USER_TASKS_DIRECTORY + File.separator + "tasks_" + MONTH_FORMATTER.format(now) + ".json";
    }

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

    private void saveTasks(JSONArray tasks) {
        String fileName = getFileName();
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(tasks.toString(2));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

}


