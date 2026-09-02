package com.carddemo.backend.user;

import com.carddemo.backend.shared.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Provisional role vocabulary for the bounded identity slice. Replace this mapping only when
 * COCOM01Y and CSUSR01Y are recovered and their role codes are confirmed.
 */
public enum UserRole {
    ADMIN,
    REGULAR;

    public static UserRole fromExternal(String value) {
        if (value == null || value.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_REQUIRED", "User type is required.", "role");
        }
        try {
            return valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_REQUIRED",
                    "User type must be one of: ADMIN, REGULAR.", "role");
        }
    }
}
