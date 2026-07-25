package com.hopeguide.controller;

import com.hopeguide.dto.OnboardingRequest;
import com.hopeguide.entity.User;
import com.hopeguide.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepo;

    public UserController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> profile(@AuthenticationPrincipal String email) {
        User user = userRepo.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(Map.of(
            "id", user.getId(),
            "name", user.getName(),
            "email", user.getEmail(),
            "role", user.getRole(),
            "onboardingComplete", user.isOnboardingComplete(),
            "triggers", orDefault(user.getTriggers()),
            "calmingStrategies", orDefault(user.getCalmingStrategies()),
            "supportContactName", orDefault(user.getSupportContactName()),
            "supportContactPhone", orDefault(user.getSupportContactPhone()),
            "personalMantra", orDefault(user.getPersonalMantra()),
            "safePlace", orDefault(user.getSafePlace()),
            "warningSigns", orDefault(user.getWarningSigns())
        ));
    }

    @PostMapping("/onboarding")
    public ResponseEntity<Map<String, Object>> onboarding(@AuthenticationPrincipal String email,
                                                           @RequestBody OnboardingRequest req) {
        User user = userRepo.findByEmail(email).orElseThrow();
        user.setTriggers(req.getTriggers());
        user.setCalmingStrategies(req.getCalmingStrategies());
        user.setSupportContactName(req.getSupportContactName());
        user.setSupportContactPhone(req.getSupportContactPhone());
        user.setPersonalMantra(req.getPersonalMantra());
        user.setSafePlace(req.getSafePlace());
        user.setWarningSigns(req.getWarningSigns());
        user.setOnboardingComplete(true);
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("message", "Onboarding complete", "onboardingComplete", true));
    }

    @PutMapping("/safety-plan")
    public ResponseEntity<Map<String, Object>> updateSafetyPlan(@AuthenticationPrincipal String email,
                                                                  @RequestBody OnboardingRequest req) {
        return onboarding(email, req);
    }

    private String orDefault(String val) {
        return val == null ? "" : val;
    }
}
