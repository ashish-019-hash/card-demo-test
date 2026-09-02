package com.carddemo.backend.config;

import com.carddemo.backend.shared.ErrorResponse;
import com.carddemo.backend.shared.TraceIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import com.carddemo.backend.auth.CurrentUserAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import java.io.IOException;
import org.slf4j.MDC;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    CsrfTokenRepository csrfTokenRepository(SecurityProperties securityProperties) {
        var repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieCustomizer(cookie -> cookie.sameSite("Strict").secure(securityProperties.csrfCookieSecure()));
        return repository;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper,
                                            CsrfTokenRepository csrfTokenRepository,
                                            CurrentUserAuthenticationFilter currentUserAuthenticationFilter) throws Exception {
        return http
                .csrf(csrfConfig -> csrfConfig.csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers(request -> HttpMethod.POST.matches(request.getMethod())
                                && request.getRequestURI().equals("/api/session")))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api-docs", "/api-docs/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/session").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(currentUserAuthenticationFilter, AuthorizationFilter.class)
                .headers(headers -> headers.frameOptions(frame -> frame.deny()))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> writeError(response, objectMapper, 401, "UNAUTHORIZED", "Authentication is required."))
                        .accessDeniedHandler((request, response, exception) -> writeError(response, objectMapper, 403, "FORBIDDEN", "Administrator access is required.")))
                .build();
    }

    private void writeError(HttpServletResponse response, ObjectMapper mapper, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(), new ErrorResponse(code, message, null, MDC.get(TraceIdFilter.MDC_KEY)));
    }
}
