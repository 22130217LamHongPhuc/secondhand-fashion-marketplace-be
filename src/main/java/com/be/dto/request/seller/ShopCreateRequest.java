package com.be.dto.request.seller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
        String bannerUrl,

        @NotNull(message = "Mã tỉnh/thành GHN không được để trống")
        Integer provinceId,

        @NotBlank(message = "Tên tỉnh/thành không được để trống")
        @Size(max = 100, message = "Tên tỉnh/thành không được vượt quá 100 ký tự")
        String provinceName,

        @NotNull(message = "Mã quận/huyện GHN không được để trống")
        Integer districtId,

        @NotBlank(message = "Tên quận/huyện không được để trống")
        @Size(max = 100, message = "Tên quận/huyện không được vượt quá 100 ký tự")
        String districtName,

        @NotBlank(message = "Mã phường/xã GHN không được để trống")
        @Size(max = 20, message = "Mã phường/xã không được vượt quá 20 ký tự")
        String wardCode,

        @NotBlank(message = "Tên phường/xã không được để trống")
        @Size(max = 100, message = "Tên phường/xã không được vượt quá 100 ký tự")
        String wardName,

        @NotBlank(message = "Địa chỉ chi tiết không được để trống")
        @Size(max = 500, message = "Địa chỉ chi tiết không được vượt quá 500 ký tự")
        String addressDetail
) {}
