package com.hopeguide.controller;

import com.hopeguide.dto.InterventionRequest;
import com.hopeguide.dto.InterventionResponse;
import com.hopeguide.service.InterventionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/intervention")
public class InterventionController {

    private final InterventionService interventionService;

    public InterventionController(InterventionService interventionService) {
        this.interventionService = interventionService;
    }

    @PostMapping("/respond")
    public ResponseEntity<InterventionResponse> respond(@RequestBody InterventionRequest req,
                                                         @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(interventionService.process(req, email));
    }
}
