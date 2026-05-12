package com.be.controller;

import com.be.dto.response.AdminDashboardResponse;
import com.be.entity.User;
import com.be.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final com.be.service.ProductService productService;

    @GetMapping("/dashboard/statistics")
    public ResponseEntity<AdminDashboardResponse> getStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    public ResponseEntity<com.be.dto.response.PagedResponse<User>> getUsers(Pageable pageable) {
        return ResponseEntity.ok(com.be.dto.response.PagedResponse.fromPage(adminService.getAllUsers(pageable)));
    }

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<User> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam boolean isActive) {
        return ResponseEntity.ok(adminService.updateUserStatus(userId, isActive));
    }

    @PostMapping("/users/{userId}/ban")
    public ResponseEntity<User> banUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.updateUserStatus(userId, false));
    }

    @PostMapping("/users/{userId}/unban")
    public ResponseEntity<User> unbanUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.updateUserStatus(userId, true));
    }

    @GetMapping("/users/statistics")
    public ResponseEntity<?> getUserStats() {
        return ResponseEntity.ok(adminService.getDashboardStats()); // Reusing dashboard stats for now
    }

    @GetMapping("/products")
    public ResponseEntity<?> getProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/dashboard/activities")
    public ResponseEntity<?> getActivities() {
        // Placeholder for now
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }
}
