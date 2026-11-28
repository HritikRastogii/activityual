package com.activityual.auth.dto;

public record TokenValidationResponse(
        boolean valid,
        String subject
) {
}
