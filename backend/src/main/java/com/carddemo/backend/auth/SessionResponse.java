package com.carddemo.backend.auth;

import com.carddemo.backend.user.UserResponse;

public record SessionResponse(UserResponse user) { }
