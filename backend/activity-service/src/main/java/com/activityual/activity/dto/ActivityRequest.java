package com.activityual.activity.dto;

import com.activityual.activity.entity.Frequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ActivityRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 1000) String description,
        @NotNull Frequency frequency,
        @NotBlank @Size(max = 60) String category,
        @Size(max = 2000) String notes,
        Boolean active
) {}
