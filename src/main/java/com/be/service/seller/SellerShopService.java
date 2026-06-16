package com.be.service.seller;

import com.be.dto.request.seller.ShopCreateRequest;
import com.be.dto.request.seller.ShopUpdateRequest;
import com.be.dto.response.seller.ShopProfileResponse;

public interface SellerShopService {
    ShopProfileResponse getMyShop();
    ShopProfileResponse createShop(ShopCreateRequest request);
    ShopProfileResponse updateShop(ShopUpdateRequest request);
}
