package com.be.dto.request;

import com.be.common.enums.ProductCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String description;

    @NotNull(message = "ID cửa hàng là bắt buộc")
    private Long shopId;

    private Long categoryId;

    private String brand;

    private String originCountry;

    @NotNull(message = "Tình trạng sản phẩm là bắt buộc")
    private ProductCondition condition;

    @NotNull(message = "Giá gốc là bắt buộc")
    @Positive(message = "Giá gốc phải lớn hơn 0")
    private BigDecimal basePrice;

    private BigDecimal salePrice;

    @NotNull(message = "Số lượng kho là bắt buộc")
    private Integer stockQuantity;

    private List<String> imageUrls;
}
