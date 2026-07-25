package com.hopeguide.dto;

import lombok.Data;

@Data
public class ScriptRequest {
    private String scenario;   // urge, relapse_prevention, refusal, grounding, family_support
    private String audience;   // self, family, peer, sponsor
}
