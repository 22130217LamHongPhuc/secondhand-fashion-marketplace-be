package com.be.controller.customer;

import com.be.common.enums.OrderStatus;
import com.be.dto.request.customer.CheckoutRequest;
import com.be.dto.response.ApiResponse;
import com.be.dto.response.customer.OrderDetailResponse;
import com.be.dto.response.customer.OrderHistoryPageResponse;
import com.be.service.customer.CustomerOrderService;
import com.be.security.JwtTokenProvider;
import com.be.repository.UserRepository;
import com.be.entity.User;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/customer/orders")
@CrossOrigin(origins = "*")
public class CustomerOrderController {

    private final CustomerOrderService customerOrderService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    private Long resolveCustomerId(Long customerId, String authHeader) {
        if (customerId != null) {
            return customerId;
        }
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtTokenProvider.validateToken(token)) {
                try {
                    String email = jwtTokenProvider.getEmailFromToken(token);
                    return userRepository.findByEmail(email)
                            .map(User::getId)
                            .orElse(null);
                } catch (Exception e) {
                    // Token parsing failed or user not found
                }
            }
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<OrderHistoryPageResponse>> getOrderHistory(
            @RequestParam(required = false) Long customerId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size
    ) {
        Long resolvedCustomerId = resolveCustomerId(customerId, authHeader);
        if (resolvedCustomerId == null) {
            return ResponseEntity.status(401).body(ApiResponse.success(null, "Yêu cầu đăng nhập để xem lịch sử đơn hàng."));
        }

        OrderStatus orderStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                orderStatus = OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(ApiResponse.success(null,
                        "Trạng thái không hợp lệ. Giá trị hợp lệ: PENDING, CONFIRMED, SHIPPING, DONE, CANCELLED"));
            }
        }

        OrderHistoryPageResponse result = customerOrderService.getOrderHistory(resolvedCustomerId, orderStatus, page, size);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy lịch sử đơn hàng thành công"));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
            @PathVariable Long orderId,
            @RequestParam(required = false) Long customerId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long resolvedCustomerId = resolveCustomerId(customerId, authHeader);
        if (resolvedCustomerId == null) {
            return ResponseEntity.status(401).body(ApiResponse.success(null, "Yêu cầu đăng nhập để xem chi tiết đơn hàng."));
        }
        OrderDetailResponse result = customerOrderService.getOrderDetail(resolvedCustomerId, orderId);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy chi tiết đơn hàng thành công"));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) Long customerId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, String> body
    ) {
        Long resolvedCustomerId = resolveCustomerId(customerId, authHeader);
        if (resolvedCustomerId == null) {
            return ResponseEntity.status(401).body(ApiResponse.success(null, "Yêu cầu đăng nhập để hủy đơn hàng."));
        }
        String reason = body != null ? body.get("reason") : null;
        OrderDetailResponse result = customerOrderService.cancelOrder(resolvedCustomerId, orderId, reason);
        return ResponseEntity.ok(ApiResponse.success(result, "Hủy đơn hàng thành công"));
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<com.be.dto.response.customer.CheckoutResponse>> checkout(
            @Validated @RequestBody CheckoutRequest request
    ) {
        com.be.dto.response.customer.CheckoutResponse result = customerOrderService.checkout(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Đặt hàng thành công"));
    }
}

