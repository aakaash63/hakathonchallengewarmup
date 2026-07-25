package com.hopeguide.controller;

import com.hopeguide.dto.FamilyGuidanceRequest;
import com.hopeguide.dto.FamilyGuidanceResponse;
import com.hopeguide.service.FamilyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/family")
public class FamilyController {

    private final FamilyService familyService;

    public FamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }

    @PostMapping("/guidance")
    public ResponseEntity<FamilyGuidanceResponse> guidance(@RequestBody FamilyGuidanceRequest req) {
        return ResponseEntity.ok(familyService.getGuidance(req));
    }
}
