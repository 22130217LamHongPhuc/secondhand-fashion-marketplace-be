package com.be.service.impl;

import com.be.common.enums.OrderStatus;
import com.be.common.enums.UserRole;
import com.be.dto.response.AdminDashboardResponse;
import com.be.entity.User;
import com.be.repository.OrderRepository;
import com.be.repository.ProductRepository;
import com.be.repository.UserRepository;
import com.be.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Override
    public AdminDashboardResponse getDashboardStats() {
        BigDecimal totalRevenue = orderRepository.sumTotalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        return AdminDashboardResponse.builder()
                .totalUsers(userRepository.count())
                .totalSellers(userRepository.countByRole(UserRole.SELLER))
                .totalProducts(productRepository.count())
                .totalOrders(orderRepository.count())
                .totalRevenue(totalRevenue)
                .activeUsers(userRepository.findAll().stream().filter(User::getIsActive).count()) // Note: Should optimize this count later
                .pendingOrders(orderRepository.countByStatus(OrderStatus.PENDING))
                .recentOrders(orderRepository.findAll(org.springframework.data.domain.PageRequest.of(0, 5, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")))
                        .getContent().stream()
                        .map(order -> AdminDashboardResponse.OrderSummary.builder()
                        .id(String.valueOf(order.getId()))
                                .customerName(order.getCustomer() != null ? order.getCustomer().getFullName() : "Unknown")
                                .total(order.getSubtotal())
                                .status(order.getStatus().name())
                                .build())
                        .collect(java.util.stream.Collectors.toList()))
                .build();
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    @Override
    @Transactional
    public User updateUserStatus(Long userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsActive(isActive);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        productRepository.deleteById(productId);
    }
}
