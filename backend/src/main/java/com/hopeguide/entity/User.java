package com.hopeguide.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hg_users")
@Data
@NoArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role = "INDIVIDUAL"; // INDIVIDUAL or FAMILY

    private String preferredLanguage = "en";
    private boolean onboardingComplete = false;

    // Safety profile
    private String triggers;
    private String calmingStrategies;
    private String supportContactName;
    private String supportContactPhone;
    private String personalMantra;
    private String safePlace;
    private String warningSigns;
}
