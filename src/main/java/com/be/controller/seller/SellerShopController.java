package com.be.controller.seller;

import com.be.dto.request.seller.ShopCreateRequest;
import com.be.dto.request.seller.ShopUpdateRequest;
import com.be.dto.response.ApiResponse;
import com.be.dto.response.seller.ShopProfileResponse;
import com.be.service.seller.SellerShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller/shop")
@RequiredArgsConstructor
public class SellerShopController {

    private final SellerShopService sellerShopService;

    @GetMapping
    public ResponseEntity<ApiResponse<ShopProfileResponse>> getMyShop() {
        return ResponseEntity.ok(ApiResponse.success(
                sellerShopService.getMyShop(),
                "Lấy thông tin cửa hàng thành công"
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ShopProfileResponse>> createShop(
            @Valid @RequestBody ShopCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerShopService.createShop(request),
                "Đăng ký cửa hàng thành công"
        ));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ShopProfileResponse>> updateShop(
            @Valid @RequestBody ShopUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerShopService.updateShop(request),
                "Cập nhật thông tin cửa hàng thành công"
        ));
    }
}
