package com.hopeguide.dto;

import lombok.Data;

@Data
public class FamilyGuidanceRequest {
    private String situation;   // anxious, agitated, withdrawn, possible_relapse, overdose_concern
    private String context;
}
