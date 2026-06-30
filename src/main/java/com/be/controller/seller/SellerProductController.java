package com.be.controller.seller;

import com.be.dto.request.seller.ProductCreateRequest;
import com.be.dto.request.seller.ProductUpdateRequest;
import com.be.dto.response.ApiResponse;
import com.be.dto.response.seller.ProductListResponse;
import com.be.dto.response.seller.ProductDetailResponse;
import com.be.dto.response.seller.ProductMutationResponse;
import com.be.service.seller.SellerProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/seller/products")
@RequiredArgsConstructor
public class SellerProductController {
    private final SellerProductService sellerProductService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductListResponse>>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isApproved,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerProductService.searchProducts(keyword, isActive, isApproved, fromDate, toDate, minPrice, maxPrice, sortBy, page),
                "Lấy danh sách sản phẩm thành công"
        ));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getDetails(@PathVariable long id) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerProductService.getDetails(id),
                "Lấy chi tiết sản phẩm thành công"
        ));
    }



    @PostMapping
    public ResponseEntity<ApiResponse<ProductMutationResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerProductService.createProduct(request),
                "Tạo sản phẩm thành công"
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductMutationResponse>> updateProduct(
            @PathVariable long id,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerProductService.updateProduct(id, request),
                "Cập nhật sản phẩm thành công"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable long id) {
        sellerProductService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa sản phẩm thành công"));
    }
}
