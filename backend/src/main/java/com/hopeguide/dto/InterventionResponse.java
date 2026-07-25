package com.hopeguide.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InterventionResponse {
    private String urgencyLevel;     // LOW / MEDIUM / HIGH
    private String message;
    private List<String> steps;
    private String script;
    private String ttsText;
    private boolean escalate;
    private String contactName;
    private String contactPhone;
}
