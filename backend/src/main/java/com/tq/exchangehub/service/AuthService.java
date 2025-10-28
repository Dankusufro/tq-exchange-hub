package com.tq.exchangehub.service;

import com.tq.exchangehub.dto.AuthResponse;
import com.tq.exchangehub.dto.LoginRequest;
import com.tq.exchangehub.dto.RegisterRequest;
import com.tq.exchangehub.dto.SessionResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    SessionResponse getCurrentSession();
}
