package com.be.service;

import com.be.dto.response.AdminDashboardResponse;
import com.be.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.be.entity.Category;
import com.be.entity.Shop;
import com.be.entity.Complaint;
import com.be.common.enums.ComplaintStatus;
import java.util.List;

import com.be.common.enums.UserRole;

public interface AdminService {
    AdminDashboardResponse getDashboardStats();
    Page<User> getAllUsers(UserRole role, String search, Pageable pageable);
    User getUserById(Long userId);
    User updateUserStatus(Long userId, boolean isActive);
    User updateUserRole(Long userId, String role);
    void deleteUser(Long userId);
    void deleteProduct(Long productId);

    // ============ CATEGORY MANAGEMENT ============
    List<Category> getAllCategories();
    Category saveCategory(Category category);
    void deleteCategory(Long categoryId);

    // ============ SHOP MANAGEMENT ============
    List<Shop> getAllShops();
    Shop verifyShop(Long shopId, boolean verify);
    Shop addWarningStrike(Long shopId);
    Shop resetStrikes(Long shopId);
    Shop toggleShopActive(Long shopId, boolean active);

    // ============ COMPLAINT MANAGEMENT ============
    List<Complaint> getAllComplaints();
    Complaint updateComplaintStatus(Long complaintId, ComplaintStatus status, String resolution);
}
