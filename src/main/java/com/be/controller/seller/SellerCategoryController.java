package com.be.controller.seller;

import com.be.dto.response.ApiResponse;
import com.be.dto.response.customer.CategoryItemResponse;
import com.be.entity.Category;
import com.be.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/seller/categories")
@RequiredArgsConstructor
public class SellerCategoryController {
    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryItemResponse>>> getCategories() {
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderBySortOrderAscNameAsc();
        List<CategoryItemResponse> response = categories.stream()
            .map(c -> new CategoryItemResponse(
                c.getId(),
                c.getName(),
                c.getSlug(),
                c.getIconUrl(),
                c.getSortOrder()
            ))
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách danh mục thành công"));
    }
}
