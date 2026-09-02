package com.carddemo.backend.auth;

import com.carddemo.backend.shared.ErrorResponse;
import com.carddemo.backend.shared.TraceIdFilter;
import com.carddemo.backend.user.User;
import com.carddemo.backend.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Invalidates a session if its authenticated identity or role is no longer current. */
@Component
public class CurrentUserAuthenticationFilter extends OncePerRequestFilter {
    private final UserRepository users;
    private final ObjectMapper objectMapper;

    public CurrentUserAuthenticationFilter(UserRepository users, ObjectMapper objectMapper) {
        this.users = users;
        this.objectMapper = objectMapper;
    }

    /** Prevent Boot from registering this security-chain filter with the servlet container as well. */
    @Bean
    FilterRegistrationBean<CurrentUserAuthenticationFilter> currentUserAuthenticationFilterRegistration() {
        FilterRegistrationBean<CurrentUserAuthenticationFilter> registration = new FilterRegistrationBean<>(this);
        registration.setEnabled(false);
        return registration;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            User persisted = users.findById(principal.getUsername()).orElse(null);
            if (persisted == null || persisted.getRole() != principal.user().role()) {
                invalidate(request);
                unauthorized(response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private void invalidate(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new ErrorResponse(
                "UNAUTHORIZED", "Authentication is required.", null, MDC.get(TraceIdFilter.MDC_KEY)));
    }
}
