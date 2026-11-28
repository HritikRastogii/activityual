package com.activityual.tracking.dto;

import com.activityual.tracking.entity.TrackingStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record TrackingRequest(
        @NotNull UUID activityId,
        @NotNull LocalDate trackingDate,
        @NotNull TrackingStatus status,
        @Size(max = 1000) String notes
) {}
