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

//https://www.youtube.com/watch?v=BZBFw6fBeIU&list=PL82C6-O4XrHcg8sNwpoDDhcxUCbFy855E&index=8

@WebMvcTest(controllers = LoginController.class)
@ExtendWith(MockitoExtension.class)
public class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    void testSuccessfulLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                        .param("username", "tomeckova.alena@gmail.com")
                        .param("password", "alena1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("OK"))
                .andExpect(MockMvcResultMatchers.request().sessionAttribute("user", "tomeckova.alena@gmail.com"));

    }

    @Test
    void testWrongPassword() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                        .param("username", "tomeckova.alena@gmail.com")
                        .param("password", "spatneHeslo")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().string("Nesprávné údaje"))
                .andExpect(MockMvcResultMatchers.request().sessionAttributeDoesNotExist("user"));
    }

    @Test
    void testUserNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                        .param("username", "neexistujici.uzivatel@gmail.com")
                        .param("password", "heslo")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().string("Nesprávné údaje"))
                .andExpect(MockMvcResultMatchers.request().sessionAttributeDoesNotExist("user"));
    }

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



