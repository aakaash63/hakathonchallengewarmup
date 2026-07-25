package com.hopeguide.controller;

import com.hopeguide.dto.MoodLogRequest;
import com.hopeguide.entity.MoodLog;
import com.hopeguide.service.MoodLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/moodlogs")
public class MoodLogController {

    private final MoodLogService moodLogService;

    public MoodLogController(MoodLogService moodLogService) {
        this.moodLogService = moodLogService;
    }

    @PostMapping
    public ResponseEntity<MoodLog> log(@RequestBody MoodLogRequest req,
                                        @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(moodLogService.logMood(req, email));
    }

    @GetMapping("/history")
    public ResponseEntity<List<MoodLog>> history(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(moodLogService.getHistory(email));
    }
}
