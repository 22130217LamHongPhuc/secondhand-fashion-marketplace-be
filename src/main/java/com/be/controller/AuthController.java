package com.be.controller;

import com.be.dto.request.auth.LoginRequest;
import com.be.dto.request.auth.RegisterRequest;
import com.be.dto.request.auth.ForgotPasswordRequest;
import com.be.dto.request.auth.ResetPasswordRequest;
import com.be.dto.request.auth.GoogleLoginRequest;
import com.be.dto.response.ApiResponse;
import com.be.dto.response.auth.AuthResponse;
import com.be.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Validated @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Mã xác thực đăng ký đã được gửi vào email của bạn"));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verify(@Validated @RequestBody com.be.dto.request.auth.VerificationRequest request) {
        AuthResponse response = authService.verify(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Kích hoạt tài khoản thành công"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Validated @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Đăng nhập thành công"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Validated @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Mã xác thực đặt lại mật khẩu đã được gửi vào email của bạn"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Validated @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Đặt lại mật khẩu thành công"));
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithGoogle(@Validated @RequestBody GoogleLoginRequest request) {
        AuthResponse response = authService.loginWithGoogle(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Đăng nhập bằng Google thành công"));
    }
}
