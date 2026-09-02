package com.carddemo.backend.auth;

import com.carddemo.backend.shared.ApiException;
import com.carddemo.backend.user.User;
import com.carddemo.backend.user.UserRepository;
import com.carddemo.backend.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Service;

@Service
public class SessionService {
    private final UserRepository users;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final CsrfTokenRepository csrfTokens;
    private final SecurityContextHolderStrategy contextHolder = SecurityContextHolder.getContextHolderStrategy();

    public SessionService(UserRepository users, UserService userService, PasswordEncoder passwordEncoder,
                          CsrfTokenRepository csrfTokens) {
        this.users = users;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.csrfTokens = csrfTokens;
    }

    public SessionResponse authenticate(SessionRequest request, jakarta.servlet.http.HttpServletRequest httpRequest,
                                        jakarta.servlet.http.HttpServletResponse httpResponse) {
        require("userId", request.userId(), "User ID is required.");
        require("password", request.password(), "Password is required.");
        String userId;
        try {
            userId = userService.normalizeUserId(request.userId());
        } catch (ApiException exception) {
            throw invalidCredentials();
        }
        User user = users.findById(userId).orElseThrow(this::invalidCredentials);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }
        UserPrincipal principal = UserPrincipal.from(user);
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContext context = new SecurityContextImpl(authentication);
        contextHolder.setContext(context);
        // The initial login has no session to rotate yet; create it before requesting rotation.
        var session = httpRequest.getSession(true);
        httpRequest.changeSessionId();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        issueCsrfToken(httpRequest, httpResponse);
        return new SessionResponse(principal.user());
    }

    public SessionResponse current(jakarta.servlet.http.HttpServletRequest httpRequest,
                                   jakarta.servlet.http.HttpServletResponse httpResponse) {
        var authentication = contextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication is required.", null);
        }
        issueCsrfToken(httpRequest, httpResponse);
        return new SessionResponse(principal.user());
    }

    private void issueCsrfToken(jakarta.servlet.http.HttpServletRequest request,
                                jakarta.servlet.http.HttpServletResponse response) {
        // Explicitly persist a fresh token so the browser receives the readable XSRF-TOKEN
        // cookie after both login and session inspection.
        csrfTokens.saveToken(csrfTokens.generateToken(request), request, response);
    }

    private void require(String field, String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_REQUIRED", message, field);
        }
    }

    private ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid user ID or password.", null);
    }
}
