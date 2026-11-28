package com.activityual.recommendation.controller;

import com.activityual.recommendation.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {
    private final RecommendationService service;

    public RecommendationController(RecommendationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll(
            Authentication authentication) {
        return ResponseEntity.ok(
                service.getRecommendations(authentication.getName()));
    }

    @GetMapping("/{recommendationId}")
    public ResponseEntity<Map<String, Object>> getOne(
            @PathVariable String recommendationId,
            Authentication authentication) {
        return ResponseEntity.ok(service.getRecommendation(
                authentication.getName(), recommendationId));
    }

    @PostMapping("/{recommendationId}/accept")
    public ResponseEntity<Map<String, Object>> accept(
            @PathVariable String recommendationId,
            Authentication authentication) {
        return ResponseEntity.ok(service.acceptRecommendation(
                authentication.getName(), recommendationId));
    }
}
