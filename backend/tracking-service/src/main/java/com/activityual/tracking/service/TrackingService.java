package com.activityual.tracking.service;

import com.activityual.tracking.dto.TrackingRequest;
import com.activityual.tracking.dto.TrackingResponse;
import com.activityual.tracking.entity.OutboxEvent;
import com.activityual.tracking.entity.TrackingRecord;
import com.activityual.tracking.exception.ResourceNotFoundException;
import com.activityual.tracking.repository.OutboxRepository;
import com.activityual.tracking.repository.TrackingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class TrackingService {
    private final TrackingRepository repository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public TrackingService(TrackingRepository repository,
            OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    public TrackingResponse create(String email, TrackingRequest request) {
        var existing = repository.findByUserEmailAndActivityIdAndTrackingDate(
                email, request.activityId(), request.trackingDate());

        if (existing.isPresent()) {
            TrackingRecord record = existing.get();
            boolean statusChanged = record.getStatus() != request.status();
            record.setStatus(request.status());
            record.setNotes(request.notes());
            TrackingRecord saved = repository.saveAndFlush(record);
            if (statusChanged) {
                saveOutboxEvent(saved);
            }
            return TrackingResponse.from(saved);
        }

        TrackingRecord record = new TrackingRecord();
        record.setUserEmail(email);
        record.setActivityId(request.activityId());
        record.setTrackingDate(request.trackingDate());
        record.setStatus(request.status());
        record.setNotes(request.notes());
        TrackingRecord saved = repository.saveAndFlush(record);
        saveOutboxEvent(saved);
        return TrackingResponse.from(saved);
    }

    public TrackingResponse update(String email, UUID id, TrackingRequest request) {
        TrackingRecord record = findOwned(email, id);

        if (!record.getActivityId().equals(request.activityId())
                || !record.getTrackingDate().equals(request.trackingDate())) {
            throw new IllegalArgumentException(
                    "Activity ID and tracking date cannot be changed.");
        }

        record.setStatus(request.status());
        record.setNotes(request.notes());
        TrackingRecord saved = repository.save(record);
        saveOutboxEvent(saved);
        return TrackingResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TrackingResponse> getAll(String email) {
        return repository.findAllByUserEmailOrderByTrackingDateDesc(email)
                .stream().map(TrackingResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<TrackingResponse> getByDate(
            String email, java.time.LocalDate date) {
        return repository.findAllByUserEmailAndTrackingDateOrderByCreatedAtDesc(
                email, date).stream().map(TrackingResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<TrackingResponse> getByActivity(String email, UUID activityId) {
        return repository.findAllByUserEmailAndActivityIdOrderByTrackingDateDesc(
                email, activityId).stream().map(TrackingResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TrackingResponse getById(String email, UUID id) {
        return TrackingResponse.from(findOwned(email, id));
    }

    private TrackingRecord findOwned(String email, UUID id) {
        return repository.findByIdAndUserEmail(id, email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tracking record not found: " + id));
    }

    private void saveOutboxEvent(TrackingRecord record) {
        String eventType = record.getStatus() == com.activityual.tracking.entity.TrackingStatus.COMPLETED
                ? "ActivityCompleted" : "ActivityMissed";

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventId", UUID.randomUUID());
        payload.put("eventType", eventType);
        payload.put("userEmail", record.getUserEmail());
        payload.put("activityId", record.getActivityId());
        payload.put("trackingId", record.getId());
        payload.put("trackingDate", record.getTrackingDate());
        payload.put("status", record.getStatus());
        payload.put("occurredAt", Instant.now());

        try {
            UUID eventId = (UUID) payload.get("eventId");
            String json = objectMapper.writeValueAsString(payload);
            outboxRepository.save(new OutboxEvent(
                    eventId, eventType, "TrackingRecord",
                    record.getId().toString(), json));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize tracking event", exception);
        }
    }
}
