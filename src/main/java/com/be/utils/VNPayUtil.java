package com.be.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VNPayUtil {
    public static String vnp_PayUrl;
    public static String vnp_ReturnUrl;
    public static String vnp_TmnCode;
    public static String secretKey;

    @Value("${vnpay.pay-url}")
    public void setPayUrl(String payUrl) {
        vnp_PayUrl = payUrl;
    }

    @Value("${vnpay.return-url}")
    public void setReturnUrl(String returnUrl) {
        vnp_ReturnUrl = returnUrl;
    }

    @Value("${vnpay.tmn-code}")
    public void setTmnCode(String tmnCode) {
        vnp_TmnCode = tmnCode;
    }

    @Value("${vnpay.secret-key}")
    public void setSecretKey(String key) {
        secretKey = key;
    }

    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKeySpec = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKeySpec);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    public static String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                sb.append(fieldName);
                sb.append("=");
                try {
                    String encodedValue = java.net.URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString())
                            .replace("+", "%20");
                    sb.append(encodedValue);
                } catch (Exception e) {
                    sb.append(fieldValue);
                }
                if (itr.hasNext()) {
                    sb.append("&");
                }
            }
        }
        return hmacSHA512(secretKey, sb.toString());
    }
}
