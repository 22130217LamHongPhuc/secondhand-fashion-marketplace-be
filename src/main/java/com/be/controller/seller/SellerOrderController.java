package com.be.controller.seller;

import com.be.common.enums.OrderStatus;
import com.be.dto.response.ApiResponse;
import com.be.dto.response.seller.OrderListResponse;
import com.be.dto.response.seller.OrderDetailResponse;
import com.be.dto.response.seller.OrderActionResponse;
import com.be.entity.Shop;
import com.be.security.AuthHelper;
import com.be.service.OrderExportService;
import com.be.service.seller.SellerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/seller/orders")
@RequiredArgsConstructor
public class SellerOrderController {
    private final SellerOrderService sellerOrderService;
    private final OrderExportService orderExportService;
    private final AuthHelper authHelper;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getDetails(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerOrderService.getDetails(id),
                "Lấy thông tin chi tiết đơn hàng thành công"
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderListResponse>>> searchOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerOrderService.searchOrders(status, orderCode, fromDate, toDate, minPrice, maxPrice, sortBy, page),
                "Lấy danh sách đơn hàng thành công"
        ));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<OrderActionResponse>> confirmOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerOrderService.confirmOrder(id),
                "Xác nhận đơn hàng thành công"
        ));
    }

    @PutMapping("/{id}/delivery")
    public ResponseEntity<ApiResponse<OrderActionResponse>> startDelivery(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerOrderService.startDelivery(id),
                "Bắt đầu giao hàng thành công"
        ));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<OrderActionResponse>> completeOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerOrderService.completeOrder(id),
                "Hoàn thành đơn hàng thành công"
        ));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderActionResponse>> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerOrderService.cancelOrder(id, reason),
                "Đã hủy đơn hàng thành công"
        ));
    }

    @PostMapping("/export")
    public ResponseEntity<ApiResponse<Void>> exportOrders() {
        Shop shop = authHelper.getCurrentSellerShop();
        String subscriberId = authHelper.getCurrentUser().getId().toString();
        orderExportService.exportOrdersAsync(shop.getId(), subscriberId);
        return ResponseEntity.accepted().body(
                ApiResponse.success(null, "Đang xử lý export đơn hàng. Vui lòng theo dõi tiến trình.")
        );
    }
}
