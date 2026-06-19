package com.be.dto.response.seller;

public record ShopProfileResponse(
        Long id,
        String name,
        String slug,
        String description,
        String avatarUrl,
        String bannerUrl
) {}
