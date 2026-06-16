package com.be.service.seller.impl;

import com.be.dto.request.seller.ShopCreateRequest;
import com.be.dto.request.seller.ShopUpdateRequest;
import com.be.dto.response.seller.ShopProfileResponse;
import com.be.dto.response.seller.mapper.SellerShopMapper;
import com.be.entity.Shop;
import com.be.entity.User;
import com.be.repository.ShopRepository;
import com.be.repository.UserRepository;
import com.be.service.ImageStoreService;
import com.be.service.seller.SellerShopService;
import com.be.utils.KeyGeneratorUtil;
import com.be.utils.SlugUtil;
import com.be.utils.UrlGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerShopServiceImpl implements SellerShopService {

    private final ShopRepository shopRepository;
    private final ImageStoreService imageStoreService;
    private final UserRepository userRepository;
    @Override
    @Transactional(readOnly = true)
    public ShopProfileResponse getMyShop() {
        Shop shop = getCurrentSellerShop();
        return SellerShopMapper.toProfileResponse(shop);
    }

    @Override
    @Transactional
    public ShopProfileResponse createShop(ShopCreateRequest request) {
        User user = getCurrentUser();
        // 1. Kiểm tra user chưa có shop
        if (shopRepository.existsBySellerId(user.getId())) {
            throw new IllegalStateException("Cửa hàng đã tồn tại cho tài khoản này.");
        }

        // 2. Generate slug từ name
        String slug = generateUniqueSlug(request.name());

        // 3. Process avatarUrl (temp -> avatar)
        String avatarUrl = processAvatarUrl(request.avatarUrl());

        // 4. Process bannerUrl (temp -> banner)
        String bannerUrl = processBannerUrl(request.bannerUrl());

        // 5. Build entity và save
        Shop shop = Shop.builder()
                .seller(user)
                .name(request.name().trim())
                .slug(slug)
                .description(request.description() != null ? request.description().trim() : null)
                .isActive(true)
                .avatarUrl(avatarUrl)
                .bannerUrl(bannerUrl)
                .build();

        Shop savedShop = shopRepository.save(shop);
        log.info("Đăng ký thành công shop: {} với slug: {} cho user: {}", savedShop.getName(), savedShop.getSlug(), user.getId());
        return SellerShopMapper.toProfileResponse(savedShop);
    }

    @Override
    @Transactional
    public ShopProfileResponse updateShop(ShopUpdateRequest request) {
        Shop shop = getCurrentSellerShop();

        // 1. Nếu thay đổi name -> re-generate slug
        if (StringUtils.hasText(request.name()) && !Objects.equals(request.name().trim(), shop.getName())) {
            String newName = request.name().trim();
            shop.setName(newName);
            shop.setSlug(generateUniqueSlug(newName));
        }

        // 2. Nếu thay đổi description
        if (request.description() != null) {
            shop.setDescription(request.description().trim());
        }

        // 3. Nếu thay đổi avatarUrl -> temp to avatar
        if (StringUtils.hasText(request.avatarUrl()) && !Objects.equals(request.avatarUrl(), shop.getAvatarUrl())) {
            shop.setAvatarUrl(processAvatarUrl(request.avatarUrl()));
        }

        // 4. Nếu thay đổi bannerUrl -> temp to banner
        if (StringUtils.hasText(request.bannerUrl()) && !Objects.equals(request.bannerUrl(), shop.getBannerUrl())) {
            shop.setBannerUrl(processBannerUrl(request.bannerUrl()));
        }

        Shop updatedShop = shopRepository.save(shop);
        log.info("Cập nhật thành công shop: {} cho seller: {}", updatedShop.getName(), shop.getSeller().getId());
        return SellerShopMapper.toProfileResponse(updatedShop);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new IllegalStateException("Yêu cầu xác thực tài khoản bán hàng.");
        }
        return user;
    }

    private Shop getCurrentSellerShop() {
        User user = getCurrentUser();
        return shopRepository.findBySellerId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thông tin cửa hàng cho tài khoản này."));
    }

    private String generateUniqueSlug(String name) {
        String baseSlug = SlugUtil.toSlug(name);
        String slug = baseSlug;
        int counter = 1;
        while (shopRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + UUID.randomUUID().toString().substring(0, 8);
            counter++;
            if (counter > 10) {
                // Break infinite loops just in case
                slug = baseSlug + "-" + System.currentTimeMillis();
                break;
            }
        }
        return slug;
    }

    private String processAvatarUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return url;
        }
        try {
            String key = KeyGeneratorUtil.extractKey(url);
            if (key.startsWith(KeyGeneratorUtil.FOLDER_TEMP)) {
                String targetUrl = UrlGenerator.convertTempUrlToAvatarUrl(url);
                String targetKey = KeyGeneratorUtil.extractKey(targetUrl);
                imageStoreService.copyImage(key, targetKey);
                return targetUrl;
            }
        } catch (Exception e) {
            log.error("Lỗi khi copy avatar từ thư mục temp: {}", e.getMessage(), e);
        }
        return url;
    }

    private String processBannerUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return url;
        }
        try {
            String key = KeyGeneratorUtil.extractKey(url);
            if (key.startsWith(KeyGeneratorUtil.FOLDER_TEMP)) {
                String targetUrl = UrlGenerator.convertTempUrlToBannerUrl(url);
                String targetKey = KeyGeneratorUtil.extractKey(targetUrl);
                imageStoreService.copyImage(key, targetKey);
                return targetUrl;
            }
        } catch (Exception e) {
            log.error("Lỗi khi copy banner từ thư mục temp: {}", e.getMessage(), e);
        }
        return url;
    }
}
