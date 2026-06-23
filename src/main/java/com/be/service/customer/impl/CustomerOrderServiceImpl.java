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
import com.be.entity.Product;
import com.be.entity.Shop;
import com.be.entity.User;
import com.be.entity.UserAddress;
import com.be.entity.Wallet;
import com.be.entity.WalletTransaction;
import com.be.entity.Role;
import com.be.entity.UserRoleMapping;
import com.be.repository.*;
import com.be.service.customer.CustomerOrderService;
import com.be.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class CustomerOrderServiceImpl implements CustomerOrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final UserAddressRepository userAddressRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final RoleRepository roleRepository;
    private final UserRoleMappingRepository userRoleMappingRepository;
    private final SseEmitterService sseEmitterService;
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
        java.util.Map<Shop, java.util.List<CheckoutItem>> shopItemsMap = new java.util.HashMap<>();
        java.util.Map<Long, Product> productCache = new java.util.HashMap<>();

        for (CheckoutItem item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với id: " + item.getProductId()));

            if (Boolean.FALSE.equals(product.getIsActive()) || product.getStockQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException("Sản phẩm '" + product.getName() + "' đã hết hàng hoặc không khả dụng.");
            }

            productCache.put(product.getId(), product);
            Shop shop = product.getShop();
            shopItemsMap.computeIfAbsent(shop, k -> new java.util.ArrayList<>()).add(item);
        }

        // 4. Validate Wallet balance if payment method is WALLET
        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ. Chỉ chấp nhận WALLET hoặc COD.");
        }

        BigDecimal totalCheckoutCost = BigDecimal.ZERO;
        BigDecimal shippingFeePerOrder = new BigDecimal("30000"); // 30,000 VND shipping fee per shop order

        for (java.util.Map.Entry<Shop, java.util.List<CheckoutItem>> entry : shopItemsMap.entrySet()) {
            BigDecimal subtotal = BigDecimal.ZERO;
            for (CheckoutItem ci : entry.getValue()) {
                Product p = productCache.get(ci.getProductId());
                BigDecimal price = p.getSalePrice() != null ? p.getSalePrice() : p.getBasePrice();
                subtotal = subtotal.add(price.multiply(BigDecimal.valueOf(ci.getQuantity())));
            }
            totalCheckoutCost = totalCheckoutCost.add(subtotal).add(shippingFeePerOrder);
        }

        String paymentRef = null;
        if (paymentMethod == PaymentMethod.WALLET) {
            paymentRef = "PAY-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 900 + 100);
        }

        // 5. Create Orders
        List<Order> createdOrders = new java.util.ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (java.util.Map.Entry<Shop, java.util.List<CheckoutItem>> entry : shopItemsMap.entrySet()) {
            Shop shop = entry.getKey();
            java.util.List<CheckoutItem> items = entry.getValue();

            // Generate unique order code
            String orderCode = "ORD-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 9000 + 1000);

            BigDecimal subtotal = BigDecimal.ZERO;
            java.util.List<OrderItem> orderItems = new java.util.ArrayList<>();

            Order order = Order.builder()
                    .customer(customer)
                    .shop(shop)
                    .orderCode(orderCode)
                    .shippingAddress(shippingAddress)
                    .shippingFee(shippingFeePerOrder)
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

                // Decrement stock quantity
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

            order.setSubtotal(subtotal);
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
                vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + paymentRef);
                vnp_Params.put("vnp_OrderType", "other");
                vnp_Params.put("vnp_Locale", "vn");
                vnp_Params.put("vnp_ReturnUrl", com.be.utils.VNPayUtil.vnp_ReturnUrl);
                vnp_Params.put("vnp_IpAddr", "127.0.0.1");

                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                vnp_Params.put("vnp_CreateDate", now.format(formatter));

                java.util.List<String> fieldNames = new java.util.ArrayList<>(vnp_Params.keySet());
                java.util.Collections.sort(fieldNames);
                StringBuilder query = new StringBuilder();
                java.util.Iterator<String> itr = fieldNames.iterator();
                while (itr.hasNext()) {
                    String fieldName = itr.next();
                    String fieldValue = vnp_Params.get(fieldName);
                    if (fieldValue != null && !fieldValue.isEmpty()) {
                        query.append(java.net.URLEncoder.encode(fieldName, java.nio.charset.StandardCharsets.US_ASCII.toString()));
                        query.append("=");
                        query.append(java.net.URLEncoder.encode(fieldValue, java.nio.charset.StandardCharsets.US_ASCII.toString()));
                        if (itr.hasNext()) {
                            query.append("&");
                        }
                    }
                }

                String queryUrl = query.toString();
                String vnp_SecureHash = com.be.utils.VNPayUtil.hmacSHA512(com.be.utils.VNPayUtil.secretKey, queryUrl);
                paymentUrl = com.be.utils.VNPayUtil.vnp_PayUrl + "?" + queryUrl + "&vnp_SecureHash=" + vnp_SecureHash;
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
}
