package com.be.controller.customer;

import com.be.dto.response.ApiResponse;
import com.be.dto.response.customer.CategoryItemResponse;
import com.be.dto.response.customer.ProductCardResponse;
import com.be.dto.response.customer.ProductDetailResponse;
import com.be.dto.request.customer.ReviewCreateRequest;
import com.be.dto.response.customer.ReviewCreateResponse;
import com.be.dto.response.customer.ShopDetailWithProductsResponse;
import com.be.dto.response.customer.ShopProductPageResponse;
import com.be.dto.response.customer.ShopPageResponse;
import com.be.service.customer.CustomerProductService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/customer")
@CrossOrigin(origins = "*")
public class CustomerProductController {
	private final CustomerProductService customerProductService;

	@GetMapping("/categories")
	public ResponseEntity<ApiResponse<List<CategoryItemResponse>>> getCurrentCategories() {
		return ResponseEntity.ok(ApiResponse.success(
				customerProductService.getCurrentCategories(),
				"Lay danh sach the loai hien tai thanh cong"
		));
	}

	@GetMapping("/products/hot-deals")
	public ResponseEntity<ApiResponse<List<ProductCardResponse>>> getHotDeals(
			@RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit
	) {
		return ResponseEntity.ok(ApiResponse.success(
				customerProductService.getHotDeals(limit),
				"Lay danh sach San deal hoi thanh cong"
		));
	}

	@GetMapping("/products/new-arrivals")
	public ResponseEntity<ApiResponse<List<ProductCardResponse>>> getNewArrivals(
			@RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit
	) {
		return ResponseEntity.ok(ApiResponse.success(
				customerProductService.getNewArrivals(limit),
				"Lay danh sach Hang moi len ke thanh cong"
		));
	}

	@GetMapping("/products/featured-shops-weekly")
	public ResponseEntity<ApiResponse<List<ProductCardResponse>>> getFeaturedShopsWeekly(
			@RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit
	) {
		return ResponseEntity.ok(ApiResponse.success(
				customerProductService.getFeaturedShopsProducts(limit),
				"Lay danh sach Shop noi bat trong tuan thanh cong"
		));
	}

	// New endpoint: Get products by category with pagination
	@GetMapping("/categories/{categoryId}/products")
	public ResponseEntity<ApiResponse<ShopProductPageResponse>> getProductsByCategory(
			@PathVariable Long categoryId,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "10") @Min(1) @Max(50) int size
	) {
		return ResponseEntity.ok(ApiResponse.success(
				customerProductService.getProductsByCategory(categoryId, page, size),
				"Lay danh sach san pham theo the loai thanh cong"
		));
	}

	@GetMapping("/products")
	public ResponseEntity<ApiResponse<ShopProductPageResponse>> filterAndSortProducts(
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) List<Long> categoryIds,
			@RequestParam(required = false) String condition,
			@RequestParam(required = false) List<String> brands,
			@RequestParam(required = false) List<String> origins,
			@RequestParam(required = false) java.math.BigDecimal minPrice,
			@RequestParam(required = false) java.math.BigDecimal maxPrice,
			@RequestParam(defaultValue = "newest") String sort,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "10") @Min(1) @Max(50) int size
	) {
		return ResponseEntity.ok(ApiResponse.success(
				customerProductService.filterAndSortProducts(keyword, categoryIds, condition, brands, origins, minPrice, maxPrice, sort, page, size),
				"Lay danh sach san pham theo filter va sort thanh cong"
		));
	}

	@GetMapping("/products/{id}")
	public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetail(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.success(
				customerProductService.getProductDetail(id),
				"Lay chi tiet san pham thanh cong"
		));
	}

	@GetMapping("/shops/{shopId}")
	public ResponseEntity<ApiResponse<ShopDetailWithProductsResponse>> getShopDetailWithProducts(
			@PathVariable Long shopId,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "10") @Min(1) @Max(50) int size
	) {
		return ResponseEntity.ok(ApiResponse.success(
				customerProductService.getShopDetailWithProducts(shopId, page, size),
				"Lay thong tin shop va san pham thanh cong"
		));
	}

	@GetMapping("/shops")
	public ResponseEntity<ApiResponse<ShopPageResponse>> listOrSearchShops(
			@RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "0") @Min(0) int page,
				@RequestParam(defaultValue = "10") @Min(1) @Max(50) int size
	) {
		if (keyword == null || keyword.isBlank()) {
			return ResponseEntity.ok(ApiResponse.success(
					customerProductService.listShops(page, size),
					"Lay danh sach shop thanh cong"
				));
		}

		return ResponseEntity.ok(ApiResponse.success(
				customerProductService.searchShopsByName(keyword, page, size),
				"Tim kiem shop theo ten thanh cong"
		));
	}

	@PostMapping(value = "/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<ReviewCreateResponse>> createReview(
			@Validated @ModelAttribute ReviewCreateRequest request
	) {

		System.out.println("DA VAO API CREATE REVIEW");
		System.out.println("orderId = " + request.orderId());
		System.out.println("productId = " + request.productId());
		System.out.println("rating = " + request.rating());
		System.out.println("comment = " + request.comment());
		System.out.println("images = " + request.images());
		return ResponseEntity.ok(ApiResponse.success(
				customerProductService.createReview(request),
				"Tao danh gia thanh cong"
		));
	}
}
