package com.be.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

@Component // Bắt buộc phải có để Spring quét và tiêm @Value
public class UrlGenerator {

    private static String baseUrl;

    private UrlGenerator() {
    }

    @Value("${cloudflare.r2.domain}")
    public void setBaseUrl(String endpoint) {
        UrlGenerator.baseUrl = endpoint;
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ URL CƠ BẢN
    // ==========================================

    public static String generateUrl(String key) {
        if (!StringUtils.hasText(baseUrl) || !StringUtils.hasText(key)) {
            throw new IllegalArgumentException("baseUrl or key is null");
        }

        String cleanBaseUrl = baseUrl.trim();
        String cleanKey = key.trim();

        if (!cleanBaseUrl.endsWith("/")) {
            cleanBaseUrl += "/";
        }

        if (cleanKey.startsWith("/")) {
            cleanKey = cleanKey.substring(1);
        }
        System.out.println(cleanBaseUrl + cleanKey);
        return cleanBaseUrl + cleanKey;
    }

    // ==========================================
    // CÁC HÀM CONVERT URL TRỰC TIẾP (Tích hợp KeyGeneratorUtil)
    // ==========================================

    public static String convertTempUrlToProductUrl(String tempUrl) {
        String tempKey = KeyGeneratorUtil.extractKey(tempUrl);
        String productKey = KeyGeneratorUtil.convertToProductKey(tempKey);
        return generateUrl(productKey);
    }

    public static String convertTempUrlToReviewUrl(String tempUrl) {
        String tempKey = KeyGeneratorUtil.extractKey(tempUrl);
        String reviewKey = KeyGeneratorUtil.convertToReviewKey(tempKey);
        return generateUrl(reviewKey);
    }

    public static String convertTempUrlToAvatarUrl(String tempUrl) {
        String tempKey = KeyGeneratorUtil.extractKey(tempUrl);
        String avatarKey = KeyGeneratorUtil.convertToAvatarKey(tempKey);
        return generateUrl(avatarKey);
    }

    public static String convertTempUrlToBannerUrl(String tempUrl) {
        String tempKey = KeyGeneratorUtil.extractKey(tempUrl);
        String bannerKey = KeyGeneratorUtil.convertToBannerKey(tempKey);
        return generateUrl(bannerKey);
    }
}