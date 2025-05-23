package com.example.taskapp;

import com.example.taskapp.controller.LoginController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * Unit tests for the login functionality in the TaskApp application.
 * Tests the login and logout functionality, as well as handling of incorrect user credentials.
 */
@WebMvcTest(controllers = LoginController.class)
@ExtendWith(MockitoExtension.class)
public class LoginControllerTest {


    @Autowired
    private MockMvc mockMvc;


    /**
     * Test case for a successful login with correct username and password.
     */
    @Test
    void testSuccessfulLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                        .param("username", "tomeckova.alena@gmail.com")
                        .param("password", "alena1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Login successful."));

    }

    /**
     * Test case for logging in with an incorrect password.
     */
    @Test
    void testWrongPassword() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                        .param("username", "tomeckova.alena@gmail.com")
                        .param("password", "spatneHeslo")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().string("Invalid credentials"));

    }

        /**
         * Test case for trying to log in with a non-existent user.
         */
    @Test
    void testUserNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                        .param("username", "neexistujici.uzivatel@gmail.com")
                        .param("password", "heslo")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().string("Invalid credentials"));
    }

    /**
     * Test case for logging out a logged-in user.
     */
    @Test
    void testLogout() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", "tomeckova.alena@gmail.com");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/logout")
                        .session(session))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.request().sessionAttributeDoesNotExist("user"));
    }
}



