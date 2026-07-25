package com.hopeguide.service;

import com.hopeguide.dto.ScriptRequest;
import com.hopeguide.entity.SupportScript;
import com.hopeguide.entity.User;
import com.hopeguide.repository.SupportScriptRepository;
import com.hopeguide.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScriptService {

    private final GeminiService gemini;
    private final SupportScriptRepository scriptRepo;
    private final UserRepository userRepo;

    public ScriptService(GeminiService gemini, SupportScriptRepository scriptRepo, UserRepository userRepo) {
        this.gemini = gemini;
        this.scriptRepo = scriptRepo;
        this.userRepo = userRepo;
    }

    public SupportScript generate(ScriptRequest req, String email) {
        User user = userRepo.findByEmail(email).orElseThrow();

        String prompt = """
            You are HopeGuide AI, a recovery support assistant.
            Generate a personalized, compassionate support script.

            User profile:
            - Name: %s
            - Calming strategies: %s
            - Support contact: %s
            - Personal mantra: %s
            - Triggers: %s

            Script details:
            - Scenario: %s
            - Intended audience: %s

            Guidelines:
            - Keep the script under 80 words
            - Use warm, direct, first-person language for "self" scripts
            - Use second-person compassionate tone for "family" or "peer" scripts
            - Do not mention substances or relapse directly in refusal scripts
            - End with something hopeful

            Return ONLY the script text, no title or explanation.
            """.formatted(
            user.getName(),
            orDefault(user.getCalmingStrategies(), "breathing exercises"),
            orDefault(user.getSupportContactName(), "my support person"),
            orDefault(user.getPersonalMantra(), "one day at a time"),
            orDefault(user.getTriggers(), "stress and isolation"),
            req.getScenario(), req.getAudience());

        String scriptText = gemini.generate(prompt);
        if (scriptText == null || scriptText.isBlank()) {
            scriptText = "I am taking things one step at a time. Right now I choose to reach for support instead of substances. I am worth this effort.";
        }

        String title = buildTitle(req.getScenario(), req.getAudience());

        SupportScript script = new SupportScript();
        script.setUser(user);
        script.setScenario(req.getScenario());
        script.setAudience(req.getAudience());
        script.setTitle(title);
        script.setScriptText(scriptText.trim());
        return scriptRepo.save(script);
    }

    public List<SupportScript> getHistory(String email) {
        User user = userRepo.findByEmail(email).orElseThrow();
        return scriptRepo.findTop10ByUserOrderByCreatedAtDesc(user);
    }

    private String buildTitle(String scenario, String audience) {
        String s = switch (scenario) {
            case "urge" -> "Managing an Urge";
            case "relapse_prevention" -> "Relapse Prevention";
            case "refusal" -> "Refusing Substances";
            case "grounding" -> "Grounding Myself";
            case "family_support" -> "Reaching Out for Family Support";
            default -> "Support Script";
        };
        String a = switch (audience) {
            case "family" -> " (For Family)";
            case "peer" -> " (For Peer)";
            case "sponsor" -> " (For Sponsor)";
            default -> " (For Myself)";
        };
        return s + a;
    }

    private String orDefault(String val, String def) {
        return (val == null || val.isBlank()) ? def : val;
    }
}
