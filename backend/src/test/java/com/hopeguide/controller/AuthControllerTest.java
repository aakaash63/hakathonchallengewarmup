package com.hopeguide.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hopeguide.config.JwtFilter;
import com.hopeguide.config.JwtService;
import com.hopeguide.config.SecurityConfig;
import com.hopeguide.dto.AuthRequest;
import com.hopeguide.dto.SignupRequest;
import com.hopeguide.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtFilter.class})
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private JwtService jwtService;

    private static final Map<String, Object> AUTH_RESPONSE = Map.of(
        "token", "mock.jwt.token",
        "id", 1L,
        "name", "Jordan Lee",
        "email", "user@hopeguide.com",
        "role", "INDIVIDUAL",
        "onboardingComplete", false
    );

    // ─── HEALTH ENDPOINT ─────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/auth/health - returns 200 with health message")
    void health_returns200() throws Exception {
        mockMvc.perform(get("/api/auth/health"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("HopeGuide")));
    }

    // ─── SIGNUP ENDPOINT ─────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/signup - success returns 200 with token")
    void signup_validRequest_returns200() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setName("Jordan Lee");
        req.setEmail("newuser@hopeguide.com");
        req.setPassword("Demo@123");
        req.setRole("INDIVIDUAL");

        when(authService.signup(any(SignupRequest.class))).thenReturn(AUTH_RESPONSE);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("mock.jwt.token"))
            .andExpect(jsonPath("$.role").value("INDIVIDUAL"));
    }

    @Test
    @DisplayName("POST /api/auth/signup - missing email returns 400")
    void signup_missingEmail_returns400() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setName("Jordan Lee");
        req.setPassword("Demo@123");
        // email intentionally missing

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/signup - invalid email format returns 400")
    void signup_invalidEmailFormat_returns400() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setName("Test");
        req.setEmail("not-an-email");
        req.setPassword("Demo@123");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/signup - duplicate email returns 500")
    void signup_duplicateEmail_returns500() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setName("Jordan Lee");
        req.setEmail("user@hopeguide.com");
        req.setPassword("Demo@123");

        when(authService.signup(any())).thenThrow(new RuntimeException("Email already registered"));

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isInternalServerError());
    }

    // ─── LOGIN ENDPOINT ──────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/login - valid credentials returns 200 with token")
    void login_validCredentials_returns200() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setEmail("user@hopeguide.com");
        req.setPassword("Demo@123");

        when(authService.login(any(AuthRequest.class))).thenReturn(AUTH_RESPONSE);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("mock.jwt.token"))
            .andExpect(jsonPath("$.email").value("user@hopeguide.com"))
            .andExpect(jsonPath("$.name").value("Jordan Lee"));
    }

    @Test
    @DisplayName("POST /api/auth/login - wrong password returns 500")
    void login_wrongPassword_returns500() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setEmail("user@hopeguide.com");
        req.setPassword("wrong");

        when(authService.login(any())).thenThrow(new RuntimeException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /api/auth/login - missing email returns 400")
    void login_missingEmail_returns400() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setPassword("Demo@123");
        // email missing

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }
}
