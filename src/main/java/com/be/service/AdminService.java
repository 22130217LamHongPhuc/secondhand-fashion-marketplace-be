package com.be.service;

import com.be.dto.response.AdminDashboardResponse;
import com.be.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {
    AdminDashboardResponse getDashboardStats();
    Page<User> getAllUsers(Pageable pageable);
    User getUserById(Long userId);
    User updateUserStatus(Long userId, boolean isActive);
    void deleteUser(Long userId);
    void deleteProduct(Long productId);
}
