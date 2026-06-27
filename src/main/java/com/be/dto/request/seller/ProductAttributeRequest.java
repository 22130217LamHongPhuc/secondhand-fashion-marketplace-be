package com.be.dto.request.seller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductAttributeRequest(
        @NotBlank(message = "Tên thuộc tính không được để trống")
        @Size(max = 100, message = "Tên thuộc tính không được vượt quá 100 ký tự")
        String attrKey,

        @NotBlank(message = "Giá trị thuộc tính không được để trống")
        @Size(max = 255, message = "Giá trị thuộc tính không được vượt quá 255 ký tự")
        String attrValue
) {
}
