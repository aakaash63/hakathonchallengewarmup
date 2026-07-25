package com.hopeguide.service;

import com.hopeguide.dto.FamilyGuidanceRequest;
import com.hopeguide.dto.FamilyGuidanceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FamilyService Tests")
class FamilyServiceTest {

    @Mock private GeminiService gemini;

    @InjectMocks private FamilyService familyService;

    // ─── OVERDOSE ESCALATION TESTS ───────────────────────────────────────────

    @Test
    @DisplayName("Overdose: auto-escalates without calling Gemini")
    void getGuidance_overdoseSituation_autoEscalatesWithoutGemini() {
        FamilyGuidanceRequest req = new FamilyGuidanceRequest();
        req.setSituation("overdose_concern");
        req.setContext("Person is unconscious");

        FamilyGuidanceResponse response = familyService.getGuidance(req);

        assertThat(response.isEmergencyEscalate()).isTrue();
        assertThat(response.getNextSteps().get(0)).contains("911");
        verify(gemini, never()).generate(anyString());
    }

    @Test
    @DisplayName("Overdose: includes naloxone instruction in steps")
    void getGuidance_overdoseSituation_includesNaloxoneInstruction() {
        FamilyGuidanceRequest req = new FamilyGuidanceRequest();
        req.setSituation("overdose_concern");

        FamilyGuidanceResponse response = familyService.getGuidance(req);

        boolean hasNaloxone = response.getNextSteps().stream()
            .anyMatch(s -> s.toLowerCase().contains("naloxone"));
        assertThat(hasNaloxone).isTrue();
    }

    // ─── NORMAL GUIDANCE TESTS ───────────────────────────────────────────────

    @Test
    @DisplayName("Normal: calls Gemini for non-emergency situations")
    void getGuidance_nonEmergencySituation_callsGemini() {
        FamilyGuidanceRequest req = new FamilyGuidanceRequest();
        req.setSituation("possible_relapse");
        req.setContext("Person seems withdrawn today");

        String geminiJson = """
            {
              "whatToSay": "I am here with you. Whatever you are feeling right now, we will face it together.",
              "avoidSaying": ["Why can't you just stop?", "You always do this", "I give up on you"],
              "nextSteps": ["Stay calm and present", "Ask open-ended questions", "Offer one concrete form of help"],
              "emergencyEscalate": false
            }
            """;

        when(gemini.generate(anyString())).thenReturn(geminiJson);

        FamilyGuidanceResponse response = familyService.getGuidance(req);

        assertThat(response.getWhatToSay()).contains("here with you");
        assertThat(response.getAvoidSaying()).hasSize(3);
        assertThat(response.getNextSteps()).hasSize(3);
        assertThat(response.isEmergencyEscalate()).isFalse();
        verify(gemini, times(1)).generate(anyString());
    }

    @Test
    @DisplayName("Normal: returns fallback when Gemini returns null")
    void getGuidance_geminiReturnsNull_returnsFallback() {
        FamilyGuidanceRequest req = new FamilyGuidanceRequest();
        req.setSituation("anxious");
        req.setContext("Some context");

        when(gemini.generate(anyString())).thenReturn(null);

        FamilyGuidanceResponse response = familyService.getGuidance(req);

        assertThat(response).isNotNull();
        assertThat(response.getWhatToSay()).isNotBlank();
        assertThat(response.getAvoidSaying()).isNotEmpty();
        assertThat(response.getNextSteps()).isNotEmpty();
    }

    @Test
    @DisplayName("Normal: returns fallback when Gemini returns malformed JSON")
    void getGuidance_geminiReturnsBadJson_returnsFallback() {
        FamilyGuidanceRequest req = new FamilyGuidanceRequest();
        req.setSituation("withdrawn");

        when(gemini.generate(anyString())).thenReturn("not json at all %%%");

        FamilyGuidanceResponse response = familyService.getGuidance(req);

        assertThat(response).isNotNull();
        assertThat(response.getWhatToSay()).isNotBlank();
    }

    @Test
    @DisplayName("Normal: handles null context gracefully")
    void getGuidance_nullContext_doesNotThrow() {
        FamilyGuidanceRequest req = new FamilyGuidanceRequest();
        req.setSituation("anxious");
        req.setContext(null);

        when(gemini.generate(anyString())).thenReturn("""
            {
              "whatToSay": "I am here.",
              "avoidSaying": ["Don't say this"],
              "nextSteps": ["Do this"],
              "emergencyEscalate": false
            }
            """);

        assertThatCode(() -> familyService.getGuidance(req)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Normal: avoidSaying list contains at least one item")
    void getGuidance_validGeminiResponse_avoidSayingNotEmpty() {
        FamilyGuidanceRequest req = new FamilyGuidanceRequest();
        req.setSituation("agitated");
        req.setContext("Raised voice, pacing around");

        String geminiJson = """
            {
              "whatToSay": "Let us take a deep breath together. I am right here.",
              "avoidSaying": ["You're being dramatic", "Calm down already"],
              "nextSteps": ["Lower your own voice first", "Give them physical space", "Ask what they need"],
              "emergencyEscalate": false
            }
            """;

        when(gemini.generate(anyString())).thenReturn(geminiJson);

        FamilyGuidanceResponse response = familyService.getGuidance(req);

        assertThat(response.getAvoidSaying()).isNotEmpty();
        assertThat(response.getNextSteps()).hasSize(3);
    }
}
