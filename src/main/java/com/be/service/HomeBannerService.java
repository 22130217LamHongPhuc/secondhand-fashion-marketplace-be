package com.be.service;

import com.be.dto.request.HomeBannerRequest;
import com.be.entity.HomeBanner;

import java.util.List;

public interface HomeBannerService {
    HomeBanner createBanner(HomeBannerRequest request);
    HomeBanner updateBanner(Long id, HomeBannerRequest request);
    void deleteBanner(Long id);
    HomeBanner toggleBannerActive(Long id, boolean active);
    List<HomeBanner> getAllBanners();
    List<HomeBanner> getActiveBanners();
    HomeBanner getBannerById(Long id);
}
