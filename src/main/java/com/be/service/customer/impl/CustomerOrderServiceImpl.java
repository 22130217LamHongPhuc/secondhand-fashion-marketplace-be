package com.be.service.customer.impl;

import com.be.common.enums.OrderStatus;
import com.be.common.enums.PaymentMethod;
import com.be.common.enums.PaymentStatus;
import com.be.common.enums.TransactionType;
import com.be.common.enums.UserRole;
import com.be.dto.request.customer.CheckoutRequest;
import com.be.dto.request.customer.CheckoutItem;
import com.be.dto.response.customer.OrderDetailResponse;
import com.be.dto.response.customer.OrderHistoryItemResponse;
import com.be.dto.response.customer.OrderHistoryPageResponse;
import com.be.entity.Order;
import com.be.entity.OrderItem;
import com.be.entity.OrderStatusLog;
import com.be.entity.Coupon;
import com.be.entity.Product;
import com.be.entity.Shop;
import com.be.entity.User;
import com.be.entity.UserAddress;
import com.be.entity.Wallet;
import com.be.entity.WalletTransaction;
import com.be.entity.Role;
import com.be.entity.UserRoleMapping;

import com.be.repository.OrderRepository;
import com.be.repository.OrderStatusLogRepository;
import com.be.repository.CouponRepository;
import com.be.repository.UserRepository;
import com.be.repository.ProductRepository;
import com.be.repository.UserAddressRepository;
import com.be.repository.WalletRepository;
import com.be.repository.WalletTransactionRepository;
import com.be.repository.RoleRepository;
import com.be.repository.UserRoleMappingRepository;
import com.be.service.customer.CustomerOrderService;
import com.be.service.SseEmitterService;
import com.be.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;

