package com.be.dto.request.seller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShopCreateRequest(
        @NotBlank(message = "Tên cửa hàng không được để trống")
        @Size(max = 255, message = "Tên cửa hàng không được vượt quá 255 ký tự")
        String name,

        @NotBlank(message = "Mô tả cửa hàng không được để trống")
        @Size(max = 5000, message = "Mô tả cửa hàng không được vượt quá 5000 ký tự")
        String description,

        @NotBlank(message = "Ảnh đại diện cửa hàng không được để trống")
        String avatarUrl,

        @NotBlank(message = "Ảnh bìa cửa hàng không được để trống")
        String bannerUrl
) {}
