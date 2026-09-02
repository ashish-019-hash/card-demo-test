package com.carddemo.backend.user;

import com.carddemo.backend.shared.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock UserRepository users;
    @Mock PasswordEncoder passwords;
    @InjectMocks UserService service;

    @Test
    void createReportsTheFirstMissingFieldInLegacyOrder() {
        var request = new CreateUserRequest(null, null, null, null, null);
        assertThatThrownBy(() -> service.create(request)).isInstanceOfSatisfying(ApiException.class, error -> {
            assertThat(error.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(error.getCode()).isEqualTo("VALIDATION_REQUIRED");
            assertThat(error.getField()).isEqualTo("firstName");
        });
    }

    @Test
    void normalizesUserIdsToUppercase() {
        when(users.findById("ADMIN001")).thenReturn(Optional.of(user("ADMIN001", 0)));
        assertThat(service.get("admin001").userId()).isEqualTo("ADMIN001");
    }

    @Test
    void rejectsUnchangedUpdate() {
        User user = user("USER0001", 3);
        when(users.findById("USER0001")).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> service.update("user0001", new UpdateUserRequest("First", "Last", null, "REGULAR", 3L)))
                .isInstanceOfSatisfying(ApiException.class, error -> assertThat(error.getCode()).isEqualTo("NO_CHANGES"));
    }

    @Test
    void rejectsAStaleVersionBeforeMutating() {
        when(users.findById("USER0001")).thenReturn(Optional.of(user("USER0001", 3)));
        assertThatThrownBy(() -> service.update("USER0001", new UpdateUserRequest("Changed", "Last", null, "REGULAR", 2L)))
                .isInstanceOfSatisfying(ApiException.class, error -> assertThat(error.getCode()).isEqualTo("VERSION_CONFLICT"));
    }

    @Test
    void validatesRoleBeforeVersionConflict() {
        assertThatThrownBy(() -> service.update("USER0001", new UpdateUserRequest("First", "Last", null, "UNKNOWN", 2L)))
                .isInstanceOfSatisfying(ApiException.class, error -> {
                    assertThat(error.getCode()).isEqualTo("VALIDATION_REQUIRED");
                    assertThat(error.getField()).isEqualTo("role");
                });
    }

    @Test
    void trimsNamesAndRejectsBcryptUnsafePasswordLength() {
        when(users.existsById("NEWUSER")).thenReturn(false);
        when(passwords.encode("secret")).thenReturn("hash");
        when(users.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertThat(service.create(new CreateUserRequest(" newuser ", " New ", " User ", "secret", "REGULAR")).firstName())
                .isEqualTo("New");
        assertThatThrownBy(() -> service.create(new CreateUserRequest("LONGPASS", "First", "Last", "x".repeat(73), "REGULAR")))
                .isInstanceOfSatisfying(ApiException.class, error -> assertThat(error.getField()).isEqualTo("password"));
    }

    private User user(String userId, long expectedVersion) {
        User user = new User(userId, "First", "Last", "hash", UserRole.REGULAR);
        try {
            var version = User.class.getDeclaredField("version");
            version.setAccessible(true);
            version.set(user, expectedVersion);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
        return user;
    }
}
