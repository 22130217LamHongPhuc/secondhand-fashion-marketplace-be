package com.be.dto.request.seller;

import com.be.common.enums.ProductCondition;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ProductCreateRequest(
        Long categoryId,

        @NotBlank(message = "Tên sản phẩm không được để trống")
        @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
        String name,

        @Size(max = 5000, message = "Mô tả sản phẩm không được vượt quá 5000 ký tự")
        String description,

        @Size(max = 100, message = "Thương hiệu không được vượt quá 100 ký tự")
        String brand,

        @Size(max = 100, message = "Xuất xứ không được vượt quá 100 ký tự")
        String originCountry,

        ProductCondition condition,

        @NotNull(message = "Giá gốc không được để trống")
        @DecimalMin(value = "0.0", inclusive = false, message = "Giá gốc phải lớn hơn 0")
        BigDecimal basePrice,

        @DecimalMin(value = "0.0", inclusive = false, message = "Giá bán phải lớn hơn 0")
        BigDecimal salePrice,

        @NotNull(message = "Số lượng tồn kho không được để trống")
        @Min(value = 0, message = "Số lượng tồn kho phải lớn hơn hoặc bằng 0")
        Integer stockQuantity,

        @Size(max = 20, message = "Sản phẩm chỉ có thể có tối đa 20 hình ảnh")
        List<@Valid ProductImageRequest> images,

        @Size(max = 30, message = "Sản phẩm chỉ có thể có tối đa 30 thuộc tính")
        List<@Valid ProductAttributeRequest> attributes,

        @Size(max = 20, message = "Sản phẩm chỉ có thể có tối đa 20 thẻ")
        List<@NotBlank(message = "Thẻ không được để trống") @Size(max = 100, message = "Thẻ không được vượt quá 100 ký tự") String> tags
) {
    @AssertTrue(message = "Giá bán phải nhỏ hơn hoặc bằng giá gốc")
    public boolean isSalePriceValid() {
        return salePrice == null || basePrice == null || salePrice.compareTo(basePrice) <= 0;
    }
}
