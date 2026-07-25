package com.hopeguide.controller;

import com.hopeguide.entity.GuideResource;
import com.hopeguide.service.ResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    public ResponseEntity<List<GuideResource>> all() {
        return ResponseEntity.ok(resourceService.getAll());
    }

    @GetMapping("/category/{cat}")
    public ResponseEntity<List<GuideResource>> byCategory(@PathVariable String cat) {
        return ResponseEntity.ok(resourceService.getByCategory(cat));
    }

    @GetMapping("/search")
    public ResponseEntity<List<GuideResource>> search(@RequestParam String q) {
        return ResponseEntity.ok(resourceService.search(q));
    }

    @PostMapping("/explain")
    public ResponseEntity<Map<String, String>> explain(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(resourceService.explain(body.get("question")));
    }
}
