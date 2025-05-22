package com.example.taskapp;

import com.example.taskapp.storage.UserTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests for the tasks-related endpoints in the TaskApp application.
 * Tests various functionalities such as retrieving tasks, completing tasks, checking user profile tasks, and removing tasks.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class TasksTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserTask userTask;

    private MockHttpSession session;

    /**
     * Returns the current month's task file name.
     */
    private String getTestFileName() {
        DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy_MM");
        return "tasks" + File.separator + "tasks_" + MONTH_FORMATTER.format(LocalDate.now()) + ".json";
    }

    /**
     * Prepares the test environment before each test.
     * Only today's tasks for the test user are removed from the task file.
     */
    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        session.setAttribute("user", "test@user.cz");

        try {
            Path filePath = Paths.get(getTestFileName());
            if (Files.exists(filePath)) {
                String content = Files.readString(filePath);
                JSONArray tasks = new JSONArray(content);

                LocalDate today = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                JSONArray filtered = new JSONArray();

                for (int i = 0; i < tasks.length(); i++) {
                    JSONObject task = tasks.getJSONObject(i);
                    String user = task.optString("user");
                    String date = task.optString("date");

                    if (!user.equals("test@user.cz") || !date.equals(today.format(formatter))) {
                        filtered.put(task);
                    }
                }

                Files.writeString(filePath, filtered.toString(), StandardOpenOption.TRUNCATE_EXISTING);
                System.out.println("Removed today's tasks for test@user.cz: " + filePath);
            }
        } catch (IOException e) {
            System.err.println("Failed to update test file: " + e.getMessage());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests if the list of available tasks is returned correctly.
     */
    @Test
    void testGetAvailableTasks() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/tasks"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasItems(
                        "Udělat myčku", "Prádlo", "Vynést koš", "Umýt zem", "Jít se psem")));
    }

    /**
     * Tests adding a completed task when the user is logged in.
     */
    @Test
    void testCompleteTaskWhenLoggedIn() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/complete")
                        .param("task", "Vynést koš")
                        .session(session)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(MockMvcResultMatchers.status().isOk());

        List<String> tasks = userTask.getTasksForToday("test@user.cz");
        assertThat(tasks, hasItem("Vynést koš"));
    }

    /**
     * Tests that completing a task without login does not save the task.
     */
    @Test
    void testCompleteTaskWhenNotLoggedIn() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/complete")
                        .param("task", "Prádlo")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(MockMvcResultMatchers.status().isOk());

        List<String> tasks = userTask.getTasksForToday(null);
        assertThat(tasks, not(hasItem("Prádlo")));
    }

    /**
     * Tests that the profile endpoint returns today's tasks for a logged-in user.
     */
    @Test
    void testGetProfileTasksWhenLoggedIn() throws Exception {
        userTask.addTask("test@user.cz", "Udělat myčku");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/profile")
                        .session(session))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasItem("Udělat myčku")));
    }

    /**
     * Tests that the profile endpoint returns an empty list when not logged in.
     */
    @Test
    void testGetProfileTasksWhenNotLoggedIn() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/profile"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", empty()));
    }

    /**
     * Tests that the user gets all completed tasks for the current month.
     */
    @Test
    void testGetAllTasksForUserWhenLoggedIn() {
        userTask.addTask("test@user.cz", "Udělat myčku");
        userTask.addTask("test@user.cz", "Prádlo");
        userTask.addTask("another@user.cz", "Prádlo");

        List<String> allTasksForUser = userTask.getAllTasksForUser("test@user.cz");

        assertThat(allTasksForUser, hasSize(2));
        assertThat(allTasksForUser, hasItem("Udělat myčku (splněno: " + LocalDate.now() + ")"));
        assertThat(allTasksForUser, hasItem("Prádlo (splněno: " + LocalDate.now() + ")"));
    }

    /**
     * Tests that no tasks are returned when the user is not logged in.
     */
    @Test
    void testGetAllTasksForUserWhenNotLoggedIn() {
        List<String> allTasksForUser = userTask.getAllTasksForUser(null);
        assertThat(allTasksForUser, is(empty()));
    }

    /**
     * Tests that the last task of the day is removed correctly.
     */
    @Test
    void testRemoveLastTask() {
        userTask.addTask("test@user.cz", "Uklidit pokoj");
        userTask.addTask("test@user.cz", "Jít se psem");

        boolean removed = userTask.removeLastTask("test@user.cz");

        assertThat(removed, is(true));
        List<String> tasks = userTask.getTasksForToday("test@user.cz");
        assertThat(tasks, not(hasItem("Jít se psem")));
        assertThat(tasks, hasItem("Uklidit pokoj"));
    }

    /**
     * Tests that removing the last task returns false when there are no tasks.
     */
    @Test
    void testRemoveLastTaskWhenNoTasks() {
        boolean removed = userTask.removeLastTask("test@user.cz");
        assertThat(removed, is(false));
    }
}
