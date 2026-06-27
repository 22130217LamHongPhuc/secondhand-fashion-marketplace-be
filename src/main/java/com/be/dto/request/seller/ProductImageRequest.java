package com.be.dto.request.seller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record ProductImageRequest(
        @NotNull(message = "Đường dẫn hình ảnh không được để trống")
        String imageUrl,

        @Min(value = 0, message = "Thứ tự sắp xếp hình ảnh phải lớn hơn hoặc bằng 0")
        Integer sortOrder,

        Boolean isPrimary
) {
}
