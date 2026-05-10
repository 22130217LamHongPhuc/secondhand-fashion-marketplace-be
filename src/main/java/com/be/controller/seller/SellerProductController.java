package com.be.controller.seller;

import com.be.dto.request.seller.ProductCreateRequest;
import com.be.dto.request.seller.ProductUpdateRequest;
import com.be.dto.response.ApiResponse;
import com.be.entity.Product;
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

@RestController
@RequestMapping("/api/seller/products")
@RequiredArgsConstructor
public class SellerProductController {
    private final SellerProductService sellerProductService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Product>>> getListByPage(
            @RequestParam(required = false, defaultValue = "0") long lastId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerProductService.getListByPage(lastId, page),
                "Get product list successfully"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getDetails(@PathVariable long id) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerProductService.getDetails(id),
                "Get product details successfully"
        ));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Page<Product>>> getListByStatus(
            @RequestParam Boolean isActive,
            @RequestParam(required = false, defaultValue = "0") long lastId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerProductService.getListByStatus(isActive, lastId, page),
                "Get product list by status successfully"
        ));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Product>> createProduct(
            @Valid @ModelAttribute ProductCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerProductService.createProduct(request),
                "Create product successfully"
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable long id,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerProductService.updateProduct(id, request),
                "Update product successfully"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable long id) {
        sellerProductService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Delete product successfully"));
    }
}
