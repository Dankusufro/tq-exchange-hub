package com.tq.exchangehub.service;

import com.tq.exchangehub.dto.AuthResponse;
import com.tq.exchangehub.dto.ForgotPasswordRequest;
import com.tq.exchangehub.dto.LoginRequest;
import com.tq.exchangehub.dto.RefreshTokenRequest;
import com.tq.exchangehub.dto.RegisterRequest;
import com.tq.exchangehub.dto.ResetPasswordRequest;
import com.tq.exchangehub.entity.UserAccount;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    AuthResponse createSession(UserAccount account);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
