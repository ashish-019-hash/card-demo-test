package com.carddemo.backend.auth;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/session")
public class SessionController {
    private final SessionService sessions;

    public SessionController(SessionService sessions) { this.sessions = sessions; }

    @PostMapping
    @Operation(summary = "Create a browser session")
    public SessionResponse create(@RequestBody SessionRequest request, HttpServletRequest httpRequest,
                                  HttpServletResponse httpResponse) {
        return sessions.authenticate(request, httpRequest, httpResponse);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Inspect the current browser session")
    public SessionResponse current(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        return sessions.current(httpRequest, httpResponse);
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "End the current browser session")
    public ResponseEntity<Void> delete(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }
}
