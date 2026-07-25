package com.recoverease.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recoverease.TestSecurityUtils;
import com.recoverease.dto.CrisisRequest;
import com.recoverease.dto.CrisisResponseDto;
import com.recoverease.entity.User;
import com.recoverease.repository.UserRepository;
import com.recoverease.service.CrisisService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestPropertySource(properties = {
        "gemini.api.key=test-key",
        "spring.datasource.url=jdbc:h2:mem:testdb_crisis;DB_CLOSE_DELAY=-1",
        "jwt.secret=RecoverEaseAISecretKey2025SuperSecureHackathonJWTToken",
        "jwt.expiration=86400000"
})
class CrisisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CrisisService crisisService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.findAll().stream()
                .filter(u -> "crisis_ctrl_test@example.com".equals(u.getEmail()))
                .findFirst()
                .ifPresentOrElse(
                        u -> testUser = u,
                        () -> {
                            testUser = new User();
                            testUser.setName("Alex");
                            testUser.setEmail("crisis_ctrl_test@example.com");
                            testUser.setPasswordHash("$2a$10$dummy");
                            testUser.setRole("INDIVIDUAL");
                            testUser = userRepository.save(testUser);
                        }
                );
    }

    @Test
    void respond_withoutAuth_returns401() throws Exception {
        CrisisRequest req = new CrisisRequest();
        req.setInputText("I need help");

        mockMvc.perform(post("/api/crisis/respond")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void respond_validRequest_returns200WithDto() throws Exception {
        CrisisResponseDto dto = new CrisisResponseDto();
        dto.setRiskLevel("MEDIUM");
        dto.setMessage("You are not alone. Take one breath at a time.");
        dto.setSteps(List.of("Breathe slowly", "Call your contact"));
        dto.setScript("I need help right now.");
        dto.setEscalate(false);
        dto.setContactName("Mom");
        dto.setContactPhone("555-1234");
        dto.setTtsText("You are not alone. Take one breath at a time.");

        when(crisisService.respond(any(CrisisRequest.class), any(User.class))).thenReturn(dto);

        CrisisRequest req = new CrisisRequest();
        req.setInputText("I am feeling overwhelmed");

        mockMvc.perform(post("/api/crisis/respond")
                        .with(TestSecurityUtils.asUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riskLevel").value("MEDIUM"))
                .andExpect(jsonPath("$.message").value("You are not alone. Take one breath at a time."))
                .andExpect(jsonPath("$.escalate").value(false))
                .andExpect(jsonPath("$.contactName").value("Mom"))
                .andExpect(jsonPath("$.ttsText").value("You are not alone. Take one breath at a time."));
    }

    @Test
    void respond_highRiskEscalation_returns200WithEscalateTrue() throws Exception {
        CrisisResponseDto dto = new CrisisResponseDto();
        dto.setRiskLevel("HIGH");
        dto.setMessage("Call 911 immediately.");
        dto.setSteps(List.of("Call 911 now"));
        dto.setScript("Emergency.");
        dto.setEscalate(true);

        when(crisisService.respond(any(CrisisRequest.class), any(User.class))).thenReturn(dto);

        CrisisRequest req = new CrisisRequest();
        req.setInputText("I overdosed");

        mockMvc.perform(post("/api/crisis/respond")
                        .with(TestSecurityUtils.asUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riskLevel").value("HIGH"))
                .andExpect(jsonPath("$.escalate").value(true));
    }

    @Test
    void respond_missingInputText_returns400() throws Exception {
        String body = "{\"mode\": \"text\"}";

        mockMvc.perform(post("/api/crisis/respond")
                        .with(TestSecurityUtils.asUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void respond_blankInputText_returns400() throws Exception {
        String body = "{\"inputText\": \"   \"}";

        mockMvc.perform(post("/api/crisis/respond")
                        .with(TestSecurityUtils.asUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
