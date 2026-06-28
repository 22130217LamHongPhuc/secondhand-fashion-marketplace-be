package com.be.service;

import com.be.dto.request.shipping.ShippingFeeItemRequest;
import com.be.dto.request.shipping.ShippingFeeRequest;
import com.be.dto.response.shipping.ShippingFeeResponse;
import com.be.entity.Product;
import com.be.entity.Shop;
import com.be.entity.UserAddress;
import com.be.repository.ProductRepository;
import com.be.repository.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GhnShippingService {
    private static final BigDecimal FALLBACK_SHIPPING_FEE = new BigDecimal("30000");

    private final ProductRepository productRepository;
    private final UserAddressRepository userAddressRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ghn.api.base-url:https://dev-online-gateway.ghn.vn/shiip/public-api}")
    private String ghnBaseUrl;

    @Value("${ghn.api.token:db44e853-cc14-11ef-b1ed-769685acafa5}")
    private String ghnToken;

    @Value("${ghn.api.shop-id:195767}")
    private String ghnShopId;

    public Object getProvinces() {
        return postOrGet("/master-data/province", null, false);
    }

    public Object getDistricts(Integer provinceId) {
        Map<String, Object> body = new HashMap<>();
        body.put("province_id", provinceId);
        return postOrGet("/master-data/district", body, true);
    }

    public Object getWards(Integer districtId) {
        return postOrGet("/master-data/ward?district_id=" + districtId, null, false);
    }

    @Transactional(readOnly = true)
    public ShippingFeeResponse quoteShippingFee(ShippingFeeRequest request) {
        UserAddress address = userAddressRepository.findById(request.getShippingAddressId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ giao hàng"));

        if (request.getCustomerId() != null
                && address.getUser() != null
                && !request.getCustomerId().equals(address.getUser().getId())) {
            throw new IllegalArgumentException("Địa chỉ giao hàng không thuộc tài khoản này");
        }

        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (ShippingFeeItemRequest item : request.getItems()) {
            quantities.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }

        Map<Shop, List<ProductQuantity>> itemsByShop = new LinkedHashMap<>();
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với id: " + entry.getKey()));
            Shop shop = product.getShop();
            itemsByShop.computeIfAbsent(shop, key -> new ArrayList<>())
                    .add(new ProductQuantity(product, entry.getValue()));
        }

        BigDecimal totalFee = BigDecimal.ZERO;
        Map<Long, BigDecimal> shopFees = new LinkedHashMap<>();
        boolean fallbackUsed = false;

        for (Map.Entry<Shop, List<ProductQuantity>> entry : itemsByShop.entrySet()) {
            Shop shop = entry.getKey();
            int totalWeight = 0;
            int maxLength = 20;
            int maxWidth = 15;
            int totalHeight = 0;
            BigDecimal insuranceValue = BigDecimal.ZERO;

            for (ProductQuantity item : entry.getValue()) {
                Product product = item.product();
                int qty = item.quantity();
                totalWeight += defaultInt(product.getWeight(), 500) * qty;
                maxLength = Math.max(maxLength, defaultInt(product.getLength(), 20));
                maxWidth = Math.max(maxWidth, defaultInt(product.getWidth(), 15));
                totalHeight += defaultInt(product.getHeight(), 5) * qty;
                BigDecimal price = product.getSalePrice() != null ? product.getSalePrice() : product.getBasePrice();
                insuranceValue = insuranceValue.add(price.multiply(BigDecimal.valueOf(qty)));
            }

            FeeResult feeResult = calculateGhnFee(
                    shop.getDistrictId(),
                    shop.getWardCode(),
                    address.getDistrictId(),
                    address.getWardCode(),
                    totalWeight,
                    maxLength,
                    maxWidth,
                    totalHeight,
                    insuranceValue
            );

            fallbackUsed = fallbackUsed || feeResult.fallbackUsed();
            totalFee = totalFee.add(feeResult.fee());
            shopFees.put(shop.getId(), feeResult.fee());
        }

        return ShippingFeeResponse.builder()
                .totalFee(totalFee)
                .shopFees(shopFees)
                .fallbackUsed(fallbackUsed)
                .message(fallbackUsed ? "Một số phí vận chuyển đang dùng phí mặc định do thiếu dữ liệu GHN hoặc GHN lỗi." : "Tính phí vận chuyển thành công")
                .build();
    }

    public FeeResult calculateGhnFee(Integer fromDistrictId, String fromWardCode,
                                     Integer toDistrictId, String toWardCode,
                                     int weight, int length, int width, int height,
                                     BigDecimal insuranceValue) {
        if (fromDistrictId == null || !StringUtils.hasText(fromWardCode)
                || toDistrictId == null || !StringUtils.hasText(toWardCode)) {
            return new FeeResult(FALLBACK_SHIPPING_FEE, true);
        }

        try {
            String url = normalizeBaseUrl() + "/v2/shipping-order/fee";
            HttpHeaders headers = ghnHeaders();
            headers.set("ShopId", ghnShopId);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("from_district_id", fromDistrictId);
            requestBody.put("from_ward_code", fromWardCode);
            requestBody.put("service_id", null);
            requestBody.put("service_type_id", 2);
            requestBody.put("to_district_id", toDistrictId);
            requestBody.put("to_ward_code", toWardCode);
            requestBody.put("height", Math.max(height, 1));
            requestBody.put("length", Math.max(length, 1));
            requestBody.put("weight", Math.max(weight, 1));
            requestBody.put("width", Math.max(width, 1));
            requestBody.put("insurance_value", insuranceValue == null ? 0 : insuranceValue.intValue());
            requestBody.put("cod_failed_amount", 0);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, new HttpEntity<>(requestBody, headers), Map.class);
            Map body = response.getBody();
            if (response.getStatusCode().is2xxSuccessful() && body != null && Integer.valueOf(200).equals(body.get("code"))) {
                Map data = (Map) body.get("data");
                if (data != null && data.get("total") instanceof Number total) {
                    return new FeeResult(new BigDecimal(total.toString()), false);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi tính phí ship GHN API: " + e.getMessage());
        }

        return new FeeResult(FALLBACK_SHIPPING_FEE, true);
    }

    public UserAddress enrichAddressCodes(UserAddress address) {
        if (address == null || address.getDistrictId() != null && StringUtils.hasText(address.getWardCode())) {
            return address;
        }

        try {
            List<Map<String, Object>> provinces = asListMap(getProvinces());
            Optional<Map<String, Object>> province = findByNormalizedName(provinces, address.getProvince(), "ProvinceName");
            if (province.isEmpty()) {
                return address;
            }
            Integer provinceId = toInteger(province.get().get("ProvinceID"));
            address.setProvinceId(provinceId);

            List<Map<String, Object>> districts = asListMap(getDistricts(provinceId));
            Optional<Map<String, Object>> district = findByNormalizedName(districts, address.getDistrict(), "DistrictName");
            if (district.isEmpty()) {
                return address;
            }
            Integer districtId = toInteger(district.get().get("DistrictID"));
            address.setDistrictId(districtId);

            List<Map<String, Object>> wards = asListMap(getWards(districtId));
            Optional<Map<String, Object>> ward = findByNormalizedName(wards, address.getWard(), "WardName");
            ward.ifPresent(value -> address.setWardCode(String.valueOf(value.get("WardCode"))));
        } catch (Exception e) {
            System.err.println("Không thể tự động resolve mã địa chỉ GHN: " + e.getMessage());
        }

        return address;
    }

    private Object postOrGet(String path, Map<String, Object> body, boolean post) {
        String url = normalizeBaseUrl() + path;
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, ghnHeaders());
        ResponseEntity<Map> response = post
                ? restTemplate.postForEntity(url, entity, Map.class)
                : restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, Map.class);
        Map responseBody = response.getBody();
        if (!response.getStatusCode().is2xxSuccessful() || responseBody == null) {
            throw new IllegalStateException("GHN API không phản hồi hợp lệ");
        }
        if (!Integer.valueOf(200).equals(responseBody.get("code"))) {
            throw new IllegalStateException(String.valueOf(responseBody.getOrDefault("message", "GHN API error")));
        }
        return responseBody.get("data");
    }

    private HttpHeaders ghnHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", ghnToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String normalizeBaseUrl() {
        String base = ghnBaseUrl == null ? "" : ghnBaseUrl.trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> asListMap(Object value) {
        if (value instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return List.of();
    }

    private Optional<Map<String, Object>> findByNormalizedName(List<Map<String, Object>> items, String name, String field) {
        String target = normalizeAddressName(name);
        return items.stream()
                .filter(item -> normalizeAddressName(String.valueOf(item.get(field))).equals(target))
                .findFirst();
    }

    private String normalizeAddressName(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\b(tinh|thanh pho|tp|quan|huyen|thi xa|tx|phuong|xa|thi tran|tt)\\b", "")
                .replaceAll("[^a-z0-9]", "");
        return normalized;
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return null;
        }
        return Integer.valueOf(String.valueOf(value));
    }

    private int defaultInt(Integer value, int fallback) {
        return value == null || value <= 0 ? fallback : value;
    }

    private record ProductQuantity(Product product, int quantity) {}

    public record FeeResult(BigDecimal fee, boolean fallbackUsed) {}
}
