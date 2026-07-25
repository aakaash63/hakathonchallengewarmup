package com.recoverease.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recoverease.dto.LoginRequest;
import com.recoverease.dto.SignupRequest;
import com.recoverease.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "gemini.api.key=test-key",
        "spring.datasource.url=jdbc:h2:mem:testdb_exception",
        "jwt.secret=RecoverEaseAISecretKey2025SuperSecureHackathonJWTToken",
        "jwt.expiration=86400000"
})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // ─── RuntimeException → 400 ───────────────────────────────────────────────

    @Test
    void handleRuntime_emailAlreadyRegistered_returns400WithErrorBody() throws Exception {
        when(authService.signup(any(SignupRequest.class)))
                .thenThrow(new RuntimeException("Email already registered"));

        SignupRequest req = new SignupRequest();
        req.setName("Test");
        req.setEmail("dup@example.com");
        req.setPassword("password123");
        req.setRole("INDIVIDUAL");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already registered"));
    }

    @Test
    void handleRuntime_invalidCredentials_returns400WithErrorBody() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        LoginRequest req = new LoginRequest();
        req.setEmail("john@example.com");
        req.setPassword("wrongpass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    // ─── Validation exception → 400 with field errors ─────────────────────────

    @Test
    void handleValidation_missingFields_returns400WithFieldErrors() throws Exception {
        // Missing required name, short password
        String body = "{\"email\": \"bad-email\", \"password\": \"ab\", \"role\": \"INDIVIDUAL\"}";

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                // At least one validation error key should be present
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    void handleValidation_invalidEmailFormat_returns400WithEmailError() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setName("Test");
        req.setEmail("not-an-email");
        req.setPassword("password123");
        req.setRole("INDIVIDUAL");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    void handleValidation_shortPassword_returns400WithPasswordError() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setName("Test");
        req.setEmail("valid@example.com");
        req.setPassword("123");  // min is 6
        req.setRole("INDIVIDUAL");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());
    }
}
