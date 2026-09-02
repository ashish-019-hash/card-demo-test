package com.carddemo.backend.shared;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Consistent API error response")
public record ErrorResponse(String code, String message, String field, String traceId) { }
