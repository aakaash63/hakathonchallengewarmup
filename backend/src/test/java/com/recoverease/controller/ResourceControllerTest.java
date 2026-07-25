package com.recoverease.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recoverease.entity.ResourceItem;
import com.recoverease.service.ResourceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "gemini.api.key=test-key",
        "spring.datasource.url=jdbc:h2:mem:testdb_resource",
        "jwt.secret=RecoverEaseAISecretKey2025SuperSecureHackathonJWTToken",
        "jwt.expiration=86400000"
})
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResourceService resourceService;

    // ─── GET /api/resources ────────────────────────────────────────────────────

    @Test
    void getAllResources_noParams_returns200WithAllResources() throws Exception {
        ResourceItem item1 = buildResource("Understanding Addiction", "EDUCATION");
        ResourceItem item2 = buildResource("SAMHSA Hotline", "HOTLINE");

        when(resourceService.getAllResources()).thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Understanding Addiction"))
                .andExpect(jsonPath("$[1].title").value("SAMHSA Hotline"));
    }

    @Test
    void getAllResources_withCategory_returns200WithFilteredResources() throws Exception {
        ResourceItem item = buildResource("Addiction Facts", "EDUCATION");

        when(resourceService.getByCategory("EDUCATION")).thenReturn(List.of(item));

        mockMvc.perform(get("/api/resources").param("category", "EDUCATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Addiction Facts"));
    }

    @Test
    void getAllResources_withSearchQuery_returns200WithSearchResults() throws Exception {
        ResourceItem item = buildResource("Recovery Steps", "EDUCATION");

        when(resourceService.search("recovery")).thenReturn(List.of(item));

        mockMvc.perform(get("/api/resources").param("search", "recovery"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Recovery Steps"));
    }

    @Test
    void getAllResources_searchTakesPriorityOverCategory_usesSearch() throws Exception {
        // When both search and category are provided, search wins
        ResourceItem item = buildResource("Search Result", "EDUCATION");
        when(resourceService.search("addiction")).thenReturn(List.of(item));

        mockMvc.perform(get("/api/resources")
                        .param("search", "addiction")
                        .param("category", "EDUCATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Search Result"));
    }

    @Test
    void getAllResources_emptyResult_returns200WithEmptyList() throws Exception {
        when(resourceService.getAllResources()).thenReturn(List.of());

        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // Resource endpoints are public (no auth required)
    @Test
    void getAllResources_noAuthRequired_returns200() throws Exception {
        when(resourceService.getAllResources()).thenReturn(List.of());

        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isOk());
    }

    // ─── POST /api/resources/explain ──────────────────────────────────────────

    @Test
    void explainTopic_validQuestion_returns200WithAnswer() throws Exception {
        when(resourceService.explainTopic("What is addiction?"))
                .thenReturn(Map.of(
                        "answer", "Addiction is a chronic condition affecting the brain.",
                        "source", "SAMHSA"
                ));

        String body = objectMapper.writeValueAsString(Map.of("question", "What is addiction?"));

        mockMvc.perform(post("/api/resources/explain")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Addiction is a chronic condition affecting the brain."))
                .andExpect(jsonPath("$.source").value("SAMHSA"));
    }

    @Test
    void explainTopic_emptyQuestion_returns400WithError() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("question", ""));

        mockMvc.perform(post("/api/resources/explain")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Question is required"));
    }

    @Test
    void explainTopic_blankQuestion_returns400WithError() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("question", "   "));

        mockMvc.perform(post("/api/resources/explain")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Question is required"));
    }

    @Test
    void explainTopic_missingQuestionKey_returns400WithError() throws Exception {
        // body has no "question" key → getOrDefault returns "" → blank
        String body = objectMapper.writeValueAsString(Map.of("topic", "addiction"));

        mockMvc.perform(post("/api/resources/explain")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Question is required"));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private ResourceItem buildResource(String title, String category) {
        ResourceItem item = new ResourceItem();
        item.setTitle(title);
        item.setCategory(category);
        return item;
    }
}
