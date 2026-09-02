package com.carddemo.backend.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User update request. A supplied password replaces the existing password; an omitted password preserves it.")
public record UpdateUserRequest(String firstName, String lastName,
                                @Schema(format = "password", writeOnly = true) String password,
                                String role, Long version) { }
