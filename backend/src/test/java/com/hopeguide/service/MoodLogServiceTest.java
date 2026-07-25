package com.hopeguide.service;

import com.hopeguide.dto.MoodLogRequest;
import com.hopeguide.entity.MoodLog;
import com.hopeguide.entity.User;
import com.hopeguide.repository.MoodLogRepository;
import com.hopeguide.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MoodLogService Tests")
class MoodLogServiceTest {

    @Mock private MoodLogRepository moodRepo;
    @Mock private UserRepository userRepo;
    @Mock private GeminiService gemini;

    @InjectMocks private MoodLogService moodLogService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("user@hopeguide.com");
        mockUser.setName("Jordan Lee");
    }

    // ─── RISK SCORING TESTS ──────────────────────────────────────────────────

    @Test
    @DisplayName("Risk: computes HIGH risk for extreme values")
    void logMood_highUrgencyStressLowSleep_computesHighRisk() {
        MoodLogRequest req = buildRequest("anxious", 9, 9, 1, "I feel like relapsing today");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(gemini.generate(anyString())).thenReturn(buildGeminiJson());
        when(moodRepo.save(any(MoodLog.class))).thenAnswer(inv -> inv.getArgument(0));

        MoodLog log = moodLogService.logMood(req, "user@hopeguide.com");

        assertThat(log.getRiskLevel()).isEqualTo("HIGH");
    }

    @Test
    @DisplayName("Risk: computes LOW risk for healthy values")
    void logMood_lowUrgencyLowStressGoodSleep_computesLowRisk() {
        MoodLogRequest req = buildRequest("calm", 1, 2, 5, "Having a good day");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(gemini.generate(anyString())).thenReturn(buildGeminiJson());
        when(moodRepo.save(any(MoodLog.class))).thenAnswer(inv -> inv.getArgument(0));

        MoodLog log = moodLogService.logMood(req, "user@hopeguide.com");

        assertThat(log.getRiskLevel()).isEqualTo("LOW");
    }

    @Test
    @DisplayName("Risk: computes MEDIUM risk for moderate values")
    void logMood_moderateValues_computesMediumRisk() {
        MoodLogRequest req = buildRequest("stressed", 5, 6, 3, "Tough day but managing");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(gemini.generate(anyString())).thenReturn(buildGeminiJson());
        when(moodRepo.save(any(MoodLog.class))).thenAnswer(inv -> inv.getArgument(0));

        MoodLog log = moodLogService.logMood(req, "user@hopeguide.com");

        assertThat(log.getRiskLevel()).isEqualTo("MEDIUM");
    }

    @Test
    @DisplayName("Risk: voice note with relapse keyword adds to risk score")
    void logMood_voiceNoteWithRelapseKeyword_increasesRisk() {
        MoodLogRequest req = buildRequest("neutral", 4, 4, 3, "thinking about relapse a lot");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(gemini.generate(anyString())).thenReturn(buildGeminiJson());
        when(moodRepo.save(any(MoodLog.class))).thenAnswer(inv -> inv.getArgument(0));

        MoodLog log = moodLogService.logMood(req, "user@hopeguide.com");

        // With keyword boost, should not be LOW
        assertThat(log.getRiskLevel()).isIn("MEDIUM", "HIGH");
    }

    // ─── AI SUMMARY TESTS ───────────────────────────────────────────────────

    @Test
    @DisplayName("AI: saves Gemini-generated summary to mood log")
    void logMood_geminiReturnsValidJson_savesSummary() {
        MoodLogRequest req = buildRequest("anxious", 7, 7, 2, "Feeling rough");
        String geminiJson = """
            {
              "aiSummary": "You are taking a brave step by checking in today. Your feelings are valid.",
              "suggestions": ["Try box breathing for 5 minutes", "Text your support contact", "Take a short walk outside"]
            }
            """;

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(gemini.generate(anyString())).thenReturn(geminiJson);
        when(moodRepo.save(any(MoodLog.class))).thenAnswer(inv -> inv.getArgument(0));

        MoodLog log = moodLogService.logMood(req, "user@hopeguide.com");

        assertThat(log.getAiSummary()).contains("brave step");
        assertThat(log.getSuggestions()).contains("box breathing");
    }

    @Test
    @DisplayName("AI: uses fallback summary when Gemini returns null")
    void logMood_geminiReturnsNull_usesFallbackSummary() {
        MoodLogRequest req = buildRequest("sad", 5, 5, 3, "");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(gemini.generate(anyString())).thenReturn(null);
        when(moodRepo.save(any(MoodLog.class))).thenAnswer(inv -> inv.getArgument(0));

        MoodLog log = moodLogService.logMood(req, "user@hopeguide.com");

        assertThat(log.getAiSummary()).isNotBlank();
        assertThat(log.getSuggestions()).isNotBlank();
    }

    @Test
    @DisplayName("AI: uses fallback when Gemini returns malformed JSON")
    void logMood_geminiReturnsBadJson_usesFallback() {
        MoodLogRequest req = buildRequest("worried", 6, 6, 2, "");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(gemini.generate(anyString())).thenReturn("{ broken json %%%");
        when(moodRepo.save(any(MoodLog.class))).thenAnswer(inv -> inv.getArgument(0));

        MoodLog log = moodLogService.logMood(req, "user@hopeguide.com");

        assertThat(log.getAiSummary()).isNotBlank();
    }

    // ─── PERSISTENCE TESTS ──────────────────────────────────────────────────

    @Test
    @DisplayName("Persistence: saves all fields to database")
    void logMood_savesAllFieldsCorrectly() {
        MoodLogRequest req = buildRequest("hopeful", 3, 3, 4, "Good morning");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(gemini.generate(anyString())).thenReturn(buildGeminiJson());
        when(moodRepo.save(any(MoodLog.class))).thenAnswer(inv -> inv.getArgument(0));

        MoodLog log = moodLogService.logMood(req, "user@hopeguide.com");

        assertThat(log.getMood()).isEqualTo("hopeful");
        assertThat(log.getUrgencyLevel()).isEqualTo(3);
        assertThat(log.getStressLevel()).isEqualTo(3);
        assertThat(log.getSleepQuality()).isEqualTo(4);
        assertThat(log.getVoiceNote()).isEqualTo("Good morning");
        assertThat(log.getUser()).isEqualTo(mockUser);
    }

    @Test
    @DisplayName("History: returns last 7 entries for user")
    void getHistory_returnsSevenEntries() {
        List<MoodLog> mockLogs = List.of(new MoodLog(), new MoodLog(), new MoodLog());
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(moodRepo.findTop7ByUserOrderByLoggedAtDesc(mockUser)).thenReturn(mockLogs);

        List<MoodLog> result = moodLogService.getHistory("user@hopeguide.com");

        assertThat(result).hasSize(3);
        verify(moodRepo).findTop7ByUserOrderByLoggedAtDesc(mockUser);
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private MoodLogRequest buildRequest(String mood, int urgency, int stress, int sleep, String note) {
        MoodLogRequest req = new MoodLogRequest();
        req.setMood(mood);
        req.setUrgencyLevel(urgency);
        req.setStressLevel(stress);
        req.setSleepQuality(sleep);
        req.setVoiceNote(note);
        return req;
    }

    private String buildGeminiJson() {
        return """
            {
              "aiSummary": "You are doing great by checking in. Keep going.",
              "suggestions": ["Take a walk", "Call your support", "Practice breathing"]
            }
            """;
    }
}
