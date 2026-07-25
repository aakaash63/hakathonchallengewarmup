package com.hopeguide.dto;

import lombok.Data;

@Data
public class MoodLogRequest {
    private String mood;
    private int urgencyLevel;
    private int stressLevel;
    private int sleepQuality;
    private String voiceNote;
}
