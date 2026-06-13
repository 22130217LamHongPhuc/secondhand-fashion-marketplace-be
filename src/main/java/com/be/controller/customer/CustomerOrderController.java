package com.be.controller.customer;

import com.be.common.enums.OrderStatus;
import com.be.dto.response.ApiResponse;
import com.be.dto.response.customer.OrderDetailResponse;
import com.be.dto.response.customer.OrderHistoryPageResponse;
import com.be.service.customer.CustomerOrderService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/customer/orders")
@CrossOrigin(origins = "*")
public class CustomerOrderController {

    private final CustomerOrderService customerOrderService;


    @GetMapping
    public ResponseEntity<ApiResponse<OrderHistoryPageResponse>> getOrderHistory(
            @RequestParam Long customerId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size
    ) {
        OrderStatus orderStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                orderStatus = OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(ApiResponse.success(null,
                        "Trạng thái không hợp lệ. Giá trị hợp lệ: PENDING, CONFIRMED, SHIPPING, DONE, CANCELLED"));
            }
        }

        OrderHistoryPageResponse result = customerOrderService.getOrderHistory(customerId, orderStatus, page, size);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy lịch sử đơn hàng thành công"));
    }


    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
            @PathVariable Long orderId,
            @RequestParam Long customerId
    ) {
        OrderDetailResponse result = customerOrderService.getOrderDetail(customerId, orderId);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy chi tiết đơn hàng thành công"));
    }


    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam Long customerId,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String reason = body != null ? body.get("reason") : null;
        OrderDetailResponse result = customerOrderService.cancelOrder(customerId, orderId, reason);
        return ResponseEntity.ok(ApiResponse.success(result, "Hủy đơn hàng thành công"));
    }
}
