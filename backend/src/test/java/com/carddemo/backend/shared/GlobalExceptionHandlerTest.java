package com.carddemo.backend.shared;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsOnlyTheNamedPrimaryKeyConstraintToDuplicateUser() {
        var exception = new DataIntegrityViolationException("race", new ConstraintViolationException(
                "duplicate", new SQLException("duplicate"), "users_pkey"));
        var response = handler.integrity(exception);
        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody().code()).isEqualTo("DUPLICATE_USER");
    }

    @Test
    void leavesOtherConstraintFailuresAsInternalErrors() {
        var exception = new DataIntegrityViolationException("bad role", new ConstraintViolationException(
                "check", new SQLException("check"), "users_role_check"));
        var response = handler.integrity(exception);
        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
    }
}
