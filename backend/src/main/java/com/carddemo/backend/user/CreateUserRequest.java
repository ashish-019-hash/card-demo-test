package com.carddemo.backend.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User creation request. Required fields are evaluated in legacy source order: firstName, lastName, userId, password, role.")
public record CreateUserRequest(String userId, String firstName, String lastName,
                                @Schema(format = "password", writeOnly = true) String password,
                                String role) { }
