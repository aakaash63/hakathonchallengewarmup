package com.hopeguide.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "hg_mood_logs")
@Data
@NoArgsConstructor
public class MoodLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String mood;
    private int urgencyLevel;   // 1-10 craving/urge scale
    private int stressLevel;    // 1-10
    private int sleepQuality;   // 1-5
    private String voiceNote;
    private String riskLevel;   // LOW / MEDIUM / HIGH
    private String aiSummary;

    @Column(columnDefinition = "TEXT")
    private String suggestions;

    private LocalDateTime loggedAt = LocalDateTime.now();
}
