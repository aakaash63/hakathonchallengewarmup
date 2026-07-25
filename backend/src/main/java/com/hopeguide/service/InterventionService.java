package com.hopeguide.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hopeguide.dto.InterventionRequest;
import com.hopeguide.dto.InterventionResponse;
import com.hopeguide.entity.User;
import com.hopeguide.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class InterventionService {

    private static final Logger log = LoggerFactory.getLogger(InterventionService.class);
    private static final List<String> EMERGENCY_KEYWORDS =
        Arrays.asList("overdose", "overdosed", "want to die", "kill myself", "can't breathe",
                       "stop breathing", "unconscious", "not breathing", "took too much");

    private final GeminiService gemini;
    private final UserRepository userRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    public InterventionService(GeminiService gemini, UserRepository userRepo) {
        this.gemini = gemini;
        this.userRepo = userRepo;
    }

    public InterventionResponse process(InterventionRequest req, String userEmail) {
        String input = req.getInputText().toLowerCase();
        boolean isEmergency = EMERGENCY_KEYWORDS.stream().anyMatch(input::contains);

        if (isEmergency) {
            return InterventionResponse.builder()
                .urgencyLevel("HIGH")
                .message("This sounds like an emergency. Please call 911 or your local emergency services immediately. You can do this.")
                .steps(List.of("Call 911 right now", "Stay on the line with emergency services", "If naloxone is available, use it now"))
                .script("I need emergency help right now. Please come immediately.")
                .ttsText("This is an emergency. Please call 911 right now. You can do this.")
                .escalate(true)
                .build();
        }

        User user = userRepo.findByEmail(userEmail).orElse(null);
        String profileContext = buildProfileContext(user);

        String prompt = """
            You are HopeGuide AI, a warm and trauma-informed recovery support companion.
            Respond with calm, non-judgmental, practical guidance. Do not diagnose.
            If you detect any physical danger, set escalate to true.

            User profile context:
            %s

            Current input from user:
            "%s"

            Return ONLY valid JSON in this exact format:
            {
              "urgencyLevel": "LOW|MEDIUM|HIGH",
              "message": "a warm 1-2 sentence supportive message",
              "steps": ["step 1", "step 2", "step 3"],
              "script": "a short personal support script the user can say or share",
              "ttsText": "the message formatted for text-to-speech playback",
              "escalate": false
            }
            """.formatted(profileContext, req.getInputText());

        String raw = gemini.generate(prompt);
        return parseIntervention(raw, user);
    }

    private String buildProfileContext(User user) {
        if (user == null) return "No profile available.";
        return String.format(
            "Name: %s | Triggers: %s | Calming strategies: %s | Support contact: %s | Personal mantra: %s",
            user.getName(),
            orDefault(user.getTriggers(), "not specified"),
            orDefault(user.getCalmingStrategies(), "not specified"),
            orDefault(user.getSupportContactName(), "not specified"),
            orDefault(user.getPersonalMantra(), "not specified")
        );
    }

    private InterventionResponse parseIntervention(String raw, User user) {
        if (raw == null) return fallbackResponse(user);
        try {
            String json = raw.contains("{") ? raw.substring(raw.indexOf("{"), raw.lastIndexOf("}") + 1) : raw;
            JsonNode node = mapper.readTree(json);

            List<String> steps = new ArrayList<>();
            node.path("steps").forEach(s -> steps.add(s.asText()));

            InterventionResponse res = InterventionResponse.builder()
                .urgencyLevel(node.path("urgencyLevel").asText("MEDIUM"))
                .message(node.path("message").asText())
                .steps(steps)
                .script(node.path("script").asText())
                .ttsText(node.path("ttsText").asText(node.path("message").asText()))
                .escalate(node.path("escalate").asBoolean(false))
                .build();

            if (user != null && user.getSupportContactName() != null) {
                res.setContactName(user.getSupportContactName());
                res.setContactPhone(user.getSupportContactPhone());
            }
            return res;
        } catch (Exception e) {
            log.error("Parse error: {}", e.getMessage());
            return fallbackResponse(user);
        }
    }

    private InterventionResponse fallbackResponse(User user) {
        InterventionResponse res = InterventionResponse.builder()
            .urgencyLevel("MEDIUM")
            .message("You reached out — that is the bravest step. Focus on the next two minutes only.")
            .steps(List.of("Move to a safe, calm space", "Take five slow deep breaths", "Contact your support person"))
            .script("I am struggling right now and need your support. Please stay with me for a few minutes.")
            .ttsText("You reached out. That is the bravest step. Focus only on the next two minutes.")
            .escalate(false)
            .build();
        if (user != null) {
            res.setContactName(user.getSupportContactName());
            res.setContactPhone(user.getSupportContactPhone());
        }
        return res;
    }

    private String orDefault(String val, String def) {
        return (val == null || val.isBlank()) ? def : val;
    }
}
