package com.carddemo.backend.auth;

import com.carddemo.backend.user.User;
import com.carddemo.backend.user.UserResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public final class UserPrincipal implements UserDetails {
    private final UserResponse user;
    private final String passwordHash;

    private UserPrincipal(User user) {
        this.user = UserResponse.from(user);
        this.passwordHash = user.getPasswordHash();
    }

    public static UserPrincipal from(User user) { return new UserPrincipal(user); }
    public UserResponse user() { return user; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name()));
    }
    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return user.userId(); }
}
