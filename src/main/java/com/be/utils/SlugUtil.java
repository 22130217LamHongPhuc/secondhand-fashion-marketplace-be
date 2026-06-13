package com.be.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SlugUtil {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final Pattern MULTI_HYPHEN = Pattern.compile("-+");

    private SlugUtil() {
    }

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        // Replace Vietnamese 'đ' and 'Đ' which are not handled by Normalizer
        String normalized = input.replace('đ', 'd').replace('Đ', 'd');

        // Normalize text to decompose characters (e.g., separating base letter and diacritics)
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        
        // Remove diacritical marks
        normalized = normalized.replaceAll("\\p{M}", "");

        // Convert to lowercase
        normalized = normalized.toLowerCase(Locale.ENGLISH);

        // Replace whitespace and underscores with hyphen
        normalized = WHITESPACE.matcher(normalized).replaceAll("-");

        // Remove non-latin and non-alphanumeric characters except hyphens and underscores
        normalized = NONLATIN.matcher(normalized).replaceAll("");

        // Replace multiple consecutive hyphens with one
        normalized = MULTI_HYPHEN.matcher(normalized).replaceAll("-");

        // Trim hyphens from both ends
        if (normalized.startsWith("-")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("-")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }
}
