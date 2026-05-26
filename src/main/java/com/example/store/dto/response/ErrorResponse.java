package com.example.store.dto.response;

import java.time.Instant;

public record ErrorResponse(Instant timestamp, int status, String error, String message, String path, Object details) {}
