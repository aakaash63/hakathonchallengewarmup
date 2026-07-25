package com.recoverease.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recoverease.dto.ScriptRequest;
import com.recoverease.entity.GeneratedScript;
import com.recoverease.entity.User;
import com.recoverease.repository.ScriptRepository;
import com.recoverease.repository.UserRepository;
import com.recoverease.service.ScriptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import com.recoverease.TestSecurityUtils;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "gemini.api.key=test-key",
        "spring.datasource.url=jdbc:h2:mem:testdb_script",
        "jwt.secret=RecoverEaseAISecretKey2025SuperSecureHackathonJWTToken",
        "jwt.expiration=86400000"
})
class ScriptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ScriptService scriptService;

    @MockBean
    private ScriptRepository scriptRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("Sam");
        testUser.setEmail("script_ctrl_test@example.com");
        testUser.setPasswordHash("$2a$10$dummy");
        testUser.setRole("INDIVIDUAL");
        userRepository.save(testUser);
    }

    // ─── Auth guard ───────────────────────────────────────────────────────────

    @Test
    void generateScript_withoutAuth_returns401() throws Exception {
        ScriptRequest req = new ScriptRequest();
        req.setScenario("craving");
        req.setAudience("self");

        mockMvc.perform(post("/api/scripts/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getHistory_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/scripts/history"))
                .andExpect(status().isUnauthorized());
    }

    // ─── Happy path: generate ─────────────────────────────────────────────────

    @Test
    void generateScript_validRequest_returns200WithTitleAndScript() throws Exception {
        when(scriptService.generateScript(any(ScriptRequest.class), any(User.class)))
                .thenReturn(Map.of(
                        "title", "Craving Script for Self",
                        "script", "I acknowledge this craving and know it will pass."
                ));

        ScriptRequest req = new ScriptRequest();
        req.setScenario("craving");
        req.setAudience("self");

        mockMvc.perform(post("/api/scripts/generate")
                        .with(TestSecurityUtils.asUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Craving Script for Self"))
                .andExpect(jsonPath("$.script").value("I acknowledge this craving and know it will pass."));
    }

    // ─── Validation: generate ─────────────────────────────────────────────────

    @Test
    void generateScript_missingScenario_returns400() throws Exception {
        String body = "{\"audience\": \"self\"}";  // scenario is @NotBlank

        mockMvc.perform(post("/api/scripts/generate")
                        .with(TestSecurityUtils.asUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateScript_missingAudience_returns400() throws Exception {
        String body = "{\"scenario\": \"craving\"}";  // audience is @NotBlank

        mockMvc.perform(post("/api/scripts/generate")
                        .with(TestSecurityUtils.asUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateScript_blankScenario_returns400() throws Exception {
        String body = "{\"scenario\": \"   \", \"audience\": \"self\"}";

        mockMvc.perform(post("/api/scripts/generate")
                        .with(TestSecurityUtils.asUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ─── History ──────────────────────────────────────────────────────────────

    @Test
    void getHistory_withAuth_returns200WithScriptList() throws Exception {
        GeneratedScript gs = new GeneratedScript();
        gs.setUserId(testUser.getId());
        gs.setScenario("craving");
        gs.setAudience("self");
        gs.setScriptText("I can get through this.");

        when(scriptRepository.findByUserIdOrderByCreatedAtDesc(anyLong())).thenReturn(List.of(gs));

        mockMvc.perform(get("/api/scripts/history")
                        .with(TestSecurityUtils.asUser(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].scenario").value("craving"))
                .andExpect(jsonPath("$[0].audience").value("self"));
    }

    @Test
    void getHistory_emptyHistory_returns200WithEmptyList() throws Exception {
        when(scriptRepository.findByUserIdOrderByCreatedAtDesc(anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/api/scripts/history")
                        .with(TestSecurityUtils.asUser(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
