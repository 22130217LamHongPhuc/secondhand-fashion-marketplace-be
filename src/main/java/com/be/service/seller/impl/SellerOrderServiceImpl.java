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
import com.be.service.seller.SellerOrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class SellerOrderServiceImpl implements SellerOrderService {
    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;
    private final ProductRepository productRepository;
    private final AuthHelper authHelper;

    @Override
    public Page<OrderListResponse> getListByPage(int page) {
        Shop shop = authHelper.getCurrentSellerShop();
        Page<Order> orders = orderRepository.getListByShopAndPage(shop.getId(), PageRequest.of(page, Constant.ORDER_SIZE));
        return orders.map(SellerOrderMapper::toListResponse);
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
    public Page<OrderListResponse> getListByStatusAndOrderCode(OrderStatus status, String orderCode, int page) {
        Shop shop = authHelper.getCurrentSellerShop();
        Page<Order> orders = orderRepository.getListByShopAndStatusAndOrderCode(shop.getId(), status, orderCode, PageRequest.of(page, Constant.ORDER_SIZE));
        return orders.map(SellerOrderMapper::toListResponse);
    }

    @Override
    public Page<OrderListResponse> getListByMonth(int year, int month, int page) {
        Shop shop = authHelper.getCurrentSellerShop();
        YearMonth yearMonth = YearMonth.of(year, month);
        Page<Order> orders = orderRepository.getListByShopAndMonth(
                shop.getId(),
                yearMonth.atDay(1).atStartOfDay(),
                yearMonth.plusMonths(1).atDay(1).atStartOfDay(),
                PageRequest.of(page, Constant.ORDER_SIZE)
        );
        return orders.map(SellerOrderMapper::toListResponse);
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
        Order order = updateOrderStatus(orderId, OrderStatus.SHIPPING, "Đơn hàng đang được giao");
        return SellerOrderMapper.toActionResponse(order);
    }

    @Override
    @Transactional
    public OrderActionResponse completeOrder(Long orderId) {
        Order order = updateOrderStatus(orderId, OrderStatus.DONE, "Đơn hàng hoàn thành");
        return SellerOrderMapper.toActionResponse(order);
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
