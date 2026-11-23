package com.duoc.notix.tramites.web;

import java.time.LocalDateTime;

public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message
) {
}
