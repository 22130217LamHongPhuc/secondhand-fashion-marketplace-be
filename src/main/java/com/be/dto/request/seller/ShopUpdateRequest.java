package com.be.dto.request.seller;

import jakarta.validation.constraints.Size;

public record ShopUpdateRequest(
        @Size(max = 255, message = "Tên cửa hàng không được vượt quá 255 ký tự")
        String name,

        @Size(max = 5000, message = "Mô tả cửa hàng không được vượt quá 5000 ký tự")
        String description,

        String avatarUrl,

        String bannerUrl,

        Integer provinceId,

        @Size(max = 100, message = "Tên tỉnh/thành không được vượt quá 100 ký tự")
        String provinceName,

        Integer districtId,

        @Size(max = 100, message = "Tên quận/huyện không được vượt quá 100 ký tự")
        String districtName,

        @Size(max = 20, message = "Mã phường/xã không được vượt quá 20 ký tự")
        String wardCode,

        @Size(max = 100, message = "Tên phường/xã không được vượt quá 100 ký tự")
        String wardName,

        @Size(max = 500, message = "Địa chỉ chi tiết không được vượt quá 500 ký tự")
        String addressDetail
) {}
