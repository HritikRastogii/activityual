package com.activityual.analytics.controller;

import com.activityual.analytics.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, AttributeValue>>> getAnalytics(
            Authentication authentication) {
        return ResponseEntity.ok(
                analyticsService.getUserAnalytics(authentication.getName()));
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<Map<String, AttributeValue>> getActivityAnalytics(
            @PathVariable String activityId,
            Authentication authentication) {
        return ResponseEntity.ok(
                analyticsService.getActivityAnalytics(
                        authentication.getName(), activityId));
    }
}
