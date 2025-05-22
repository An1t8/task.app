package com.example.taskapp;

import com.example.taskapp.storage.UserTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the profile-related endpoints in the TaskApp application.
 * Tests functionality for retrieving user profile information, both when logged in and when not logged in.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ProfileTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserTask userTask;

    private MockHttpSession session;

    /**
     * Setup method that creates a mock session for the user before each test.
     */
    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        session.setAttribute("user", "test@user.cz");
    }

    /**
     * Test case for retrieving the email of the logged-in user.
     */
    @Test
    void testGetEmailWhenLoggedIn() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/email")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string("test@user.cz"));
    }

    /**
     * Test case for retrieving the email when no user is logged in.
     */
    @Test
    void testGetEmailWhenNotLoggedIn() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/email"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Not logged in"));
    }

    /**
     * Test case for retrieving all profiles when the user is logged in.
     */
    @Test
    void testGetAllProfilesWhenLoggedIn() throws Exception {
        Map<String, List<String>> allTasks = Map.of(
                "test@user.cz", List.of("Udělat myčku", "Vynést koš"),
                "another@user.cz", List.of("Jít se psem")
        );
        when(userTask.getAllUserTasks()).thenReturn(allTasks);

        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("user", "test@user.cz");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/all-profiles")
                        .session(mockSession))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$['another@user.cz']").isArray());

    }

    /**
     * Test case for retrieving all profiles when no user is logged in.
     */
    @Test
    void testGetAllProfilesWhenNotLoggedIn() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/all-profiles"))
                .andExpect(status().isUnauthorized());

    }
}