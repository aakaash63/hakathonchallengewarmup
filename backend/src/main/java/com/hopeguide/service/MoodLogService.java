package com.hopeguide.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hopeguide.dto.MoodLogRequest;
import com.hopeguide.entity.MoodLog;
import com.hopeguide.entity.User;
import com.hopeguide.repository.MoodLogRepository;
import com.hopeguide.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MoodLogService {

    private final MoodLogRepository moodRepo;
    private final UserRepository userRepo;
    private final GeminiService gemini;
    private final ObjectMapper mapper = new ObjectMapper();

    public MoodLogService(MoodLogRepository moodRepo, UserRepository userRepo, GeminiService gemini) {
        this.moodRepo = moodRepo;
        this.userRepo = userRepo;
        this.gemini = gemini;
    }

    public MoodLog logMood(MoodLogRequest req, String email) {
        User user = userRepo.findByEmail(email).orElseThrow();
        String riskLevel = computeRisk(req);

        String prompt = """
            A person in substance use recovery just completed a wellness check-in.
            Mood: %s | Urge level: %d/10 | Stress: %d/10 | Sleep quality: %d/5
            Note: "%s"
            Computed risk: %s

            Write:
            1. A 2-sentence warm, non-judgmental summary (aiSummary)
            2. Exactly 3 concrete, compassionate suggestions (suggestions)

            Return ONLY valid JSON:
            {
              "aiSummary": "...",
              "suggestions": ["...", "...", "..."]
            }
            """.formatted(
            req.getMood(), req.getUrgencyLevel(), req.getStressLevel(),
            req.getSleepQuality(), orDefault(req.getVoiceNote(), "none"), riskLevel);

        String raw = gemini.generate(prompt);
        String summary = "Your check-in has been recorded. Keep going — you are doing great.";
        String suggestions = "Try a 5-minute walk.|Reach out to your support contact.|Practice box breathing.";

        if (raw != null) {
            try {
                String json = raw.contains("{") ? raw.substring(raw.indexOf("{"), raw.lastIndexOf("}") + 1) : raw;
                JsonNode node = mapper.readTree(json);
                summary = node.path("aiSummary").asText(summary);
                List<String> sug = new java.util.ArrayList<>();
                node.path("suggestions").forEach(s -> sug.add(s.asText()));
                if (!sug.isEmpty()) suggestions = String.join("|", sug);
            } catch (Exception ignored) {}
        }

        MoodLog log = new MoodLog();
        log.setUser(user);
        log.setMood(req.getMood());
        log.setUrgencyLevel(req.getUrgencyLevel());
        log.setStressLevel(req.getStressLevel());
        log.setSleepQuality(req.getSleepQuality());
        log.setVoiceNote(req.getVoiceNote());
        log.setRiskLevel(riskLevel);
        log.setAiSummary(summary);
        log.setSuggestions(suggestions);
        return moodRepo.save(log);
    }

    public List<MoodLog> getHistory(String email) {
        User user = userRepo.findByEmail(email).orElseThrow();
        return moodRepo.findTop7ByUserOrderByLoggedAtDesc(user);
    }

    private String computeRisk(MoodLogRequest req) {
        int score = 0;
        if (req.getUrgencyLevel() >= 8) score += 3;
        else if (req.getUrgencyLevel() >= 5) score += 2;
        else score += 1;
        if (req.getStressLevel() >= 8) score += 2;
        else if (req.getStressLevel() >= 5) score += 1;
        if (req.getSleepQuality() <= 2) score += 2;
        else if (req.getSleepQuality() <= 3) score += 1;
        String note = orDefault(req.getVoiceNote(), "").toLowerCase();
        if (note.contains("relapse") || note.contains("use") || note.contains("overwhelmed")) score += 2;

        if (score >= 7) return "HIGH";
        if (score >= 4) return "MEDIUM";
        return "LOW";
    }

    private String orDefault(String val, String def) {
        return (val == null || val.isBlank()) ? def : val;
    }
}
