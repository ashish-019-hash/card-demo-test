package com.carddemo.backend.user;

import com.carddemo.backend.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class UserService {
    private static final int USER_ID_MAX_LENGTH = 64;
    private static final int NAME_MAX_LENGTH = 128;
    private static final int BCRYPT_PASSWORD_MAX_UTF8_BYTES = 72;
    private static final Pattern USER_ID_PATTERN = Pattern.compile("[A-Z0-9][A-Z0-9._-]*");

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse get(String userId) {
        return UserResponse.from(find(normalizeUserId(userId)));
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String firstName = normalizeName("firstName", request.firstName(), "First name is required.");
        String lastName = normalizeName("lastName", request.lastName(), "Last name is required.");
        String userId = normalizeUserId(request.userId());
        String password = validatePassword(request.password());
        UserRole role = UserRole.fromExternal(request.role());
        if (users.existsById(userId)) throw duplicate();
        return UserResponse.from(users.saveAndFlush(new User(userId, firstName, lastName,
                passwordEncoder.encode(password), role)));
    }

    @Transactional
    public UserResponse update(String userId, UpdateUserRequest request) {
        normalizeUserId(userId);
        String firstName = normalizeName("firstName", request.firstName(), "First name is required.");
        String lastName = normalizeName("lastName", request.lastName(), "Last name is required.");
        String password = request.password() == null ? null : validatePassword(request.password());
        UserRole role = UserRole.fromExternal(request.role());
        User user = find(normalizeUserId(userId));
        if (request.version() == null || request.version() != user.getVersion()) throw versionConflict();
        boolean passwordChanged = password != null && !passwordEncoder.matches(password, user.getPasswordHash());
        boolean changed = !user.getFirstName().equals(firstName)
                || !user.getLastName().equals(lastName)
                || user.getRole() != role
                || passwordChanged;
        if (!changed) throw new ApiException(HttpStatus.CONFLICT, "NO_CHANGES", "Please modify a value before updating.", null);
        user.update(firstName, lastName, passwordChanged ? passwordEncoder.encode(password) : user.getPasswordHash(), role);
        return UserResponse.from(users.saveAndFlush(user));
    }

    @Transactional
    public void delete(String userId, Long version) {
        String normalizedUserId = normalizeUserId(userId);
        if (version == null) throw required("version", "Version is required.");
        User user = find(normalizedUserId);
        if (version != user.getVersion()) throw versionConflict();
        users.delete(user);
        users.flush();
    }

    public String normalizeUserId(String userId) {
        require("userId", userId, "User ID is required.");
        String normalized = userId.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() > USER_ID_MAX_LENGTH || !USER_ID_PATTERN.matcher(normalized).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_REQUIRED",
                    "User ID must use 1-64 uppercase letters, digits, periods, hyphens, or underscores.", "userId");
        }
        return normalized;
    }

    private String normalizeName(String field, String value, String requiredMessage) {
        require(field, value, requiredMessage);
        String trimmed = value.trim();
        if (trimmed.length() > NAME_MAX_LENGTH) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_REQUIRED",
                    "Name must be 1-128 characters.", field);
        }
        return trimmed;
    }

    private String validatePassword(String password) {
        require("password", password, "Password is required.");
        if (password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_PASSWORD_MAX_UTF8_BYTES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_REQUIRED",
                    "Password must not exceed 72 UTF-8 bytes.", "password");
        }
        return password;
    }

    private User find(String userId) {
        return users.findById(userId).orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User ID was not found.", "userId"));
    }

    private void require(String field, String value, String message) {
        if (value == null || value.isBlank()) throw required(field, message);
    }

    private ApiException required(String field, String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_REQUIRED", message, field);
    }

    private ApiException duplicate() {
        return new ApiException(HttpStatus.CONFLICT, "DUPLICATE_USER", "User ID already exists.", "userId");
    }

    private ApiException versionConflict() {
        return new ApiException(HttpStatus.CONFLICT, "VERSION_CONFLICT", "The user was changed by another request.", "version");
    }
}
