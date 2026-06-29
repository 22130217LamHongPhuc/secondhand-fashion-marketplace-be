package com.be.controller.admin;

import com.be.common.enums.OrderStatus;
import com.be.dto.request.CategoryRequest;
import com.be.dto.response.*;
import com.be.entity.Category;
import com.be.service.AdminService;
import com.be.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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
    private final com.be.repository.UserRepository userRepository;

    @GetMapping("/dashboard/statistics")
    public ResponseEntity<AdminDashboardResponse> getStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // ============ USER MANAGEMENT ============
    
    @GetMapping("/users")
    public ResponseEntity<PagedResponse<UserResponse>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, limit);
        com.be.common.enums.UserRole userRole = null;
        if (role != null && !role.isEmpty() && !role.equalsIgnoreCase("all")) {
            try {
                userRole = com.be.common.enums.UserRole.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid role
            }
        }
        Page<UserResponse> users = adminService.getAllUsers(userRole, search, pageable)
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

    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable Long userId,
            @RequestParam String role) {
        return ResponseEntity.ok(UserResponse.fromEntity(adminService.updateUserRole(userId, role)));
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
        long totalUsers = userRepository.count();
        long totalSellers = userRepository.countByRole(com.be.common.enums.UserRole.SELLER);
        long totalCustomers = userRepository.countByRole(com.be.common.enums.UserRole.CUSTOMER);
        long activeUsers = userRepository.countByIsActiveTrue();
        long lockedUsers = totalUsers - activeUsers;
        return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "totalSellers", totalSellers,
                "totalCustomers", totalCustomers,
                "lockedUsers", lockedUsers
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

    @PutMapping("/products/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long productId,
            @RequestBody com.be.dto.request.ProductRequest request) {
        return ResponseEntity.ok(ProductResponse.fromEntity(productService.updateProduct(productId, request)));
    }

    @PutMapping("/products/{productId}/active")
    public ResponseEntity<ProductResponse> toggleProductActive(
            @PathVariable Long productId,
            @RequestParam boolean active) {
        return ResponseEntity.ok(ProductResponse.fromEntity(productService.toggleProductActive(productId, active)));
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
        Pageable pageable = PageRequest.of(0, 10, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        List<Map<String, Object>> activities = orderService.getAllOrders(pageable).stream()
                .map(order -> Map.<String, Object>of(
                        "timestamp", order.getCreatedAt(),
                        "description", "Đơn hàng #" + order.getOrderCode() + " chuyển sang trạng thái " + order.getStatus().name(),
                        "type", "ORDER_STATUS"
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(activities);
    }

    // ============ CATEGORY MANAGEMENT API ============
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(adminService.getAllCategories().stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList()));
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
        return ResponseEntity.ok(CategoryResponse.fromEntity(adminService.saveCategory(toEntity(request, null))));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(CategoryResponse.fromEntity(adminService.saveCategory(toEntity(request, id))));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Map<String, String>> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return ResponseEntity.ok(Map.of("message", "Category deleted successfully"));
    }

    // ============ SHOP MANAGEMENT API ============
    @GetMapping("/shops")
    public ResponseEntity<List<com.be.entity.Shop>> getShops() {
        return ResponseEntity.ok(adminService.getAllShops());
    }

    @PutMapping("/shops/{id}/verify")
    public ResponseEntity<com.be.entity.Shop> verifyShop(
            @PathVariable Long id,
            @RequestParam boolean verify) {
        return ResponseEntity.ok(adminService.verifyShop(id, verify));
    }

    @PutMapping("/shops/{id}/strike")
    public ResponseEntity<com.be.entity.Shop> addStrikeShop(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.addWarningStrike(id));
    }

    @PutMapping("/shops/{id}/active")
    public ResponseEntity<com.be.entity.Shop> toggleShopActive(
            @PathVariable Long id,
            @RequestParam boolean active) {
        return ResponseEntity.ok(adminService.toggleShopActive(id, active));
    }

    @PutMapping("/shops/{id}/reset-strikes")
    public ResponseEntity<com.be.entity.Shop> resetStrikes(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.resetStrikes(id));
    }

    // ============ COMPLAINT MANAGEMENT API ============
    @GetMapping("/complaints")
    public ResponseEntity<List<com.be.entity.Complaint>> getComplaints() {
        return ResponseEntity.ok(adminService.getAllComplaints());
    }

    @PutMapping("/complaints/{id}/status")
    public ResponseEntity<com.be.entity.Complaint> updateComplaintStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");
        String resolution = body.get("resolution");
        com.be.common.enums.ComplaintStatus status = com.be.common.enums.ComplaintStatus.valueOf(statusStr.toUpperCase());
        return ResponseEntity.ok(adminService.updateComplaintStatus(id, status, resolution));
    }

    private Category toEntity(CategoryRequest request, Long id) {
        Category category = Category.builder()
                .id(id)
                .name(request.getName())
                .slug(request.getSlug())
                .iconUrl(request.getIconUrl())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        if (request.getParentId() != null) {
            category.setParent(Category.builder().id(request.getParentId()).build());
        }

        return category;
    }
}
