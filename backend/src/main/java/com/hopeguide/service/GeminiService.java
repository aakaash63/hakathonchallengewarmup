package com.hopeguide.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public GeminiService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    public String generate(String prompt) {
        try {
            Map<String, Object> body = Map.of(
                "contents", new Object[]{
                    Map.of("parts", new Object[]{
                        Map.of("text", prompt)
                    })
                },
                "generationConfig", Map.of(
                    "temperature", 0.7,
                    "maxOutputTokens", 800
                )
            );

            String response = webClient.post()
                .uri(apiUrl + "?key=" + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(java.time.Duration.ofSeconds(25))
                .block();

            JsonNode root = mapper.readTree(response);
            return root.path("candidates").get(0)
                       .path("content").path("parts").get(0)
                       .path("text").asText();

        } catch (Exception e) {
            log.error("Gemini API error: {}", e.getMessage());
            return null;
        }
    }
}
