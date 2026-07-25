package com.hopeguide.service;

import com.hopeguide.entity.GuideResource;
import com.hopeguide.repository.GuideResourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ResourceService {

    private final GuideResourceRepository resourceRepo;
    private final GeminiService gemini;

    public ResourceService(GuideResourceRepository resourceRepo, GeminiService gemini) {
        this.resourceRepo = resourceRepo;
        this.gemini = gemini;
    }

    public List<GuideResource> getAll() {
        return resourceRepo.findAll();
    }

    public List<GuideResource> getByCategory(String category) {
        return resourceRepo.findByCategory(category.toUpperCase());
    }

    public List<GuideResource> search(String query) {
        return resourceRepo.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query);
    }

    public Map<String, String> explain(String question) {
        String prompt = """
            You are HopeGuide AI, a recovery education assistant.
            Answer the following question about substance use recovery in simple, plain language.
            Keep the answer under 120 words.
            End with one trusted source reference (SAMHSA, NIDA, CDC, or WHO).
            Do not diagnose or prescribe.

            Question: %s

            Return ONLY valid JSON:
            {
              "answer": "...",
              "source": "Source Name — url or description"
            }
            """.formatted(question);

        String raw = gemini.generate(prompt);
        String answer = "Please consult a trusted recovery resource such as SAMHSA (samhsa.gov) for information on this topic.";
        String source = "SAMHSA — samhsa.gov";

        if (raw != null) {
            try {
                var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String json = raw.contains("{") ? raw.substring(raw.indexOf("{"), raw.lastIndexOf("}") + 1) : raw;
                var node = mapper.readTree(json);
                answer = node.path("answer").asText(answer);
                source = node.path("source").asText(source);
            } catch (Exception ignored) {}
        }

        return Map.of("answer", answer, "source", source);
    }
}
