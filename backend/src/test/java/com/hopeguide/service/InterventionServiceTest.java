package com.hopeguide.service;

import com.hopeguide.dto.InterventionRequest;
import com.hopeguide.dto.InterventionResponse;
import com.hopeguide.entity.User;
import com.hopeguide.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InterventionService Tests")
class InterventionServiceTest {

    @Mock private GeminiService gemini;
    @Mock private UserRepository userRepo;

    @InjectMocks private InterventionService interventionService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Jordan Lee");
        mockUser.setEmail("user@hopeguide.com");
        mockUser.setTriggers("loneliness, stress");
        mockUser.setCalmingStrategies("deep breathing, walking");
        mockUser.setSupportContactName("Alex");
        mockUser.setSupportContactPhone("9876543210");
        mockUser.setPersonalMantra("One day at a time");
    }

    // ─── EMERGENCY DETECTION TESTS ───────────────────────────────────────────

    @Test
    @DisplayName("Emergency: detects 'overdose' keyword and returns HIGH urgency")
    void process_overdoseKeyword_returnsEmergencyResponse() {
        InterventionRequest req = new InterventionRequest();
        req.setInputText("I think I overdosed please help");
        req.setMode("text");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));

        InterventionResponse response = interventionService.process(req, "user@hopeguide.com");

        assertThat(response.getUrgencyLevel()).isEqualTo("HIGH");
        assertThat(response.isEscalate()).isTrue();
        assertThat(response.getSteps()).contains("Call 911 right now");
        verify(gemini, never()).generate(anyString()); // Gemini not called for emergency
    }

    @Test
    @DisplayName("Emergency: detects 'want to die' keyword")
    void process_wantToDieKeyword_returnsEmergencyResponse() {
        InterventionRequest req = new InterventionRequest();
        req.setInputText("I want to die I can't do this anymore");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));

        InterventionResponse response = interventionService.process(req, "user@hopeguide.com");

        assertThat(response.isEscalate()).isTrue();
        assertThat(response.getUrgencyLevel()).isEqualTo("HIGH");
        verify(gemini, never()).generate(anyString());
    }

    @Test
    @DisplayName("Emergency: detects 'not breathing' keyword")
    void process_notBreathingKeyword_returnsEmergencyResponse() {
        InterventionRequest req = new InterventionRequest();
        req.setInputText("She is not breathing what do I do");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));

        InterventionResponse response = interventionService.process(req, "user@hopeguide.com");

        assertThat(response.isEscalate()).isTrue();
        verify(gemini, never()).generate(anyString());
    }

    // ─── NORMAL INTERVENTION TESTS ───────────────────────────────────────────

    @Test
    @DisplayName("Normal: calls Gemini for non-emergency input")
    void process_normalInput_callsGeminiService() {
        InterventionRequest req = new InterventionRequest();
        req.setInputText("I am feeling a strong craving right now");
        req.setMode("voice");

        String geminiResponse = """
            {
              "urgencyLevel": "MEDIUM",
              "message": "You are doing so well by reaching out right now.",
              "steps": ["Move to a safe space", "Take 5 slow breaths", "Call your support person"],
              "script": "I need support right now. Please stay with me.",
              "ttsText": "You are doing so well by reaching out.",
              "escalate": false
            }
            """;

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(gemini.generate(anyString())).thenReturn(geminiResponse);

        InterventionResponse response = interventionService.process(req, "user@hopeguide.com");

        assertThat(response.getUrgencyLevel()).isEqualTo("MEDIUM");
        assertThat(response.isEscalate()).isFalse();
        assertThat(response.getSteps()).hasSize(3);
        assertThat(response.getMessage()).isNotBlank();
        verify(gemini, times(1)).generate(anyString());
    }

    @Test
    @DisplayName("Normal: includes support contact in response when profile exists")
    void process_normalInput_includesSupportContact() {
        InterventionRequest req = new InterventionRequest();
        req.setInputText("Feeling overwhelmed today");

        String geminiResponse = """
            {
              "urgencyLevel": "LOW",
              "message": "You reached out. That is the bravest step.",
              "steps": ["Breathe", "Move", "Call support"],
              "script": "I need a few minutes of support.",
              "ttsText": "You reached out. That is the bravest step.",
              "escalate": false
            }
            """;

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(gemini.generate(anyString())).thenReturn(geminiResponse);

        InterventionResponse response = interventionService.process(req, "user@hopeguide.com");

        assertThat(response.getContactName()).isEqualTo("Alex");
        assertThat(response.getContactPhone()).isEqualTo("9876543210");
    }

    @Test
    @DisplayName("Normal: returns fallback response when Gemini returns null")
    void process_geminiReturnsNull_returnsFallbackResponse() {
        InterventionRequest req = new InterventionRequest();
        req.setInputText("I feel like using again");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(gemini.generate(anyString())).thenReturn(null);

        InterventionResponse response = interventionService.process(req, "user@hopeguide.com");

        assertThat(response).isNotNull();
        assertThat(response.getUrgencyLevel()).isEqualTo("MEDIUM");
        assertThat(response.getSteps()).isNotEmpty();
        assertThat(response.getMessage()).isNotBlank();
    }

    @Test
    @DisplayName("Normal: returns fallback response when Gemini returns malformed JSON")
    void process_geminiReturnsBadJson_returnsFallbackResponse() {
        InterventionRequest req = new InterventionRequest();
        req.setInputText("I am struggling today");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(gemini.generate(anyString())).thenReturn("not valid json at all %%%");

        InterventionResponse response = interventionService.process(req, "user@hopeguide.com");

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isNotBlank();
    }

    @Test
    @DisplayName("Normal: works when user profile is missing")
    void process_userNotFound_stillReturnsResponse() {
        InterventionRequest req = new InterventionRequest();
        req.setInputText("I feel anxious");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.empty());
        when(gemini.generate(anyString())).thenReturn("""
            {
              "urgencyLevel": "LOW",
              "message": "Take a deep breath. You are safe.",
              "steps": ["Breathe", "Ground yourself", "Reach out"],
              "script": "I need some support right now.",
              "ttsText": "Take a deep breath.",
              "escalate": false
            }
            """);

        InterventionResponse response = interventionService.process(req, "unknown@hopeguide.com");

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isNotBlank();
    }
}
