package com.example.taskapp.storage;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UserTask {

    private final Map<String, List<String>> completedTasks = new HashMap<>();


    public void addTask(String user, String task) {
        List<String> seznam = completedTasks.get(user);

        if (seznam == null) {
            seznam = new ArrayList<>();
            completedTasks.put(user, seznam);
        }
        seznam.add(task);
    }


    public List<String> getTasksForToday(String user) {
        List<String> seznam = completedTasks.get(user);

        if (seznam == null) {
            return new ArrayList<>();
        } else {
            return seznam;
        }
    }


}
