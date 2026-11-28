package com.activityual.tracking.dto;

import com.activityual.tracking.entity.TrackingRecord;
import com.activityual.tracking.entity.TrackingStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TrackingResponse(
        UUID id,
        UUID activityId,
        LocalDate trackingDate,
        TrackingStatus status,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
    public static TrackingResponse from(TrackingRecord record) {
        return new TrackingResponse(
                record.getId(),
                record.getActivityId(),
                record.getTrackingDate(),
                record.getStatus(),
                record.getNotes(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }
}
