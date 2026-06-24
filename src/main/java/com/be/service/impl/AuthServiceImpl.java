package com.be.service.impl;

import com.be.common.enums.AuthProvider;
import com.be.common.enums.UserRole;
import com.be.dto.request.auth.LoginRequest;
import com.be.dto.request.auth.RegisterRequest;
import com.be.dto.request.auth.VerificationRequest;
import com.be.dto.request.auth.ForgotPasswordRequest;
import com.be.dto.request.auth.ResetPasswordRequest;
import com.be.dto.request.auth.GoogleLoginRequest;
import com.be.dto.response.auth.AuthResponse;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import com.be.entity.Role;
import com.be.entity.User;
import com.be.entity.UserRoleMapping;
import com.be.entity.Wallet;
import com.be.repository.RoleRepository;
import com.be.repository.UserRepository;
import com.be.repository.UserRoleMappingRepository;
import com.be.repository.WalletRepository;
import com.be.security.JwtTokenProvider;
import com.be.service.AuthService;
import com.be.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleMappingRepository userRoleMappingRepository;
    private final WalletRepository walletRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    // Helper method to get role ID from user
    private Long getRoleId(User user) {
        if (user.getUserRoles() != null && !user.getUserRoles().isEmpty() && user.getUserRoles().get(0).getRole() != null) {
            return user.getUserRoles().get(0).getRole().getId();
        }
        return null;
    }

    // Helper method to get role name from user
    private String getRoleName(User user) {
        if (user.getRole() != null) {
            return user.getRole().name();
        }
        if (user.getUserRoles() != null && !user.getUserRoles().isEmpty() && user.getUserRoles().get(0).getRole() != null) {
            return user.getUserRoles().get(0).getRole().getName().name();
        }
        return UserRole.CUSTOMER.name();
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        java.util.Optional<User> existingUserOpt = userRepository.findByEmail(request.getEmail());
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            if (Boolean.TRUE.equals(existingUser.getIsActive())) {
                throw new IllegalArgumentException("Email đã được sử dụng");
            } else {
                userRepository.delete(existingUser);
                userRepository.flush();
            }
        }

        Role customerRole = roleRepository.findByName(UserRole.CUSTOMER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(UserRole.CUSTOMER).build()));

        // Generate 6-digit OTP code
        String otpCode = String.valueOf((int) ((Math.random() * 900000) + 100000));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

        // Save user as inactive (isActive = false) with verification code details
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .avatarUrl(request.getAvatarUrl())
                .authProvider(AuthProvider.EMAIL)
                .isActive(false) // Must verify via email code first
                .verificationCode(otpCode)
                .verificationCodeExpiresAt(expiry)
                .role(UserRole.CUSTOMER)
                .userRoles(new ArrayList<>())
                .build();

        User savedUser = userRepository.save(user);

        UserRoleMapping mapping = UserRoleMapping.builder()
                .user(savedUser)
                .role(customerRole)
                .build();
        UserRoleMapping savedMapping = userRoleMappingRepository.save(mapping);
        savedUser.getUserRoles().add(savedMapping);

        Wallet wallet = Wallet.builder()
                .user(savedUser)
                .balance(BigDecimal.ZERO)
                .build();
        walletRepository.save(wallet);

        // Send OTP verification email
        emailService.sendVerificationCode(savedUser.getEmail(), otpCode);

        // Return registration response (token is null until verified)
        return AuthResponse.builder()
                .token(null)
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(UserRole.CUSTOMER.name())
                .avatarUrl(savedUser.getAvatarUrl())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse verify(VerificationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tài khoản"));

        if (Boolean.TRUE.equals(user.getIsActive())) {
            throw new IllegalArgumentException("Tài khoản này đã được kích hoạt trước đó");
        }

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(request.getCode())) {
            throw new IllegalArgumentException("Mã xác thực không chính xác");
        }

        if (user.getVerificationCodeExpiresAt() == null || LocalDateTime.now().isAfter(user.getVerificationCodeExpiresAt())) {
            throw new IllegalArgumentException("Mã xác thực đã hết hạn. Vui lòng đăng ký lại.");
        }

        // Activate user and clear OTP details
        user.setIsActive(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        User activatedUser = userRepository.save(user);

        // Generate final JWT token
        String token = jwtTokenProvider.generateToken(activatedUser.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(activatedUser.getId())
                .email(activatedUser.getEmail())
                .fullName(activatedUser.getFullName())
                .role(getRoleName(activatedUser))
                .roleId(getRoleId(activatedUser))
                .avatarUrl(activatedUser.getAvatarUrl())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email hoặc mật khẩu không chính xác"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Email hoặc mật khẩu không chính xác");
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            if (user.getEmailVerifiedAt() == null) {
                throw new IllegalArgumentException("Tài khoản của bạn chưa được kích hoạt. Vui lòng xác thực email.");
            } else {
                throw new IllegalArgumentException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
            }
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(getRoleName(user))
                .roleId(getRoleId(user))
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tài khoản với email này"));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new IllegalArgumentException("Tài khoản chưa được kích hoạt. Vui lòng kích hoạt trước.");
        }

        // Generate 6-digit OTP code for password reset
        String otpCode = String.valueOf((int) ((Math.random() * 900000) + 100000));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

        user.setVerificationCode(otpCode);
        user.setVerificationCodeExpiresAt(expiry);
        userRepository.save(user);

        // Send reset code email
        emailService.sendForgotPasswordCode(user.getEmail(), otpCode);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tài khoản"));

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(request.getCode())) {
            throw new IllegalArgumentException("Mã xác thực không chính xác");
        }

        if (user.getVerificationCodeExpiresAt() == null || LocalDateTime.now().isAfter(user.getVerificationCodeExpiresAt())) {
            throw new IllegalArgumentException("Mã xác thực đã hết hạn. Vui lòng yêu cầu mã mới.");
        }

        // Update password and clear verification code details
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        String email;
        String name;
        String picture;
        String googleId;

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getIdToken();
            Map<String, Object> payload = restTemplate.getForObject(url, Map.class);
            
            if (payload == null || !payload.containsKey("email")) {
                throw new IllegalArgumentException("Xác thực Google thất bại hoặc Token không hợp lệ");
            }
            
            email = (String) payload.get("email");
            name = (String) payload.get("name");
            picture = (String) payload.get("picture");
            googleId = (String) payload.get("sub");
        } catch (Exception e) {
            throw new IllegalArgumentException("Xác thực Google thất bại: " + e.getMessage());
        }

        // Find existing user by email
        java.util.Optional<User> userOpt = userRepository.findByEmail(email);
        User user;

        if (userOpt.isPresent()) {
            user = userOpt.get();
            // If user was locked/disabled by admin, don't let them login
            if (Boolean.FALSE.equals(user.getIsActive()) && user.getEmailVerifiedAt() != null) {
                throw new IllegalArgumentException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
            }
            // If user exists but is inactive, activate them
            if (Boolean.FALSE.equals(user.getIsActive()) && user.getEmailVerifiedAt() == null) {
                user.setIsActive(true);
                user.setEmailVerifiedAt(LocalDateTime.now());
            }
            // Update details if they are empty
            if (user.getProviderId() == null) {
                user.setProviderId(googleId);
                user.setAuthProvider(AuthProvider.GOOGLE);
            }
            if (user.getAvatarUrl() == null && picture != null) {
                user.setAvatarUrl(picture);
            }
            user = userRepository.save(user);
        } else {
            // Register new user
            Role customerRole = roleRepository.findByName(UserRole.CUSTOMER)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(UserRole.CUSTOMER).build()));

            user = User.builder()
                    .email(email)
                    .fullName(name != null ? name : "Google User")
                    .avatarUrl(picture)
                    .authProvider(AuthProvider.GOOGLE)
                    .providerId(googleId)
                    .isActive(true)
                    .emailVerifiedAt(LocalDateTime.now())
                    .role(UserRole.CUSTOMER)
                    .userRoles(new java.util.ArrayList<>())
                    .build();

            User savedUser = userRepository.save(user);

            UserRoleMapping mapping = UserRoleMapping.builder()
                    .user(savedUser)
                    .role(customerRole)
                    .build();
            UserRoleMapping savedMapping = userRoleMappingRepository.save(mapping);
            savedUser.getUserRoles().add(savedMapping);

            Wallet wallet = Wallet.builder()
                    .user(savedUser)
                    .balance(BigDecimal.ZERO)
                    .build();
            walletRepository.save(wallet);

            user = savedUser;
        }

        // Generate final JWT token
        String token = jwtTokenProvider.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole() != null ? user.getRole().name() : UserRole.CUSTOMER.name())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
