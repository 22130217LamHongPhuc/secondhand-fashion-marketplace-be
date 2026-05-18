package com.be.controller;

import com.be.common.enums.OrderStatus;
import com.be.dto.response.*;
import com.be.service.AdminService;
import com.be.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final com.be.service.ProductService productService;
    private final OrderService orderService;

    @GetMapping("/dashboard/statistics")
    public ResponseEntity<AdminDashboardResponse> getStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // ============ USER MANAGEMENT ============
    
    @GetMapping("/users")
    public ResponseEntity<PagedResponse<UserResponse>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, limit);
        Page<UserResponse> users = adminService.getAllUsers(pageable)
                .map(UserResponse::fromEntity);
        return ResponseEntity.ok(PagedResponse.fromPage(users));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(UserResponse.fromEntity(adminService.getUserById(userId)));
    }

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam boolean isActive) {
        return ResponseEntity.ok(UserResponse.fromEntity(adminService.updateUserStatus(userId, isActive)));
    }

    @PostMapping("/users/{userId}/ban")
    public ResponseEntity<UserResponse> banUser(
            @PathVariable Long userId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : "Banned by admin";
        return ResponseEntity.ok(UserResponse.fromEntity(adminService.updateUserStatus(userId, false)));
    }

    @PostMapping("/users/{userId}/unban")
    public ResponseEntity<UserResponse> unbanUser(@PathVariable Long userId) {
        return ResponseEntity.ok(UserResponse.fromEntity(adminService.updateUserStatus(userId, true)));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @GetMapping("/users/statistics")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        AdminDashboardResponse stats = adminService.getDashboardStats();
        return ResponseEntity.ok(Map.of(
                "totalUsers", stats.getTotalUsers(),
                "newSellers", stats.getTotalSellers(),
                "lockedUsers", stats.getTotalUsers() - stats.getActiveUsers()
        ));
    }

    // ============ PRODUCT MANAGEMENT ============
    
    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getProducts() {
        List<ProductResponse> products = productService.getAllProducts().stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long productId) {
        return ResponseEntity.ok(ProductResponse.fromEntity(productService.getProductById(productId)));
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long productId) {
        adminService.deleteProduct(productId);
        return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
    }

    // ============ ORDER MANAGEMENT ============
    
    @GetMapping("/orders")
    public ResponseEntity<PagedResponse<OrderResponse>> getOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String status) {
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, limit);
        Page<OrderResponse> orders;
        
        if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("all")) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                orders = orderService.getOrdersByStatus(orderStatus, pageable)
                        .map(OrderResponse::fromEntity);
            } catch (IllegalArgumentException e) {
                orders = orderService.getAllOrders(pageable)
                        .map(OrderResponse::fromEntity);
            }
        } else {
            orders = orderService.getAllOrders(pageable)
                    .map(OrderResponse::fromEntity);
        }
        
        return ResponseEntity.ok(PagedResponse.fromPage(orders));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(OrderResponse.fromEntity(orderService.getOrderById(orderId)));
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");
        OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase());
        return ResponseEntity.ok(OrderResponse.fromEntity(orderService.updateOrderStatus(orderId, status)));
    }

    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "Cancelled by admin");
        return ResponseEntity.ok(OrderResponse.fromEntity(orderService.cancelOrder(orderId, reason)));
    }

    @GetMapping("/orders/statistics")
    public ResponseEntity<Map<String, Object>> getOrderStats() {
        AdminDashboardResponse stats = adminService.getDashboardStats();
        return ResponseEntity.ok(Map.of(
                "totalOrders", stats.getTotalOrders(),
                "pendingOrders", stats.getPendingOrders(),
                "totalRevenue", stats.getTotalRevenue()
        ));
    }

    @GetMapping("/orders/export")
    public ResponseEntity<String> exportOrders(@RequestParam(defaultValue = "csv") String format) {
        // Placeholder for export functionality
        return ResponseEntity.ok("Export functionality not yet implemented");
    }

    @GetMapping("/dashboard/activities")
    public ResponseEntity<List<Map<String, Object>>> getActivities() {
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }
}
