package com.carddemo.backend.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credentials for establishing a browser session. Passwords are case-sensitive by intentional security deviation.")
public record SessionRequest(String userId, @Schema(format = "password", writeOnly = true) String password) { }
