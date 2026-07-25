package com.hopeguide.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hopeguide.dto.FamilyGuidanceRequest;
import com.hopeguide.dto.FamilyGuidanceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FamilyService {

    private static final Logger log = LoggerFactory.getLogger(FamilyService.class);
    private final GeminiService gemini;
    private final ObjectMapper mapper = new ObjectMapper();

    public FamilyService(GeminiService gemini) {
        this.gemini = gemini;
    }

    public FamilyGuidanceResponse getGuidance(FamilyGuidanceRequest req) {
        boolean isOverdose = "overdose_concern".equals(req.getSituation());

        if (isOverdose) {
            return FamilyGuidanceResponse.builder()
                .whatToSay("I am calling for help right now. Stay with me. I am right here.")
                .avoidSaying(List.of("Do not leave them alone", "Do not give them water or food", "Do not wait to see if it gets better"))
                .nextSteps(List.of("Call 911 immediately", "Administer naloxone if available", "Stay on the line with emergency services until help arrives"))
                .emergencyEscalate(true)
                .build();
        }

        String prompt = """
            You are HopeGuide AI, helping a family member or caregiver support someone in substance use recovery.
            Be compassionate, practical, and trauma-informed.

            Situation: %s
            Additional context: %s

            Provide guidance in valid JSON only:
            {
              "whatToSay": "a calm, supportive script under 60 words",
              "avoidSaying": ["phrase 1", "phrase 2", "phrase 3"],
              "nextSteps": ["step 1", "step 2", "step 3"],
              "emergencyEscalate": false
            }
            """.formatted(req.getSituation(), orDefault(req.getContext(), "none provided"));

        String raw = gemini.generate(prompt);
        return parseGuidance(raw);
    }

    private FamilyGuidanceResponse parseGuidance(String raw) {
        if (raw == null) return fallback();
        try {
            String json = raw.contains("{") ? raw.substring(raw.indexOf("{"), raw.lastIndexOf("}") + 1) : raw;
            JsonNode node = mapper.readTree(json);
            List<String> avoid = new ArrayList<>();
            List<String> steps = new ArrayList<>();
            node.path("avoidSaying").forEach(s -> avoid.add(s.asText()));
            node.path("nextSteps").forEach(s -> steps.add(s.asText()));
            return FamilyGuidanceResponse.builder()
                .whatToSay(node.path("whatToSay").asText())
                .avoidSaying(avoid)
                .nextSteps(steps)
                .emergencyEscalate(node.path("emergencyEscalate").asBoolean(false))
                .build();
        } catch (Exception e) {
            log.error("Parse error: {}", e.getMessage());
            return fallback();
        }
    }

    private FamilyGuidanceResponse fallback() {
        return FamilyGuidanceResponse.builder()
            .whatToSay("I am here with you. I am not here to judge. Whatever you need right now, let's figure it out together.")
            .avoidSaying(List.of("Why can't you just stop?", "You always do this", "I give up on you"))
            .nextSteps(List.of("Stay calm and present", "Offer one specific form of support", "Encourage professional help if needed"))
            .emergencyEscalate(false)
            .build();
    }

    private String orDefault(String val, String def) {
        return (val == null || val.isBlank()) ? def : val;
    }
}
