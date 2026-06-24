package com.be.controller.admin;

import com.be.dto.response.ApiResponse;
import com.be.service.admin.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminOrderController {
    
    private final AdminOrderService adminOrderService;
    
    /**
     * Get all orders (admin view - read only)
     * Admin can view all orders across all shops for monitoring purposes
     */
    @GetMapping
    @SuppressWarnings("unchecked")
    public ResponseEntity<ApiResponse<Page<com.be.dto.response.OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String status
    ) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        Page<com.be.entity.Order> orders = (Page<com.be.entity.Order>) adminOrderService.getAllOrders(pageable, status);
        Page<com.be.dto.response.OrderResponse> response = orders.map(com.be.dto.response.OrderResponse::fromEntity);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách đơn hàng thành công"));
    }
    
    /**
     * Get order by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getOrderById(@PathVariable Long id) {
        var order = adminOrderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(order, "Lấy chi tiết đơn hàng thành công"));
    }
    
    /**
     * Get order statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<?>> getOrderStatistics() {
        var stats = adminOrderService.getOrderStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats, "Lấy thống kê đơn hàng thành công"));
    }
}
