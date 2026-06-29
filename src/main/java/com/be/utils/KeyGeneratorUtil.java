package com.be.utils;

import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

public final class KeyGeneratorUtil {

    // Định nghĩa các hằng số prefix thư mục
    public static final String FOLDER_TEMP = "temp/";
    public static final String FOLDER_PRODUCT = "products/";
    public static final String FOLDER_REVIEW = "reviews/";
    public static final String FOLDER_BANNER = "banners/";
    public static final String FOLDER_AVATAR = "avatars/";

    private KeyGeneratorUtil() {
    }

    /**
     * Hàm sinh chuỗi UUID theo Time-Ordered Epoch gốc của bạn
     */
    public static String generateKey() {
        return UuidCreator.getTimeOrderedEpoch().toString();
    }
    public static String extractKey(String fullUrl) {
        if (!StringUtils.hasText(fullUrl)) {
            throw new IllegalArgumentException("fullUrl không được để trống");
        }

        try {
            URI uri = new URI(fullUrl.trim());
            String path = uri.getPath();

            if (!StringUtils.hasText(path)) {
                throw new IllegalArgumentException("URL không chứa đường dẫn file hợp lệ");
            }

            if (path.startsWith("/")) {
                return path.substring(1);
            }

            return path;

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Định dạng URL không hợp lệ: " + fullUrl, e);
        }
    }

    public static String generateTempKey(String originalFilename) {
        String safeName = StringUtils.hasText(originalFilename) ? originalFilename.replaceAll("\\s+", "-") : "file";
        return FOLDER_TEMP + generateKey() + "-" + safeName;
    }

    private static String convertFromTemp(String tempKey, String targetFolder) {
        if (!StringUtils.hasText(tempKey) || !tempKey.startsWith(FOLDER_TEMP)) {
            throw new IllegalArgumentException("Key không hợp lệ hoặc không nằm trong thư mục temp: " + tempKey);
        }
        return tempKey.replaceFirst(FOLDER_TEMP, targetFolder);
    }

    public static String convertToProductKey(String tempKey) {
        return convertFromTemp(tempKey, FOLDER_PRODUCT);
    }

    public static String convertToReviewKey(String tempKey) {
        return convertFromTemp(tempKey, FOLDER_REVIEW);
    }

    public static String convertToBannerKey(String tempKey) {
        return convertFromTemp(tempKey, FOLDER_BANNER);
    }

    public static String convertToAvatarKey(String tempKey) {
        return convertFromTemp(tempKey, FOLDER_AVATAR);
    }
}