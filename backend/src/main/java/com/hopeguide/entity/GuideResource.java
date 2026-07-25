package com.hopeguide.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hg_resources")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuideResource {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String category;   // RELAPSE, COPING, OVERDOSE, FAMILY, TREATMENT, RECOVERY
    private String sourceName;
    private String sourceUrl;

    @Column(columnDefinition = "TEXT")
    private String summary;
}
