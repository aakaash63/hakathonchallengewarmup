package com.recoverease.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recoverease.TestSecurityUtils;
import com.recoverease.dto.CheckInRequest;
import com.recoverease.entity.CheckIn;
import com.recoverease.entity.User;
import com.recoverease.repository.UserRepository;
import com.recoverease.service.CheckInService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestPropertySource(properties = {
        "gemini.api.key=test-key",
        "spring.datasource.url=jdbc:h2:mem:testdb_checkin;DB_CLOSE_DELAY=-1",
        "jwt.secret=RecoverEaseAISecretKey2025SuperSecureHackathonJWTToken",
        "jwt.expiration=86400000"
})
class CheckInControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CheckInService checkInService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.findAll().stream()
                .filter(u -> "checkin_test@example.com".equals(u.getEmail()))
                .findFirst()
                .ifPresentOrElse(
                        u -> testUser = u,
                        () -> {
                            testUser = new User();
                            testUser.setName("Test User");
                            testUser.setEmail("checkin_test@example.com");
                            testUser.setPasswordHash("$2a$10$dummy");
                            testUser.setRole("INDIVIDUAL");
                            testUser = userRepository.save(testUser);
                        }
                );
    }

    @Test
    void submitCheckIn_withAuthentication_returns200() throws Exception {
        when(checkInService.submitCheckIn(any(CheckInRequest.class), any(User.class)))
                .thenReturn(Map.of(
                        "riskLevel", "LOW",
                        "summary", "Good check-in",
                        "suggestions", List.of("Keep going")
                ));

        CheckInRequest req = new CheckInRequest();
        req.setMood("calm");
        req.setCravingLevel(2);
        req.setStressLevel(2);
        req.setSleepQuality(8);

        mockMvc.perform(post("/api/checkins")
                        .with(TestSecurityUtils.asUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riskLevel").value("LOW"));
    }

    @Test
    void submitCheckIn_withoutAuthentication_returns401() throws Exception {
        CheckInRequest req = new CheckInRequest();
        req.setMood("calm");
        req.setCravingLevel(2);
        req.setStressLevel(2);
        req.setSleepQuality(8);

        mockMvc.perform(post("/api/checkins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void submitCheckIn_cravingOutOfRange_returns400() throws Exception {
        String body = """
                {"mood": "calm", "cravingLevel": 11, "stressLevel": 5, "sleepQuality": 7}
                """;

        mockMvc.perform(post("/api/checkins")
                        .with(TestSecurityUtils.asUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getHistory_withAuthentication_returns200() throws Exception {
        CheckIn c1 = new CheckIn();
        c1.setUserId(testUser.getId());
        c1.setMood("calm");
        c1.setCravingLevel(3);

        when(checkInService.getHistory(any())).thenReturn(List.of(c1));

        mockMvc.perform(get("/api/checkins/history")
                        .with(TestSecurityUtils.asUser(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mood").value("calm"));
    }

    @Test
    void getHistory_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/checkins/history"))
                .andExpect(status().isUnauthorized());
    }
}
