package com.carddemo.backend.config;

import com.carddemo.backend.user.User;
import com.carddemo.backend.user.UserRepository;
import com.carddemo.backend.user.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/** Explicitly opt-in development and test fixtures; production never enables this property. */
@Configuration
@ConditionalOnProperty(prefix = "carddemo.seed", name = "enabled", havingValue = "true")
public class SeedDataConfiguration {
    @Bean
    ApplicationRunner seedUsers(UserRepository users, PasswordEncoder passwordEncoder,
                                @Value("${carddemo.seed.admin-user-id}") String adminUserId,
                                @Value("${carddemo.seed.admin-password}") String adminPassword,
                                @Value("${carddemo.seed.regular-user-id}") String regularUserId,
                                @Value("${carddemo.seed.regular-password}") String regularPassword) {
        return arguments -> {
            seed(users, passwordEncoder, adminUserId, adminPassword, "Admin", "User", UserRole.ADMIN);
            seed(users, passwordEncoder, regularUserId, regularPassword, "Regular", "User", UserRole.REGULAR);
        };
    }

    @Transactional
    void seed(UserRepository users, PasswordEncoder passwordEncoder, String userId, String password,
              String firstName, String lastName, UserRole role) {
        users.findById(userId).orElseGet(() -> users.save(new User(userId, firstName, lastName,
                passwordEncoder.encode(password), role)));
    }
}
