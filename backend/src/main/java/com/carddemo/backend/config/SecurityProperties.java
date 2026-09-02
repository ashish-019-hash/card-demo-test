package com.carddemo.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "carddemo.security")
public record SecurityProperties(boolean csrfCookieSecure) { }
