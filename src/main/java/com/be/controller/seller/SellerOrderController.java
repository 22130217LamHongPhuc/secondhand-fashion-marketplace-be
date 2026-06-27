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

    @GetMapping("all")
    public ResponseEntity<ApiResponse<Page<OrderListResponse>>> getListByPage(
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerOrderService.getListByPage(page),
                "Lấy danh sách đơn hàng thành công"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getDetails(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerOrderService.getDetails(id),
                "Lấy thông tin chi tiết đơn hàng thành công"
        ));
    }

    @GetMapping()
    public ResponseEntity<ApiResponse<Page<OrderListResponse>>> getListByStatusAndOrderCode(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String orderCode
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerOrderService.getListByStatusAndOrderCode(status, orderCode, page),
                "Lấy danh sách đơn hàng theo trạng thái thành công"
        ));
    }

    @GetMapping("/current-month")
    public ResponseEntity<ApiResponse<Page<OrderListResponse>>> getListByCurrentMonth(
            @RequestParam(defaultValue = "0") int page
    ) {
        LocalDate now = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(
                sellerOrderService.getListByMonth(now.getYear(), now.getMonthValue(), page),
                "Lấy danh sách đơn hàng trong tháng thành công"
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
                "Hủy đơn hàng thành công"
        ));
    }
}
