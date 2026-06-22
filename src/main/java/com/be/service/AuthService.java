package com.be.service;

import com.be.dto.request.auth.LoginRequest;
import com.be.dto.request.auth.RegisterRequest;
import com.be.dto.request.auth.VerificationRequest;
import com.be.dto.request.auth.ForgotPasswordRequest;
import com.be.dto.request.auth.ResetPasswordRequest;
import com.be.dto.request.auth.GoogleLoginRequest;
import com.be.dto.response.auth.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse verify(VerificationRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    AuthResponse loginWithGoogle(GoogleLoginRequest request);
}
