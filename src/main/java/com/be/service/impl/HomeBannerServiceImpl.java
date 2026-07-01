package com.be.service.impl;

import com.be.dto.request.HomeBannerRequest;
import com.be.entity.HomeBanner;
import com.be.repository.HomeBannerRepository;
import com.be.service.HomeBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeBannerServiceImpl implements HomeBannerService {

    private final HomeBannerRepository homeBannerRepository;

    @Override
    @Transactional
    public HomeBanner createBanner(HomeBannerRequest request) {
        HomeBanner banner = HomeBanner.builder()
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
                .imageUrl(request.getImageUrl())
                .linkUrl(request.getLinkUrl())
                .orderNum(request.getOrderNum() != null ? request.getOrderNum() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        return homeBannerRepository.save(banner);
    }

    @Override
    @Transactional
    public HomeBanner updateBanner(Long id, HomeBannerRequest request) {
        HomeBanner existing = homeBannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found with id: " + id));

        existing.setTitle(request.getTitle());
        existing.setSubtitle(request.getSubtitle());
        existing.setImageUrl(request.getImageUrl());
        existing.setLinkUrl(request.getLinkUrl());
        if (request.getOrderNum() != null) {
            existing.setOrderNum(request.getOrderNum());
        }
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }

        return homeBannerRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteBanner(Long id) {
        if (!homeBannerRepository.existsById(id)) {
            throw new RuntimeException("Banner not found with id: " + id);
        }
        homeBannerRepository.deleteById(id);
    }

    @Override
    @Transactional
    public HomeBanner toggleBannerActive(Long id, boolean active) {
        HomeBanner banner = homeBannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found with id: " + id));
        banner.setIsActive(active);
        return homeBannerRepository.save(banner);
    }

    @Override
    public List<HomeBanner> getAllBanners() {
        return homeBannerRepository.findAllByOrderByOrderNumAsc();
    }

    @Override
    public List<HomeBanner> getActiveBanners() {
        return homeBannerRepository.findByIsActiveTrueOrderByOrderNumAsc();
    }

    @Override
    public HomeBanner getBannerById(Long id) {
        return homeBannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found with id: " + id));
    }
}
