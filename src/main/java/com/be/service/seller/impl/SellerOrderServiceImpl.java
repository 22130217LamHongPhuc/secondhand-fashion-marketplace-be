package com.be.service.seller.impl;

import com.be.common.enums.OrderStatus;
import com.be.constant.Constant;
import com.be.dto.response.seller.OrderListResponse;
import com.be.dto.response.seller.OrderDetailResponse;
import com.be.dto.response.seller.OrderActionResponse;
import com.be.dto.response.seller.mapper.SellerOrderMapper;
import com.be.entity.Order;
import com.be.entity.OrderItem;
import com.be.entity.OrderStatusLog;
import com.be.entity.Product;
import com.be.entity.Shop;
import com.be.entity.User;
import com.be.repository.OrderRepository;
import com.be.repository.OrderStatusLogRepository;
import com.be.repository.ProductRepository;
import com.be.security.AuthHelper;
import com.be.service.GhnShippingService;
import com.be.service.seller.SellerOrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.be.repository.specification.OrderSpecification;

import java.time.YearMonth;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SellerOrderServiceImpl implements SellerOrderService {
    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;
    private final ProductRepository productRepository;
    private final AuthHelper authHelper;
    private final GhnShippingService ghnShippingService;

    @Override
    public Page<OrderListResponse> searchOrders(OrderStatus status, String orderCode, LocalDateTime fromDate, LocalDateTime toDate, BigDecimal minPrice, BigDecimal maxPrice, String sortBy, int page) {
        Shop shop = authHelper.getCurrentSellerShop();
        Specification<Order> spec = OrderSpecification.buildFilter(shop.getId(), status, orderCode, fromDate, toDate, minPrice, maxPrice);
        Sort sort = resolveOrderSort(sortBy);
        Page<Order> orders = orderRepository.findAll(spec, PageRequest.of(page, Constant.ORDER_SIZE, sort));
        return orders.map(SellerOrderMapper::toListResponse);
    }

    private Sort resolveOrderSort(String sortBy) {
        if (sortBy == null) {
            return Sort.by("createdAt").descending();
        }
        return switch (sortBy) {
            case "price_asc" -> Sort.by("subtotal").ascending();
            case "price_desc" -> Sort.by("subtotal").descending();
            case "oldest" -> Sort.by("createdAt").ascending();
            default -> Sort.by("createdAt").descending();
        };
    }

    @Override
    public OrderDetailResponse getDetails(Long id) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng với id: " + id));
        Shop shop = authHelper.getCurrentSellerShop();
        if (!order.getShop().getId().equals(shop.getId())) {
            throw new IllegalStateException("Bạn không có quyền xem đơn hàng này");
        }
        return SellerOrderMapper.toDetailResponse(order);
    }


    @Override
    @Transactional
    public OrderActionResponse confirmOrder(Long orderId) {
        Order order = updateOrderStatus(orderId, OrderStatus.CONFIRMED, "Đơn hàng đã được xác nhận bởi cửa hàng");
        return SellerOrderMapper.toActionResponse(order);
    }

    @Override
    @Transactional
    public OrderActionResponse startDelivery(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với id: " + orderId));
        
        Shop shop = authHelper.getCurrentSellerShop();
        if (!order.getShop().getId().equals(shop.getId())) {
            throw new IllegalStateException("Bạn không có quyền cập nhật đơn hàng này");
        }
        
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Trạng thái chuyển đổi không hợp lệ. Chỉ có thể giao đơn hàng đã xác nhận.");
        }

        // Gọi GHN API tạo đơn giao hàng
        GhnShippingService.CreateOrderResult ghnResult = ghnShippingService.createShippingOrder(order);
        
        order.setGhnOrderCode(ghnResult.orderCode());
        order.setExpectedDeliveryTime(ghnResult.expectedDeliveryTime());
        order.setGhnTotalFee(ghnResult.totalFee());
        orderRepository.save(order); // Lưu trước thông tin để updateOrderStatus có thể dùng instance này
        
        Order savedOrder = updateOrderStatus(orderId, OrderStatus.SHIPPING, "Đơn hàng đang được giao (Mã GHN: " + ghnResult.orderCode() + ")");
        
        // Ensure the GHN fields are included in the mapper by passing the updated instance if updateOrderStatus fetched a stale one (though 1st level cache prevents this)
        savedOrder.setGhnOrderCode(ghnResult.orderCode());
        
        return SellerOrderMapper.toActionResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderActionResponse completeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với id: " + orderId));
        order.setDeliveredAt(LocalDateTime.now());
        orderRepository.save(order);
        
        Order savedOrder = updateOrderStatus(orderId, OrderStatus.DONE, "Đơn hàng hoàn thành");
        return SellerOrderMapper.toActionResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderActionResponse cancelOrder(Long orderId, String cancelReason) {
        Order order = updateOrderStatus(orderId, OrderStatus.CANCELLED, cancelReason != null ? cancelReason : "Cửa hàng hủy đơn");
        order.setCancelReason(cancelReason);
        Order savedOrder = orderRepository.save(order);
        return SellerOrderMapper.toActionResponse(savedOrder);
    }

    private Order updateOrderStatus(Long orderId, OrderStatus status, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với id: " + orderId));

        Shop shop = authHelper.getCurrentSellerShop();
        if (!order.getShop().getId().equals(shop.getId())) {
            throw new IllegalStateException("Bạn không có quyền cập nhật đơn hàng này");
        }

        OrderStatus previousStatus = order.getStatus();
        validateStatusTransition(previousStatus, status);
        if (status == OrderStatus.CANCELLED && previousStatus != OrderStatus.CANCELLED) {
            restoreStock(order);
        }
        order.setStatus(status);

        OrderStatusLog log = OrderStatusLog.builder()
                .order(order)
                .status(status)
                .note(note)
                .build();
        orderStatusLogRepository.save(log);

        return orderRepository.save(order);
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        boolean valid = switch (currentStatus) {
            case PENDING -> newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.CONFIRMED;
            case CONFIRMED -> newStatus == OrderStatus.SHIPPING;
            case SHIPPING -> newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.DONE;
            default -> false;
        };

        if (!valid) {
            throw new IllegalStateException("Trạng thái chuyển đổi không hợp lệ");
        }
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
        }
    }
}
