package com.be.dto.response.customer;

public record ShopDetailWithProductsResponse(
        ShopInfoResponse shop,
        ShopProductPageResponse products
) {
}

