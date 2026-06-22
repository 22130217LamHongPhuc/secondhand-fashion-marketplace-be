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
import com.be.entity.Category;
import com.be.entity.Shop;
import com.be.entity.Complaint;
import com.be.common.enums.ComplaintStatus;
import com.be.repository.CategoryRepository;
import com.be.repository.ShopRepository;
import com.be.repository.ComplaintRepository;
import com.be.repository.RoleRepository;
import com.be.entity.Role;
import com.be.entity.UserRoleMapping;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;
    private final ComplaintRepository complaintRepository;
    private final RoleRepository roleRepository;

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
    public User updateUserRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String cleanedRole = role.toUpperCase().replace("ROLE_", "").replace("BUYER", "CUSTOMER");
        UserRole userRole = UserRole.valueOf(cleanedRole);
        
        Role targetRole = roleRepository.findByName(userRole)
                .orElseThrow(() -> new RuntimeException("Role not found: " + userRole));
        
        if (user.getUserRoles() == null) {
            user.setUserRoles(new java.util.ArrayList<>());
        } else {
            user.getUserRoles().clear();
        }
        
        user.getUserRoles().add(UserRoleMapping.builder()
                .user(user)
                .role(targetRole)
                .build());
        
        user.setRole(userRole);
                
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

    // ============ CATEGORY MANAGEMENT ============
    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        if (categoryRepository.existsByParentId(categoryId)) {
            throw new RuntimeException("Category has child categories and cannot be deleted");
        }
        categoryRepository.deleteById(categoryId);
    }

    // ============ SHOP MANAGEMENT ============
    @Override
    public List<Shop> getAllShops() {
        return shopRepository.findAll();
    }

    @Override
    @Transactional
    public Shop verifyShop(Long shopId, boolean verify) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + shopId));
        shop.setIsVerified(verify);
        return shopRepository.save(shop);
    }

    @Override
    @Transactional
    public Shop addWarningStrike(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + shopId));
        int strikes = shop.getWarningStrikes() + 1;
        shop.setWarningStrikes(strikes);
        if (strikes >= 5) {
            shop.setIsActive(false);
        }
        return shopRepository.save(shop);
    }

    @Override
    @Transactional
    public Shop resetStrikes(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + shopId));
        shop.setWarningStrikes(0);
        return shopRepository.save(shop);
    }

    @Override
    @Transactional
    public Shop toggleShopActive(Long shopId, boolean active) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + shopId));
        shop.setIsActive(active);
        return shopRepository.save(shop);
    }

    // ============ COMPLAINT MANAGEMENT ============
    @Override
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    @Override
    @Transactional
    public Complaint updateComplaintStatus(Long complaintId, ComplaintStatus status, String resolution) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found with id: " + complaintId));
        complaint.setStatus(status);
        if (resolution != null) {
            complaint.setResolution(resolution);
        }
        return complaintRepository.save(complaint);
    }
}
