package com.hopeguide.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hopeguide.config.JwtFilter;
import com.hopeguide.config.JwtService;
import com.hopeguide.config.SecurityConfig;
import com.hopeguide.dto.MoodLogRequest;
import com.hopeguide.entity.MoodLog;
import com.hopeguide.service.MoodLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MoodLogController.class)
@Import({SecurityConfig.class, JwtFilter.class})
@DisplayName("MoodLogController Tests")
class MoodLogControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private MoodLogService moodLogService;
    @MockBean private JwtService jwtService;

    private MoodLog buildMockLog(String mood, String riskLevel) {
        MoodLog log = new MoodLog();
        log.setMood(mood);
        log.setRiskLevel(riskLevel);
        log.setAiSummary("You are taking a brave step by checking in today.");
        log.setSuggestions("Take a walk|Call your support|Practice breathing");
        log.setUrgencyLevel(5);
        log.setStressLevel(5);
        log.setSleepQuality(3);
        return log;
    }

    // ─── POST /api/moodlogs ──────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "user@hopeguide.com")
    @DisplayName("POST /api/moodlogs - valid check-in returns 200")
    void logMood_validRequest_returns200() throws Exception {
        MoodLogRequest req = new MoodLogRequest();
        req.setMood("anxious");
        req.setUrgencyLevel(7);
        req.setStressLevel(6);
        req.setSleepQuality(3);
        req.setVoiceNote("Feeling rough today");

        when(moodLogService.logMood(any(), anyString()))
            .thenReturn(buildMockLog("anxious", "MEDIUM"));

        mockMvc.perform(post("/api/moodlogs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mood").value("anxious"))
            .andExpect(jsonPath("$.riskLevel").value("MEDIUM"))
            .andExpect(jsonPath("$.aiSummary").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/moodlogs - unauthenticated returns 403")
    void logMood_unauthenticated_returns403() throws Exception {
        MoodLogRequest req = new MoodLogRequest();
        req.setMood("sad");
        req.setUrgencyLevel(5);

        mockMvc.perform(post("/api/moodlogs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@hopeguide.com")
    @DisplayName("POST /api/moodlogs - HIGH risk log is returned correctly")
    void logMood_highRisk_returnsHighRiskLog() throws Exception {
        MoodLogRequest req = new MoodLogRequest();
        req.setMood("overwhelmed");
        req.setUrgencyLevel(9);
        req.setStressLevel(9);
        req.setSleepQuality(1);
        req.setVoiceNote("Thinking about relapse");

        when(moodLogService.logMood(any(), anyString()))
            .thenReturn(buildMockLog("overwhelmed", "HIGH"));

        mockMvc.perform(post("/api/moodlogs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.riskLevel").value("HIGH"));
    }

    // ─── GET /api/moodlogs/history ───────────────────────────────────────────

    @Test
    @WithMockUser(username = "user@hopeguide.com")
    @DisplayName("GET /api/moodlogs/history - returns list of logs")
    void getHistory_returns200WithList() throws Exception {
        List<MoodLog> mockLogs = List.of(
            buildMockLog("calm", "LOW"),
            buildMockLog("anxious", "MEDIUM"),
            buildMockLog("hopeful", "LOW")
        );

        when(moodLogService.getHistory(anyString())).thenReturn(mockLogs);

        mockMvc.perform(get("/api/moodlogs/history"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].mood").value("calm"))
            .andExpect(jsonPath("$[1].mood").value("anxious"));
    }

    @Test
    @WithMockUser(username = "user@hopeguide.com")
    @DisplayName("GET /api/moodlogs/history - returns empty list when no history")
    void getHistory_emptyHistory_returnsEmptyList() throws Exception {
        when(moodLogService.getHistory(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/api/moodlogs/history"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/moodlogs/history - unauthenticated returns 403")
    void getHistory_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/moodlogs/history"))
            .andExpect(status().isForbidden());
    }
}
