package com.be.dto.response;

import com.be.common.enums.AuthProvider;
import com.be.common.enums.UserRole;
import com.be.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String fullName;
    private String phone;
    private String avatar;
    private String avatarUrl;
    private UserRole role;
    private String status;
    private Boolean isActive;
    private AuthProvider authProvider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime emailVerifiedAt;
    private Integer totalOrders;
    private Long totalSpent;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getFullName())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatar(user.getAvatarUrl())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .status(user.getIsActive() ? "active" : "banned")
                .isActive(user.getIsActive())
                .authProvider(user.getAuthProvider())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .emailVerifiedAt(user.getEmailVerifiedAt())
                .totalOrders(user.getOrders() != null ? user.getOrders().size() : 0)
                .totalSpent(0L)
                .build();
    }
}
