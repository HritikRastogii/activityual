package com.activityual.activity.dto;

import com.activityual.activity.entity.Activity;
import com.activityual.activity.entity.Frequency;
import java.time.Instant;
import java.util.UUID;

public record ActivityResponse(
        UUID id,
        String name,
        String description,
        Frequency frequency,
        String category,
        String notes,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static ActivityResponse from(Activity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getName(),
                activity.getDescription(),
                activity.getFrequency(),
                activity.getCategory(),
                activity.getNotes(),
                activity.isActive(),
                activity.getCreatedAt(),
                activity.getUpdatedAt()
        );
    }
}
