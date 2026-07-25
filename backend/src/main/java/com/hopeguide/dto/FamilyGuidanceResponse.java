package com.hopeguide.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FamilyGuidanceResponse {
    private String whatToSay;
    private List<String> avoidSaying;
    private List<String> nextSteps;
    private boolean emergencyEscalate;
}
