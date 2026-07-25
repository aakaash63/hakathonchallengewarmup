package com.hopeguide.dto;

import lombok.Data;

@Data
public class OnboardingRequest {
    private String triggers;
    private String calmingStrategies;
    private String supportContactName;
    private String supportContactPhone;
    private String personalMantra;
    private String safePlace;
    private String warningSigns;
}
