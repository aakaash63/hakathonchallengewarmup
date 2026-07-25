package com.hopeguide.controller;

import com.hopeguide.dto.ScriptRequest;
import com.hopeguide.entity.SupportScript;
import com.hopeguide.service.ScriptService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scripts")
public class ScriptController {

    private final ScriptService scriptService;

    public ScriptController(ScriptService scriptService) {
        this.scriptService = scriptService;
    }

    @PostMapping("/generate")
    public ResponseEntity<SupportScript> generate(@RequestBody ScriptRequest req,
                                                   @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(scriptService.generate(req, email));
    }

    @GetMapping("/history")
    public ResponseEntity<List<SupportScript>> history(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(scriptService.getHistory(email));
    }
}
