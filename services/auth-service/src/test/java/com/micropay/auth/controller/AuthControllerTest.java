package com.micropay.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.micropay.auth.dto.AuthResponse;
import com.micropay.auth.dto.LoginRequest;
import com.micropay.auth.dto.RegisterRequest;
import com.micropay.auth.service.AuthService;
import com.micropay.auth.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.micropay.auth.TestConfig.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("POST /auth/register returns 201 CREATED")
    void register_shouldReturnCreated() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("user@example.com");
        req.setPassword("password");
        req.setFirstName("Test");
        req.setLastName("User");

        AuthResponse resp = new AuthResponse("token", UUID.randomUUID(),
                req.getEmail(), req.getFirstName(), req.getLastName());

        Mockito.when(authService.register(Mockito.any(RegisterRequest.class)))
                .thenReturn(resp);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /auth/login returns 200 OK")
    void login_shouldReturnOk() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("password");

        AuthResponse resp = new AuthResponse("token", UUID.randomUUID(),
                "user@example.com", "Test", "User");

        Mockito.when(authService.login(Mockito.any(LoginRequest.class)))
                .thenReturn(resp);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}


