package com.it_support_ticket_system.demo.common;

import java.time.Instant;
import java.util.List;

public record ApiError(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path,
    List<ValidationErrorItem> validationErrors
) {
}
