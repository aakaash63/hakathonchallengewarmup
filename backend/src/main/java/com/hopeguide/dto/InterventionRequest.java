package com.hopeguide.dto;

import lombok.Data;

@Data
public class InterventionRequest {
    private String inputText;
    private String mode; // voice or text
}
