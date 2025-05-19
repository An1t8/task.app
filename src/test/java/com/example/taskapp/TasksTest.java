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

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        session.setAttribute("user", "test@user.cz");
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

}

