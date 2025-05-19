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


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProfileTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserTask userTask;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        session.setAttribute("user", "test@user.cz");
    }

    @Test
    void testGetEmailWhenLoggedIn() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/email")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string("test@user.cz"));
    }

    @Test
    void testGetEmailWhenNotLoggedIn() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/email"))
                .andExpect(status().isOk())
                .andExpect(content().string("Nepřihlášen"));
    }

    @Test
    void testGetAllProfilesWhenLoggedIn() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/all-profiles")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['test@user.cz']").exists());
    }


    @Test
    void testGetAllProfilesWhenNotLoggedIn() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/all-profiles"))
                .andExpect(status().isOk())
                .andExpect(content().string("{}"));
    }
}