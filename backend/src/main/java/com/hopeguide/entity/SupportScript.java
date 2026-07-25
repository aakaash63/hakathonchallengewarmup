package com.hopeguide.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "hg_support_scripts")
@Data
@NoArgsConstructor
public class SupportScript {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String scenario;      // urge, relapse_prevention, refusal, grounding, family_support
    private String audience;      // self, family, peer, sponsor
    private String title;

    @Column(columnDefinition = "TEXT")
    private String scriptText;

    private LocalDateTime createdAt = LocalDateTime.now();
}
