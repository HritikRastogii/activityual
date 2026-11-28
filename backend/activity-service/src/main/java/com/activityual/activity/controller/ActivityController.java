package com.activityual.activity.controller;

import com.activityual.activity.dto.ActivityRequest;
import com.activityual.activity.dto.ActivityResponse;
import com.activityual.activity.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping
    public ResponseEntity<ActivityResponse> create(
            Authentication authentication,
            @Valid @RequestBody ActivityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(activityService.create(authentication.getName(), request));
    }

    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getAll(Authentication authentication) {
        return ResponseEntity.ok(activityService.getAll(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponse> getById(
            Authentication authentication,
            @PathVariable UUID id) {
        return ResponseEntity.ok(activityService.getById(authentication.getName(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityResponse> update(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody ActivityRequest request) {
        return ResponseEntity.ok(
                activityService.update(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            Authentication authentication,
            @PathVariable UUID id) {
        activityService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
