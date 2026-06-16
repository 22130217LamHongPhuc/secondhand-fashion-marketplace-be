package com.be.dto.response.seller.mapper;

import com.be.dto.response.seller.ShopProfileResponse;
import com.be.entity.Shop;

public final class SellerShopMapper {

    private SellerShopMapper() {
    }

    public static ShopProfileResponse toProfileResponse(Shop shop) {
        if (shop == null) {
            return null;
        }
        return new ShopProfileResponse(
                shop.getId(),
                shop.getName(),
                shop.getSlug(),
                shop.getDescription(),
                shop.getAvatarUrl(),
                shop.getBannerUrl()
        );
    }
}
