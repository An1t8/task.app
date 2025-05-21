package com.example.taskapp;

import com.example.taskapp.storage.UserTask;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TasksTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserTask userTask;

    private MockHttpSession session;

    private String getTestFileName() {
        DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy_MM");
        return "tasks" + java.io.File.separator + "tasks_" + MONTH_FORMATTER.format(LocalDate.now()) + ".json";
    }
    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        session.setAttribute("user", "test@user.cz");
        try {
            Path filePath = Paths.get(getTestFileName());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                System.out.println("Cleaned up file: " + filePath);
            }
        } catch (IOException e) {
            System.err.println("Failed to clean up test file: " + e.getMessage());
        }
    }

    @Test
    void testGetAvailableTasks() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/tasks"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasItems(
                        "Udělat myčku", "Prádlo", "Vynést koš", "Umýt zem", "Jít se psem")));
    }

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

    @Test
    void testCompleteTaskWhenNotLoggedIn() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/complete")
                        .param("task", "Prádlo")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(MockMvcResultMatchers.status().isOk());

        List<String> tasks = userTask.getTasksForToday(null);
        assertThat(tasks, not(hasItem("Prádlo")));
    }

    @Test
    void testGetProfileTasksWhenLoggedIn() throws Exception {
        userTask.addTask("test@user.cz", "Udělat myčku");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/profile")
                        .session(session))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasItem("Udělat myčku")));
    }

    @Test
    void testGetProfileTasksWhenNotLoggedIn() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/profile"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", empty()));
    }

    @Test
    void testGetAllTasksForUserWhenLoggedIn()  {
        userTask.addTask("test@user.cz", "Udělat myčku");
        userTask.addTask("test@user.cz", "Prádlo");
        userTask.addTask("another@user.cz", "Prádlo");
        List<String> allTasksForUser = userTask.getAllTasksForUser("test@user.cz");

        assertThat(allTasksForUser, hasSize(2));
        assertThat(allTasksForUser, hasItem("Udělat myčku (splněno: " + java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd").format(java.time.LocalDate.now()) + ")"));
        assertThat(allTasksForUser, hasItem("Prádlo (splněno: " + java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd").format(java.time.LocalDate.now()) + ")"));
        assertThat(allTasksForUser, not(hasItem("Prádlo (splněno: " + java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd").format(java.time.LocalDate.now()) + ")")));
    }

}

