package com.be.controller;

import com.be.dto.request.shipping.ShippingFeeRequest;
import com.be.dto.response.ApiResponse;
import com.be.dto.response.shipping.ShippingFeeResponse;
import com.be.service.GhnShippingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shipping")
@CrossOrigin(origins = "*")
public class ShippingController {

    private final GhnShippingService ghnShippingService;

    @GetMapping("/provinces")
    public ResponseEntity<ApiResponse<Object>> getProvinces() {
        return ResponseEntity.ok(ApiResponse.success(ghnShippingService.getProvinces(), "Lấy danh sách tỉnh/thành thành công"));
    }

    @GetMapping("/districts")
    public ResponseEntity<ApiResponse<Object>> getDistricts(@RequestParam Integer provinceId) {
        return ResponseEntity.ok(ApiResponse.success(ghnShippingService.getDistricts(provinceId), "Lấy danh sách quận/huyện thành công"));
    }

    @GetMapping("/wards")
    public ResponseEntity<ApiResponse<Object>> getWards(@RequestParam Integer districtId) {
        return ResponseEntity.ok(ApiResponse.success(ghnShippingService.getWards(districtId), "Lấy danh sách phường/xã thành công"));
    }

    @PostMapping("/fee")
    public ResponseEntity<ApiResponse<ShippingFeeResponse>> quoteShippingFee(
            @Valid @RequestBody ShippingFeeRequest request
    ) {
        ShippingFeeResponse result = ghnShippingService.quoteShippingFee(request);
        return ResponseEntity.ok(ApiResponse.success(result, result.getMessage()));
    }
}
