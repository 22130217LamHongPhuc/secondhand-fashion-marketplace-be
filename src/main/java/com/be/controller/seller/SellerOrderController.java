package com.be.controller.seller;

import com.be.common.enums.OrderStatus;
import com.be.dto.response.ApiResponse;
import com.be.dto.response.seller.OrderListResponse;
import com.be.dto.response.seller.OrderDetailResponse;
import com.be.dto.response.seller.OrderActionResponse;
import com.be.service.seller.SellerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/seller/orders")
@RequiredArgsConstructor
public class SellerOrderController {
    private final SellerOrderService sellerOrderService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderListResponse>>> getListByPage(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerOrderService.getListByPage(lastId, page),
                "Get order list successfully"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getDetails(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerOrderService.getDetails(id),
                "Get order details successfully"
        ));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Page<OrderListResponse>>> getListByStatus(
            @RequestParam OrderStatus status,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerOrderService.getListByStatus(status, lastId, page),
                "Get order list by status successfully"
        ));
    }

    @GetMapping("/current-month")
    public ResponseEntity<ApiResponse<Page<OrderListResponse>>> getListByCurrentMonth(
            @RequestParam(defaultValue = "0") int page
    ) {
        LocalDate now = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(
                sellerOrderService.getListByMonth(now.getYear(), now.getMonthValue(), page),
                "Get current month order list successfully"
        ));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<OrderActionResponse>> confirmOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerOrderService.confirmOrder(id),
                "Confirm order successfully"
        ));
    }

    @PutMapping("/{id}/delivery")
    public ResponseEntity<ApiResponse<OrderActionResponse>> startDelivery(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerOrderService.startDelivery(id),
                "Start delivery successfully"
        ));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<OrderActionResponse>> completeOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerOrderService.completeOrder(id),
                "Complete order successfully"
        ));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderActionResponse>> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerOrderService.cancelOrder(id, reason),
                "Cancel order successfully"
        ));
    }
}
