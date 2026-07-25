package com.hopeguide.service;

import com.hopeguide.dto.ScriptRequest;
import com.hopeguide.entity.SupportScript;
import com.hopeguide.entity.User;
import com.hopeguide.repository.SupportScriptRepository;
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
@DisplayName("ScriptService Tests")
class ScriptServiceTest {

    @Mock private GeminiService gemini;
    @Mock private SupportScriptRepository scriptRepo;
    @Mock private UserRepository userRepo;

    @InjectMocks private ScriptService scriptService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Jordan Lee");
        mockUser.setEmail("user@hopeguide.com");
        mockUser.setCalmingStrategies("deep breathing");
        mockUser.setSupportContactName("Alex");
        mockUser.setPersonalMantra("One day at a time");
        mockUser.setTriggers("stress, loneliness");
    }

    // ─── SCRIPT GENERATION TESTS ─────────────────────────────────────────────

    @Test
    @DisplayName("Generate: returns Gemini-generated script text")
    void generate_validRequest_returnsGeminiScript() {
        ScriptRequest req = new ScriptRequest();
        req.setScenario("urge");
        req.setAudience("self");

        String geminiScript = "I am feeling the urge right now, but I choose to breathe and reach out instead. I am stronger than this moment.";

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(gemini.generate(anyString())).thenReturn(geminiScript);
        when(scriptRepo.save(any(SupportScript.class))).thenAnswer(inv -> inv.getArgument(0));

        SupportScript result = scriptService.generate(req, "user@hopeguide.com");

        assertThat(result.getScriptText()).isEqualTo(geminiScript.trim());
        assertThat(result.getScenario()).isEqualTo("urge");
        assertThat(result.getAudience()).isEqualTo("self");
        verify(gemini, times(1)).generate(anyString());
    }

    @Test
    @DisplayName("Generate: uses fallback text when Gemini returns null")
    void generate_geminiReturnsNull_usesFallbackText() {
        ScriptRequest req = new ScriptRequest();
        req.setScenario("grounding");
        req.setAudience("self");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(gemini.generate(anyString())).thenReturn(null);
        when(scriptRepo.save(any(SupportScript.class))).thenAnswer(inv -> inv.getArgument(0));

        SupportScript result = scriptService.generate(req, "user@hopeguide.com");

        assertThat(result.getScriptText()).isNotBlank();
        assertThat(result.getScriptText()).contains("one step at a time");
    }

    @Test
    @DisplayName("Generate: uses fallback when Gemini returns blank string")
    void generate_geminiReturnsBlank_usesFallback() {
        ScriptRequest req = new ScriptRequest();
        req.setScenario("refusal");
        req.setAudience("self");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(gemini.generate(anyString())).thenReturn("   ");
        when(scriptRepo.save(any(SupportScript.class))).thenAnswer(inv -> inv.getArgument(0));

        SupportScript result = scriptService.generate(req, "user@hopeguide.com");

        assertThat(result.getScriptText()).isNotBlank();
    }

    // ─── TITLE GENERATION TESTS ──────────────────────────────────────────────

    @Test
    @DisplayName("Title: urge + self generates correct title")
    void generate_urgeScenarioSelfAudience_correctTitle() {
        ScriptRequest req = new ScriptRequest();
        req.setScenario("urge");
        req.setAudience("self");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(gemini.generate(anyString())).thenReturn("Script text here");
        when(scriptRepo.save(any(SupportScript.class))).thenAnswer(inv -> inv.getArgument(0));

        SupportScript result = scriptService.generate(req, "user@hopeguide.com");

        assertThat(result.getTitle()).contains("Managing an Urge");
        assertThat(result.getTitle()).contains("For Myself");
    }

    @Test
    @DisplayName("Title: relapse_prevention + family generates correct title")
    void generate_relapsePreventionFamilyAudience_correctTitle() {
        ScriptRequest req = new ScriptRequest();
        req.setScenario("relapse_prevention");
        req.setAudience("family");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(gemini.generate(anyString())).thenReturn("Script");
        when(scriptRepo.save(any(SupportScript.class))).thenAnswer(inv -> inv.getArgument(0));

        SupportScript result = scriptService.generate(req, "user@hopeguide.com");

        assertThat(result.getTitle()).contains("Relapse Prevention");
        assertThat(result.getTitle()).contains("For Family");
    }

    @Test
    @DisplayName("Title: grounding scenario title is correct")
    void generate_groundingScenario_correctTitle() {
        ScriptRequest req = new ScriptRequest();
        req.setScenario("grounding");
        req.setAudience("peer");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(gemini.generate(anyString())).thenReturn("Grounding script");
        when(scriptRepo.save(any(SupportScript.class))).thenAnswer(inv -> inv.getArgument(0));

        SupportScript result = scriptService.generate(req, "user@hopeguide.com");

        assertThat(result.getTitle()).contains("Grounding Myself");
        assertThat(result.getTitle()).contains("For Peer");
    }

    // ─── HISTORY TESTS ───────────────────────────────────────────────────────

    @Test
    @DisplayName("History: returns last 10 scripts for user")
    void getHistory_returnsUpToTenScripts() {
        List<SupportScript> scripts = List.of(new SupportScript(), new SupportScript());
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(scriptRepo.findTop10ByUserOrderByCreatedAtDesc(mockUser)).thenReturn(scripts);

        List<SupportScript> result = scriptService.getHistory("user@hopeguide.com");

        assertThat(result).hasSize(2);
        verify(scriptRepo).findTop10ByUserOrderByCreatedAtDesc(mockUser);
    }

    // ─── PROFILE CONTEXT TESTS ───────────────────────────────────────────────

    @Test
    @DisplayName("Prompt: includes user profile in Gemini prompt")
    void generate_includesUserProfileInPrompt() {
        ScriptRequest req = new ScriptRequest();
        req.setScenario("family_support");
        req.setAudience("family");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(gemini.generate(argThat(prompt ->
            prompt.contains("Jordan Lee") &&
            prompt.contains("deep breathing") &&
            prompt.contains("Alex") &&
            prompt.contains("One day at a time")
        ))).thenReturn("Script content");
        when(scriptRepo.save(any(SupportScript.class))).thenAnswer(inv -> inv.getArgument(0));

        SupportScript result = scriptService.generate(req, "user@hopeguide.com");

        assertThat(result.getScriptText()).isNotBlank();
        verify(gemini).generate(anyString());
    }
}
