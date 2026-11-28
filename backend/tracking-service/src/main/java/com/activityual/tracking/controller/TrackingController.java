package com.activityual.tracking.controller;

import com.activityual.tracking.dto.TrackingRequest;
import com.activityual.tracking.dto.TrackingResponse;
import com.activityual.tracking.service.TrackingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tracking")
public class TrackingController {
    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @PostMapping
    public ResponseEntity<TrackingResponse> create(
            Authentication authentication,
            @Valid @RequestBody TrackingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trackingService.create(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrackingResponse> update(
            Authentication authentication, @PathVariable UUID id,
            @Valid @RequestBody TrackingRequest request) {
        return ResponseEntity.ok(
                trackingService.update(authentication.getName(), id, request));
    }

    @GetMapping
    public ResponseEntity<List<TrackingResponse>> getAll(
            Authentication authentication,
            @RequestParam(required = false) LocalDate date) {
        List<TrackingResponse> result = date == null
                ? trackingService.getAll(authentication.getName())
                : trackingService.getByDate(authentication.getName(), date);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrackingResponse> getById(
            Authentication authentication, @PathVariable UUID id) {
        return ResponseEntity.ok(
                trackingService.getById(authentication.getName(), id));
    }

    @GetMapping("/activity/{activityId}")
    public ResponseEntity<List<TrackingResponse>> getByActivity(
            Authentication authentication, @PathVariable UUID activityId) {
        return ResponseEntity.ok(
                trackingService.getByActivity(authentication.getName(), activityId));
    }
}
