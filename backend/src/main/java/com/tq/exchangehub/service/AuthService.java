package com.tq.exchangehub.service;

import com.tq.exchangehub.dto.AuthResponse;
import com.tq.exchangehub.dto.LoginRequest;
import com.tq.exchangehub.dto.RefreshTokenRequest;
import com.tq.exchangehub.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);
}
