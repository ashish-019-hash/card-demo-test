package com.carddemo.backend.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    private final UserService users;

    public UserController(UserService users) { this.users = users; }

    @GetMapping("/{userId}")
    @Operation(summary = "Retrieve a user for administration")
    public UserResponse get(@PathVariable String userId) { return users.get(userId); }

    @PostMapping
    @Operation(summary = "Create a user")
    @ApiResponse(responseCode = "201", description = "User created")
    public ResponseEntity<UserResponse> create(@RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(users.create(request));
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update a user using optimistic locking")
    public UserResponse update(@PathVariable String userId, @RequestBody UpdateUserRequest request) {
        return users.update(userId, request);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete a user using optimistic locking")
    public ResponseEntity<Void> delete(@PathVariable String userId, @RequestParam Long version) {
        users.delete(userId, version);
        return ResponseEntity.noContent().build();
    }
}