@Service
@RequiredArgsConstructor
public class CustomerOrderServiceImpl implements CustomerOrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final UserAddressRepository userAddressRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final RoleRepository roleRepository;
    private final UserRoleMappingRepository userRoleMappingRepository;
    private final SseEmitterService sseEmitterService;

    private final PromotionService promotionService;

    @Value("${ghn.api.base-url:https://dev-online-gateway.ghn.vn/shiip/public-api}")
    private String ghnBaseUrl;

    @Value("${ghn.api.token:db44e853-cc14-11ef-b1ed-769685acafa5}")
    private String ghnToken;

    @Value("${ghn.api.shop-id:2509459}")
    private String ghnShopId;

    private final OrderStatusLogRepository orderStatusLogRepository;


    @Override
    @Transactional(readOnly = true)
    public OrderHistoryPageResponse getOrderHistory(Long customerId, OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> orderPage;
        if (status != null) {
            orderPage = orderRepository.findByCustomerIdAndStatus(customerId, status, pageable);
        } else {
            orderPage = orderRepository.findByCustomerId(customerId, pageable);
        }

        orderPage.getContent().forEach(order -> {
            if (order.getItems() != null) order.getItems().forEach(item -> {
                if (item.getProduct() != null && item.getProduct().getImages() != null) {
                    item.getProduct().getImages().size();
                }
            });
        });

        List<OrderHistoryItemResponse> items = orderPage.getContent().stream()
                .map(OrderHistoryItemResponse::fromEntity)
                .collect(Collectors.toList());

        return new OrderHistoryPageResponse(
                items,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.hasNext(),
                orderPage.hasPrevious()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long customerId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với id: " + orderId));

        if (order.getCustomer() == null || !order.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này");
        }

        if (order.getItems() != null) {
            order.getItems().forEach(item -> {
                if (item.getProduct() != null && item.getProduct().getImages() != null) {
                    item.getProduct().getImages().size();
                }
            });
        }
        if (order.getReviews() != null) {
            order.getReviews().size();
        }

        return OrderDetailResponse.fromEntity(order);
    }

    @Override
    @Transactional
    public OrderDetailResponse cancelOrder(Long customerId, Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với id: " + orderId));

        if (order.getCustomer() == null || !order.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Bạn không có quyền hủy đơn hàng này");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException(
                    "Chỉ có thể hủy đơn hàng đang ở trạng thái PENDING. Trạng thái hiện tại: " + order.getStatus()
            );
        }

        restoreStock(order);

        order.setStatus(OrderStatus.CANCELLED);
        String cancelNote = reason != null ? reason : "Khách hàng hủy đơn";
        order.setCancelReason(cancelNote);
        Order saved = orderRepository.save(order);

        OrderStatusLog statusLog = OrderStatusLog.builder()
                .order(saved)
                .status(OrderStatus.CANCELLED)
                .note(cancelNote)
                .changedBy(saved.getCustomer())
                .build();
        orderStatusLogRepository.save(statusLog);

        // Eagerly initialize for response
        if (saved.getItems() != null) {
            saved.getItems().forEach(item -> {
                if (item.getProduct() != null && item.getProduct().getImages() != null) {
                    item.getProduct().getImages().size();
                }
            });
        }
        if (saved.getReviews() != null) saved.getReviews().size();

        return OrderDetailResponse.fromEntity(saved);
    }

    private void restoreStock(Order order) {
        if (order.getItems() == null) {
            return;
        }

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product == null || item.getQuantity() == null) {
                continue;
            }

            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            if (product.getStockQuantity() > 0) {
                product.setIsActive(true);
            }
            productRepository.save(product);

            try {
                java.util.Map<String, Object> eventData = new java.util.HashMap<>();
                eventData.put("productId", product.getId());
                eventData.put("stockQuantity", product.getStockQuantity());
                eventData.put("isActive", product.getIsActive());
                sseEmitterService.broadcastToChannel("product-stock", "stock-updated", eventData);
            } catch (Exception e) {
                // Fail-silent for SSE broadcast so it doesn't interrupt cancel transaction
            }
        }
    }

    @Override
    @Transactional
    public com.be.dto.response.customer.CheckoutResponse checkout(CheckoutRequest request) {
        // 1. Fetch customer
        User customer = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tài khoản khách hàng"));

        // 2. Fetch shipping address
        UserAddress shippingAddress = null;
        if (request.getShippingAddressId() != null) {
            shippingAddress = userAddressRepository.findById(request.getShippingAddressId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ giao hàng"));
        } else {
            // Find default address
            shippingAddress = userAddressRepository.findByUserIdAndIsDefaultTrue(customer.getId())
                    .orElse(null);
            if (shippingAddress == null) {
                // If no default address, check if user has any address
                List<UserAddress> addresses = userAddressRepository.findByUserId(customer.getId());
                if (!addresses.isEmpty()) {
                    shippingAddress = addresses.get(0);
                }
            }
        }
        if (shippingAddress == null) {
            throw new IllegalArgumentException("Khách hàng chưa thiết lập địa chỉ giao hàng. Vui lòng thêm địa chỉ.");
        }

        // 3. Resolve products and group items by Shop
        java.util.Map<Long, Integer> requestedQuantities = new java.util.HashMap<>();
        for (CheckoutItem item : request.getItems()) {
            if (item.getProductId() == null || item.getQuantity() == null || item.getQuantity() < 1) {
                throw new IllegalArgumentException("Thông tin sản phẩm đặt hàng không hợp lệ.");
            }
            requestedQuantities.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }

        if (requestedQuantities.isEmpty()) {
            throw new IllegalArgumentException("Không có sản phẩm nào để đặt hàng.");
        }

        java.util.Map<Shop, java.util.List<CheckoutItem>> shopItemsMap = new java.util.HashMap<>();
        java.util.Map<Long, Product> productCache = new java.util.HashMap<>();

        for (java.util.Map.Entry<Long, Integer> requestedItem : requestedQuantities.entrySet()) {
            Long productId = requestedItem.getKey();
            Integer requestedQuantity = requestedItem.getValue();
            Product product = productRepository.findByIdForUpdate(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với id: " + productId));

            if (Boolean.FALSE.equals(product.getIsActive()) || product.getStockQuantity() < requestedQuantity) {
                throw new IllegalArgumentException("Sản phẩm '" + product.getName() + "' đã hết hàng hoặc không khả dụng.");
            }

            productCache.put(product.getId(), product);
            Shop shop = product.getShop();
            shop.getId();
            shopItemsMap.computeIfAbsent(shop, k -> new java.util.ArrayList<>())
                    .add(CheckoutItem.builder()
                            .productId(productId)
                            .quantity(requestedQuantity)
                            .build());
        }

        // 4. Validate Wallet balance if payment method is WALLET
        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ. Chỉ chấp nhận WALLET hoặc COD.");
        }

        BigDecimal itemsSubtotal = BigDecimal.ZERO;
        java.util.Map<Shop, BigDecimal> subtotalByShop = new java.util.HashMap<>();
        java.util.Map<Shop, BigDecimal> shippingFeeByShop = new java.util.HashMap<>();
        BigDecimal totalShippingFee = BigDecimal.ZERO;

        for (java.util.Map.Entry<Shop, java.util.List<CheckoutItem>> entry : shopItemsMap.entrySet()) {
            Shop shop = entry.getKey();
            BigDecimal subtotal = BigDecimal.ZERO;
            int totalWeight = 0;
            int totalHeight = 0;
            
            for (CheckoutItem ci : entry.getValue()) {
                Product p = productCache.get(ci.getProductId());
                BigDecimal price = p.getSalePrice() != null ? p.getSalePrice() : p.getBasePrice();
                subtotal = subtotal.add(price.multiply(BigDecimal.valueOf(ci.getQuantity())));
                
                int qty = ci.getQuantity();
                totalWeight += (p.getWeight() != null ? p.getWeight() : 500) * qty;
                totalHeight += (p.getHeight() != null ? p.getHeight() : 5) * qty;
            }
            subtotalByShop.put(shop, subtotal);
            itemsSubtotal = itemsSubtotal.add(subtotal);

            // Tính phí ship qua GHN API cho shop hiện tại
            BigDecimal shopShippingFee = calculateGhnFee(
                    shop.getDistrictId(),
                    shop.getWardCode(),
                    shippingAddress.getDistrictId(),
                    shippingAddress.getWardCode(),
                    totalWeight,
                    20, // length
                    15, // width
                    totalHeight,
                    subtotal
            );
            shippingFeeByShop.put(shop, shopShippingFee);
            totalShippingFee = totalShippingFee.add(shopShippingFee);
        }

        Coupon appliedCoupon = null;
        BigDecimal couponDiscount = BigDecimal.ZERO;
        BigDecimal couponEligibleSubtotal = itemsSubtotal;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            String normalizedCode = request.getCouponCode().trim().toUpperCase();
            appliedCoupon = couponRepository.findByCodeForUpdate(normalizedCode)
                    .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không tồn tại"));

            if (appliedCoupon.getShop() != null) {
                Long couponShopId = appliedCoupon.getShop().getId();
                couponEligibleSubtotal = subtotalByShop.entrySet().stream()
                        .filter(entry -> entry.getKey().getId().equals(couponShopId))
                        .map(java.util.Map.Entry::getValue)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không áp dụng cho các sản phẩm đã chọn"));
            }

            com.be.dto.response.CouponValidationResponse validation =
                    promotionService.validateCoupon(normalizedCode, couponEligibleSubtotal);
            if (!validation.isValid()) {
                throw new IllegalArgumentException(validation.getMessage());
            }
            couponDiscount = validation.getDiscountAmount();
        }

        BigDecimal totalCheckoutCost = itemsSubtotal
                .add(totalShippingFee)
                .subtract(couponDiscount);

        String paymentRef = null;
        if (paymentMethod == PaymentMethod.WALLET) {
            paymentRef = "PAY-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 900 + 100);
        }

        // 5. Create Orders
        List<Order> createdOrders = new java.util.ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        BigDecimal remainingDiscount = couponDiscount;
        BigDecimal remainingEligibleSubtotal = couponEligibleSubtotal;

        for (java.util.Map.Entry<Shop, java.util.List<CheckoutItem>> entry : shopItemsMap.entrySet()) {
            Shop shop = entry.getKey();
            java.util.List<CheckoutItem> items = entry.getValue();

            // Generate unique order code
            String orderCode = "ORD-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 9000 + 1000);

            BigDecimal subtotal = BigDecimal.ZERO;
            java.util.List<OrderItem> orderItems = new java.util.ArrayList<>();
            BigDecimal shopShippingFee = shippingFeeByShop.getOrDefault(shop, new BigDecimal("30000"));

            Order order = Order.builder()
                    .customer(customer)
                    .shop(shop)
                    .orderCode(orderCode)
                    .shippingAddress(shippingAddress)
                    .shippingFee(shopShippingFee)
                    .status(OrderStatus.PENDING)
                    .paymentMethod(paymentMethod)
                    .paymentStatus(PaymentStatus.UNPAID)
                    .paymentRef(paymentRef)
                    .paidAt(null)
                    .build();

            for (CheckoutItem ci : items) {
                Product p = productCache.get(ci.getProductId());
                BigDecimal price = p.getSalePrice() != null ? p.getSalePrice() : p.getBasePrice();
                BigDecimal itemSubtotal = price.multiply(BigDecimal.valueOf(ci.getQuantity()));
                subtotal = subtotal.add(itemSubtotal);

                // Order is being created as PENDING; reserve stock in the same transaction.
                p.setStockQuantity(p.getStockQuantity() - ci.getQuantity());
                if (p.getStockQuantity() <= 0) {
                    p.setIsActive(false);
                }
                productRepository.save(p);

                try {
                    java.util.Map<String, Object> eventData = new java.util.HashMap<>();
                    eventData.put("productId", p.getId());
                    eventData.put("stockQuantity", p.getStockQuantity());
                    eventData.put("isActive", p.getIsActive());
                    sseEmitterService.broadcastToChannel("product-stock", "stock-updated", eventData);
                } catch (Exception e) {
                    // Fail-silent for SSE broadcast so it doesn't interrupt checkout transaction
                }

                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .product(p)
                        .productName(p.getName())
                        .unitPrice(price)
                        .quantity(ci.getQuantity())
                        .subtotal(itemSubtotal)
                        .build();

                orderItems.add(orderItem);
            }

            BigDecimal orderDiscount = BigDecimal.ZERO;
            if (appliedCoupon != null && remainingDiscount.signum() > 0) {
                boolean eligible = appliedCoupon.getShop() == null
                        || appliedCoupon.getShop().getId().equals(shop.getId());
                if (eligible) {
                    if (remainingEligibleSubtotal.compareTo(subtotal) == 0) {
                        orderDiscount = remainingDiscount;
                    } else {
                        orderDiscount = couponDiscount.multiply(subtotal)
                                .divide(couponEligibleSubtotal, 2, java.math.RoundingMode.HALF_UP)
                                .min(remainingDiscount);
                    }
                    remainingDiscount = remainingDiscount.subtract(orderDiscount);
                    remainingEligibleSubtotal = remainingEligibleSubtotal.subtract(subtotal);
                }
            }

            order.setSubtotal(subtotal);
            order.setDiscountAmount(orderDiscount);
            order.setCoupon(orderDiscount.signum() > 0 ? appliedCoupon : null);
            order.setItems(orderItems);

            // Add OrderStatusLog
            OrderStatusLog log = OrderStatusLog.builder()
                    .order(order)
                    .status(OrderStatus.PENDING)
                    .note("Đơn hàng được khởi tạo từ giỏ hàng")
                    .changedBy(customer)
                    .build();
            java.util.List<OrderStatusLog> statusLogList = new java.util.ArrayList<>();
            statusLogList.add(log);
            order.setStatusLogs(statusLogList);

            Order savedOrder = orderRepository.save(order);
            createdOrders.add(savedOrder);
        }

        if (appliedCoupon != null) {
            int usedCount = appliedCoupon.getUsedCount() == null ? 0 : appliedCoupon.getUsedCount();
            appliedCoupon.setUsedCount(usedCount + 1);
            couponRepository.save(appliedCoupon);
        }

        // 6. Generate VNPay URL if payment method is WALLET
        String paymentUrl = null;
        if (paymentMethod == PaymentMethod.WALLET && paymentRef != null) {
            try {
                java.util.Map<String, String> vnp_Params = new java.util.HashMap<>();
                vnp_Params.put("vnp_Version", "2.1.0");
                vnp_Params.put("vnp_Command", "pay");
                vnp_Params.put("vnp_TmnCode", com.be.utils.VNPayUtil.vnp_TmnCode);
                long amountInVndLong = totalCheckoutCost.multiply(new BigDecimal("100")).longValue();
                vnp_Params.put("vnp_Amount", String.valueOf(amountInVndLong));
                vnp_Params.put("vnp_CurrCode", "VND");
                vnp_Params.put("vnp_TxnRef", paymentRef);
                vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + paymentRef);
                vnp_Params.put("vnp_OrderType", "other");
                vnp_Params.put("vnp_Locale", "vn");
                vnp_Params.put("vnp_ReturnUrl", com.be.utils.VNPayUtil.vnp_ReturnUrl);
                vnp_Params.put("vnp_IpAddr", "127.0.0.1");

                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                vnp_Params.put("vnp_CreateDate", now.format(formatter));

                java.util.List<String> fieldNames = new java.util.ArrayList<>(vnp_Params.keySet());
                java.util.Collections.sort(fieldNames);

                StringBuilder hashData = new StringBuilder();
                StringBuilder query = new StringBuilder();
                boolean isFirst = true;
                for (String fieldName : fieldNames) {
                    String fieldValue = vnp_Params.get(fieldName);
                    if (fieldValue != null && !fieldValue.isEmpty()) {
                        if (!isFirst) {
                            hashData.append("&");
                            query.append("&");
                        }
                        // Theo chuẩn chính thức VNPAY: hashData dùng URLEncoder.encode(US_ASCII)
                        // Space -> '+', ký tự đặc biệt -> %XX
                        String encodedValue = java.net.URLEncoder.encode(fieldValue, java.nio.charset.StandardCharsets.US_ASCII);
                        hashData.append(fieldName).append("=").append(encodedValue);
                        query.append(fieldName).append("=").append(encodedValue);

                        isFirst = false;
                    }
                }

                String vnp_SecureHash = com.be.utils.VNPayUtil.hmacSHA512(com.be.utils.VNPayUtil.secretKey, hashData.toString());
                paymentUrl = com.be.utils.VNPayUtil.vnp_PayUrl + "?" + query.toString() + "&vnp_SecureHash=" + vnp_SecureHash;
            } catch (Exception e) {
                throw new RuntimeException("Lỗi tạo link thanh toán VNPay: " + e.getMessage());
            }
        }

        List<OrderDetailResponse> orderDetailResponses = createdOrders.stream()
                .map(OrderDetailResponse::fromEntity)
                .collect(Collectors.toList());

        return com.be.dto.response.customer.CheckoutResponse.builder()
                .orders(orderDetailResponses)
                .paymentUrl(paymentUrl)
                .build();
    }

    /**
     * Gọi API Giao Hàng Nhanh (GHN) để tính phí vận chuyển từ địa chỉ Shop đến người mua.
     */
    private BigDecimal calculateGhnFee(Integer fromDistrictId, String fromWardCode, 
                                        Integer toDistrictId, String toWardCode, 
                                        int weight, int length, int width, int height,
                                        BigDecimal insuranceValue) {
        if (fromDistrictId == null || fromWardCode == null || toDistrictId == null || toWardCode == null) {
            return new BigDecimal("30000"); // Fallback phí cố định nếu thiếu dữ liệu địa chỉ
        }

        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            String url = ghnBaseUrl + "/v2/shipping-order/fee";

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Token", ghnToken);
            headers.set("ShopId", ghnShopId);
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("from_district_id", fromDistrictId);
            requestBody.put("from_ward_code", fromWardCode);
            requestBody.put("service_id", null);
            requestBody.put("service_type_id", 2); // 2: Hàng tiêu chuẩn (Standard)
            requestBody.put("to_district_id", toDistrictId);
            requestBody.put("to_ward_code", toWardCode);
            requestBody.put("height", height);
            requestBody.put("length", length);
            requestBody.put("weight", weight);
            requestBody.put("width", width);
            requestBody.put("insurance_value", insuranceValue.intValue());
            requestBody.put("cod_failed_amount", 0);

            org.springframework.http.HttpEntity<java.util.Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(requestBody, headers);
            
            org.springframework.http.ResponseEntity<java.util.Map> response = restTemplate.postForEntity(url, entity, java.util.Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                java.util.Map body = response.getBody();
                if (body.containsKey("data")) {
                    java.util.Map data = (java.util.Map) body.get("data");
                    if (data.containsKey("total")) {
                        Number total = (Number) data.get("total");
                        return new BigDecimal(total.toString());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi tính phí ship GHN API: " + e.getMessage());
        }
        return new BigDecimal("30000"); // Fallback nếu API lỗi
    }

    @Override
    @Transactional
    public void processVNPayPayment(String paymentRef, String responseCode, String vnpAmountStr) {
        if (paymentRef == null || paymentRef.trim().isEmpty()) {
            throw new IllegalArgumentException("InvalidPaymentReference");
        }

        List<Order> orders = orderRepository.findByPaymentRef(paymentRef);
        if (orders.isEmpty()) {
            throw new IllegalArgumentException("OrderNotFound");
        }

        // 1. Calculate combined total cost of all orders under this payment reference
        BigDecimal combinedTotal = BigDecimal.ZERO;
        for (Order order : orders) {
            BigDecimal orderTotal = order.getSubtotal()
                    .add(order.getShippingFee())
                    .subtract(order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO);
            combinedTotal = combinedTotal.add(orderTotal);
        }

        // 2. Convert to VND cents and verify
        long expectedAmountCents = combinedTotal.multiply(new BigDecimal("100")).longValue();
        long receivedAmountCents = Long.parseLong(vnpAmountStr);
        if (expectedAmountCents != receivedAmountCents) {
            throw new IllegalArgumentException("AmountMismatch");
        }

        // 3. Check if already confirmed (idempotency) and check for cancelled status
        boolean allPaid = true;
        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.CANCELLED) {
                throw new IllegalStateException("OrderCancelled");
            }
            if (order.getPaymentStatus() != PaymentStatus.PAID) {
                allPaid = false;
            }
        }
        if (allPaid) {
            throw new IllegalStateException("AlreadyConfirmed");
        }

        // 4. Process status update idempotently
        LocalDateTime now = LocalDateTime.now();
        for (Order order : orders) {

            if ("00".equals(responseCode)) {
                order.setPaymentStatus(PaymentStatus.PAID);
                order.setPaidAt(now);

                OrderStatusLog log = OrderStatusLog.builder()
                        .order(order)
                        .status(order.getStatus()) // Keep current status (PENDING)
                        .note("Thanh toán thành công qua VNPay (paymentRef: " + paymentRef + ")")
                        .changedBy(order.getCustomer())
                        .build();

                orderStatusLogRepository.save(log);
            } else {
                OrderStatusLog log = OrderStatusLog.builder()
                        .order(order)
                        .status(order.getStatus())
                        .note("Giao dịch thanh toán VNPay thất bại. Mã lỗi: " + responseCode)
                        .changedBy(order.getCustomer())
                        .build();

                orderStatusLogRepository.save(log);
            }
            orderRepository.save(order);
        }
    }

    @Override
    @Transactional
    public String regeneratePaymentUrl(Long customerId, Long orderId) {
        Order targetOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));

        if (targetOrder.getCustomer() == null || !targetOrder.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("Bạn không có quyền thanh toán đơn hàng này");
        }

        if (targetOrder.getPaymentMethod() != PaymentMethod.WALLET) {
            throw new IllegalArgumentException("Phương thức thanh toán không hỗ trợ thanh toán lại");
        }

        if (targetOrder.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("Đơn hàng đã được thanh toán");
        }

        if (targetOrder.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Đơn hàng đã bị hủy, không thể thanh toán");
        }

        String oldPaymentRef = targetOrder.getPaymentRef();
        List<Order> orders;
        if (oldPaymentRef != null && !oldPaymentRef.trim().isEmpty()) {
            orders = orderRepository.findByPaymentRef(oldPaymentRef);
        } else {
            orders = List.of(targetOrder);
        }

        String newPaymentRef = "PAY-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 900 + 100);
        BigDecimal combinedTotal = BigDecimal.ZERO;

        for (Order order : orders) {
            if (order.getPaymentStatus() == PaymentStatus.PAID) {
                throw new IllegalStateException("Một trong các đơn hàng thuộc giao dịch này đã được thanh toán");
            }
            if (order.getStatus() == OrderStatus.CANCELLED) {
                throw new IllegalStateException("Một trong các đơn hàng thuộc giao dịch này đã bị hủy");
            }
            order.setPaymentRef(newPaymentRef);
            orderRepository.save(order);

            BigDecimal orderTotal = order.getSubtotal()
                    .add(order.getShippingFee())
                    .subtract(order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO);
            combinedTotal = combinedTotal.add(orderTotal);
        }

        try {
            java.util.Map<String, String> vnp_Params = new java.util.HashMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", com.be.utils.VNPayUtil.vnp_TmnCode);
            long amountInVndLong = combinedTotal.multiply(new BigDecimal("100")).longValue();
            vnp_Params.put("vnp_Amount", String.valueOf(amountInVndLong));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", newPaymentRef);
            vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + newPaymentRef);
            vnp_Params.put("vnp_OrderType", "other");
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", com.be.utils.VNPayUtil.vnp_ReturnUrl);
            vnp_Params.put("vnp_IpAddr", "127.0.0.1");

            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            vnp_Params.put("vnp_CreateDate", LocalDateTime.now().format(formatter));

            java.util.List<String> fieldNames = new java.util.ArrayList<>(vnp_Params.keySet());
            java.util.Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            boolean isFirst = true;
            for (String fieldName : fieldNames) {
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    if (!isFirst) {
                        hashData.append("&");
                        query.append("&");
                    }
                    // Theo chuẩn chính thức VNPAY: hashData dùng URLEncoder.encode(US_ASCII)
                    // Space -> '+', ký tự đặc biệt -> %XX
                    String encodedValue = java.net.URLEncoder.encode(fieldValue, java.nio.charset.StandardCharsets.US_ASCII);
                    hashData.append(fieldName).append("=").append(encodedValue);
                    query.append(fieldName).append("=").append(encodedValue);

                    isFirst = false;
                }
            }

            String vnp_SecureHash = com.be.utils.VNPayUtil.hmacSHA512(com.be.utils.VNPayUtil.secretKey, hashData.toString());
            return com.be.utils.VNPayUtil.vnp_PayUrl + "?" + query.toString() + "&vnp_SecureHash=" + vnp_SecureHash;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo link thanh toán VNPay: " + e.getMessage());
        }
    }

    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void autoCancelUnpaidOrders() {
        LocalDateTime limit = LocalDateTime.now().minusMinutes(15);
        List<Order> ordersToCancel = orderRepository.findExpiredOrders(
                OrderStatus.PENDING,
                PaymentStatus.UNPAID,
                PaymentMethod.WALLET,
                limit
        );

        if (ordersToCancel.isEmpty()) {
            return;
        }

        for (Order order : ordersToCancel) {
            try {
                restoreStock(order);
                order.setStatus(OrderStatus.CANCELLED);
                order.setCancelReason("Hệ thống tự động hủy đơn hàng do hết thời gian chờ thanh toán (15 phút).");
                orderRepository.save(order);

                OrderStatusLog log = OrderStatusLog.builder()
                        .order(order)
                        .status(OrderStatus.CANCELLED)
                        .note("Hủy tự động do hết hạn thanh toán (15 phút)")
                        .build();
                orderStatusLogRepository.save(log);
            } catch (Exception e) {
                System.err.println("Lỗi tự động hủy đơn hàng ID " + order.getId() + ": " + e.getMessage());
            }
        }
    }
}
