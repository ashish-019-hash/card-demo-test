package com.carddemo.backend.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A user record. Password values and hashes are never returned.")
public record UserResponse(String userId, String firstName, String lastName, UserRole role, long version) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getUserId(), user.getFirstName(), user.getLastName(), user.getRole(), user.getVersion());
    }
}
