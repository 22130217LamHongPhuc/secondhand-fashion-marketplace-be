package com.be.dto.request.seller;

import jakarta.validation.constraints.Size;

public record ShopUpdateRequest(
        @Size(max = 255, message = "Tên cửa hàng không được vượt quá 255 ký tự")
        String name,

        @Size(max = 5000, message = "Mô tả cửa hàng không được vượt quá 5000 ký tự")
        String description,

        String avatarUrl,

        String bannerUrl
) {}
