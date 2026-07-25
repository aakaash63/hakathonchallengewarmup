package com.hopeguide.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hopeguide.config.JwtFilter;
import com.hopeguide.config.JwtService;
import com.hopeguide.config.SecurityConfig;
import com.hopeguide.dto.InterventionRequest;
import com.hopeguide.dto.InterventionResponse;
import com.hopeguide.service.InterventionService;
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

@WebMvcTest(InterventionController.class)
@Import({SecurityConfig.class, JwtFilter.class})
@DisplayName("InterventionController Tests")
class InterventionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private InterventionService interventionService;
    @MockBean private JwtService jwtService;

    private InterventionResponse buildMockResponse(String urgency, boolean escalate) {
        return InterventionResponse.builder()
            .urgencyLevel(urgency)
            .message("You are not alone. Focus on the next two minutes.")
            .steps(List.of("Breathe slowly", "Move to a safe space", "Call your support"))
            .script("I need support right now. Please stay with me.")
            .ttsText("You are not alone. Focus on the next two minutes.")
            .escalate(escalate)
            .contactName("Alex")
            .contactPhone("9876543210")
            .build();
    }

    // ─── POST /api/intervention/respond ─────────────────────────────────────

    @Test
    @WithMockUser(username = "user@hopeguide.com")
    @DisplayName("POST /api/intervention/respond - craving input returns MEDIUM response")
    void respond_cravingInput_returnsMediumResponse() throws Exception {
        InterventionRequest req = new InterventionRequest();
        req.setInputText("I am having a strong craving right now");
        req.setMode("voice");

        when(interventionService.process(any(), anyString()))
            .thenReturn(buildMockResponse("MEDIUM", false));

        mockMvc.perform(post("/api/intervention/respond")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.urgencyLevel").value("MEDIUM"))
            .andExpect(jsonPath("$.escalate").value(false))
            .andExpect(jsonPath("$.steps").isArray())
            .andExpect(jsonPath("$.steps.length()").value(3));
    }

    @Test
    @WithMockUser(username = "user@hopeguide.com")
    @DisplayName("POST /api/intervention/respond - overdose keyword returns HIGH + escalate")
    void respond_overdoseKeyword_returnsHighEscalate() throws Exception {
        InterventionRequest req = new InterventionRequest();
        req.setInputText("I think I overdosed");
        req.setMode("text");

        when(interventionService.process(any(), anyString()))
            .thenReturn(buildMockResponse("HIGH", true));

        mockMvc.perform(post("/api/intervention/respond")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.urgencyLevel").value("HIGH"))
            .andExpect(jsonPath("$.escalate").value(true));
    }

    @Test
    @DisplayName("POST /api/intervention/respond - unauthenticated returns 403")
    void respond_unauthenticated_returns403() throws Exception {
        InterventionRequest req = new InterventionRequest();
        req.setInputText("I need help");

        mockMvc.perform(post("/api/intervention/respond")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@hopeguide.com")
    @DisplayName("POST /api/intervention/respond - response includes contact info")
    void respond_validInput_includesContactInfo() throws Exception {
        InterventionRequest req = new InterventionRequest();
        req.setInputText("Feeling really low today");
        req.setMode("voice");

        when(interventionService.process(any(), anyString()))
            .thenReturn(buildMockResponse("LOW", false));

        mockMvc.perform(post("/api/intervention/respond")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.contactName").value("Alex"))
            .andExpect(jsonPath("$.contactPhone").value("9876543210"));
    }

    @Test
    @WithMockUser(username = "user@hopeguide.com")
    @DisplayName("POST /api/intervention/respond - response has ttsText")
    void respond_validInput_hasTtsText() throws Exception {
        InterventionRequest req = new InterventionRequest();
        req.setInputText("I am anxious");

        when(interventionService.process(any(), anyString()))
            .thenReturn(buildMockResponse("LOW", false));

        mockMvc.perform(post("/api/intervention/respond")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ttsText").isNotEmpty());
    }
}
