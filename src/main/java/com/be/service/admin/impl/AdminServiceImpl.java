package com.be.service.admin.impl;

import com.be.common.enums.OrderStatus;
import com.be.common.enums.UserRole;
import com.be.dto.response.AdminDashboardResponse;
import com.be.entity.User;
import com.be.repository.OrderRepository;
import com.be.repository.ProductRepository;
import com.be.repository.UserRepository;
import com.be.service.admin.AdminService;
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
import com.be.entity.Role;
import com.be.entity.UserRoleMapping;
import com.be.repository.RoleRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;
    private final ComplaintRepository complaintRepository;
    private final RoleRepository roleRepository;
    private final com.be.service.SseEmitterService sseEmitterService;

    private double calculateGrowthPercentage(double current, double previous) {
        if (previous <= 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((current - previous) / previous) * 100.0;
    }

    @Override
    public AdminDashboardResponse getDashboardStats() {
        BigDecimal totalRevenue = orderRepository.sumTotalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        long totalUsers = userRepository.count();
        long totalOrders = orderRepository.count();

        java.time.LocalDateTime thirtyDaysAgo = java.time.LocalDateTime.now().minusDays(30);
        java.time.LocalDateTime sixtyDaysAgo = java.time.LocalDateTime.now().minusDays(60);

        // Growth calculations
        long usersThisMonth = userRepository.countByCreatedAtAfter(thirtyDaysAgo);
        long usersPrevMonth = userRepository.countByCreatedAtBetween(sixtyDaysAgo, thirtyDaysAgo);
        double userGrowth = calculateGrowthPercentage(usersThisMonth, usersPrevMonth);

        long ordersThisMonth = orderRepository.countByCreatedAtAfter(thirtyDaysAgo);
        long ordersPrevMonth = orderRepository.countByCreatedAtBetween(sixtyDaysAgo, thirtyDaysAgo);
        double orderGrowth = calculateGrowthPercentage(ordersThisMonth, ordersPrevMonth);

        BigDecimal revenueThisMonth = orderRepository.sumRevenueSince(thirtyDaysAgo, OrderStatus.DONE);
        BigDecimal revenuePrevMonth = orderRepository.sumRevenueBetween(sixtyDaysAgo, thirtyDaysAgo, OrderStatus.DONE);
        double revenueGrowth = calculateGrowthPercentage(revenueThisMonth.doubleValue(), revenuePrevMonth.doubleValue());

        // Order counts by status
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        long confirmedOrders = orderRepository.countByStatus(OrderStatus.CONFIRMED);
        long shippingOrders = orderRepository.countByStatus(OrderStatus.SHIPPING);
        long completedOrders = orderRepository.countByStatus(OrderStatus.DONE);
        long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);

        // Returned orders (complaints resolved and order not null)
        long returnedOrders = complaintRepository.findAll().stream()
                .filter(c -> c.getStatus() == com.be.common.enums.ComplaintStatus.RESOLVED 
                        && c.getType() == com.be.common.enums.ComplaintType.SHOP_COMPLAINT 
                        && c.getOrder() != null)
                .count();

        double cancellationRate = totalOrders > 0 ? ((double) cancelledOrders / totalOrders) * 100.0 : 0.0;
        double returnRate = totalOrders > 0 ? ((double) returnedOrders / totalOrders) * 100.0 : 0.0;

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalSellers(userRepository.countByRole(UserRole.SELLER))
                .totalProducts(productRepository.count())
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .activeUsers(userRepository.countByIsActiveTrue())
                .pendingOrders(pendingOrders)
                .confirmedOrders(confirmedOrders)
                .shippingOrders(shippingOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .returnedOrders(returnedOrders)
                .cancellationRate(cancellationRate)
                .returnRate(returnRate)
                .userGrowth(userGrowth)
                .orderGrowth(orderGrowth)
                .revenueGrowth(revenueGrowth)
                .pendingComplaints(complaintRepository.countByStatus(com.be.common.enums.ComplaintStatus.PENDING))
                .totalShops(shopRepository.count())
                .activeShops(shopRepository.countByIsActive(true))
                .verifiedShops(shopRepository.countByIsVerified(true))
                .recentOrders(orderRepository.findAll(org.springframework.data.domain.PageRequest.of(0, 5, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")))
                        .getContent().stream()
                        .map(order -> AdminDashboardResponse.OrderSummary.builder()
                                .id(String.valueOf(order.getId()))
                                .customerName(order.getCustomer() != null ? order.getCustomer().getFullName() : "Unknown")
                                .total(order.getSubtotal() != null ? order.getSubtotal() : BigDecimal.ZERO)
                                .status(order.getStatus() != null ? order.getStatus().name() : "UNKNOWN")
                                .build())
                        .collect(java.util.stream.Collectors.toList()))
                .build();
    }

    @Override
    public Page<User> getAllUsers(UserRole role, Boolean active, String search, Pageable pageable) {
        return userRepository.findAllFiltered(role, active, search, pageable);
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
        
        user.setRole(userRole);
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
        if (category.getId() != null) {
            Category existing = categoryRepository.findById(category.getId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + category.getId()));
            
            existing.setName(category.getName());
            existing.setSlug(category.getSlug());
            existing.setIconUrl(category.getIconUrl());
            existing.setSortOrder(category.getSortOrder());
            existing.setIsActive(category.getIsActive());
            existing.setParent(category.getParent());
            
            return categoryRepository.save(existing);
        } else {
            return categoryRepository.save(category);
        }
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
            
            // Revert user to CUSTOMER if shop gets auto-locked
            User user = shop.getSeller();
            Role targetRole = roleRepository.findByName(UserRole.CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Role not found: CUSTOMER"));
            user.setRole(UserRole.CUSTOMER);
            if (user.getUserRoles() != null && !user.getUserRoles().isEmpty()) {
                user.getUserRoles().get(0).setRole(targetRole);
            }
            userRepository.save(user);
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
        
        // Cập nhật vai trò người dùng tương ứng (SELLER khi được duyệt, CUSTOMER khi huỷ duyệt/khóa)
        User user = shop.getSeller();
        UserRole targetRoleEnum = active ? UserRole.SELLER : UserRole.CUSTOMER;
        
        Role targetRole = roleRepository.findByName(targetRoleEnum)
                .orElseThrow(() -> new RuntimeException("Role not found: " + targetRoleEnum));
        
        user.setRole(targetRoleEnum);
        if (user.getUserRoles() == null) {
            user.setUserRoles(new java.util.ArrayList<>());
        }
        
        if (!user.getUserRoles().isEmpty()) {
            UserRoleMapping firstMapping = user.getUserRoles().get(0);
            firstMapping.setRole(targetRole);
            while (user.getUserRoles().size() > 1) {
                user.getUserRoles().remove(1);
            }
        } else {
            user.getUserRoles().add(UserRoleMapping.builder()
                    .user(user)
                    .role(targetRole)
                    .build());
        }
        
        userRepository.save(user);
        return shopRepository.save(shop);
    }

    // ============ COMPLAINT MANAGEMENT ============
    @Override
    @Transactional
    public List<Complaint> getAllComplaints() {
        List<Complaint> list = complaintRepository.findAll();
        if (list.isEmpty()) {
            User customer = userRepository.findAll().stream()
                    .filter(u -> u.getFullName().toLowerCase().contains("customer") || u.getEmail().contains("customer"))
                    .findFirst()
                    .orElse(userRepository.findAll().stream().findFirst().orElse(null));
            Shop shop = shopRepository.findAll().stream().findFirst().orElse(null);
            if (customer != null && shop != null) {
                com.be.entity.Order orderDone = orderRepository.findAll().stream()
                        .filter(o -> o.getStatus() == OrderStatus.DONE)
                        .findFirst()
                        .orElse(null);
                com.be.entity.Order orderShipping = orderRepository.findAll().stream()
                        .filter(o -> o.getStatus() == OrderStatus.SHIPPING)
                        .findFirst()
                        .orElse(null);
                com.be.entity.Order orderConfirmed = orderRepository.findAll().stream()
                        .filter(o -> o.getStatus() == OrderStatus.CONFIRMED)
                        .findFirst()
                        .orElse(null);

                List<Complaint> complaints = List.of(
                        Complaint.builder()
                                .reporter(orderDone != null && orderDone.getCustomer() != null ? orderDone.getCustomer() : customer)
                                .reportedShop(orderDone != null && orderDone.getShop() != null ? orderDone.getShop() : shop)
                                .order(orderDone)
                                .type(com.be.common.enums.ComplaintType.SHOP_COMPLAINT)
                                .title("Sản phẩm rách nát, khác với hình ảnh mô tả")
                                .content("Tôi mua áo blazer linen với giá 410.000đ nhưng nhận về áo bị rách tay rất to và bẩn. Shop từ chối giải quyết hoàn trả hàng. Đề nghị ban quản trị can thiệp!")
                                .status(com.be.common.enums.ComplaintStatus.PENDING)
                                .severity(com.be.common.enums.ComplaintSeverity.HIGH)
                                .build(),

                        Complaint.builder()
                                .reporter(orderShipping != null && orderShipping.getCustomer() != null ? orderShipping.getCustomer() : customer)
                                .reportedShop(orderShipping != null && orderShipping.getShop() != null ? orderShipping.getShop() : shop)
                                .order(orderShipping)
                                .type(com.be.common.enums.ComplaintType.SHOP_COMPLAINT)
                                .title("Shop không chịu gửi hàng dù đơn hàng đã thanh toán")
                                .content("Tôi đã thanh toán qua thẻ ngân hàng từ 3 ngày trước, đơn hàng báo đang giao nhưng tôi liên hệ shop hỏi mã vận đơn thì không trả lời tin nhắn.")
                                .status(com.be.common.enums.ComplaintStatus.PENDING)
                                .severity(com.be.common.enums.ComplaintSeverity.MEDIUM)
                                .build(),

                        Complaint.builder()
                                .reporter(orderConfirmed != null && orderConfirmed.getCustomer() != null ? orderConfirmed.getCustomer() : customer)
                                .reportedShop(orderConfirmed != null && orderConfirmed.getShop() != null ? orderConfirmed.getShop() : shop)
                                .order(orderConfirmed)
                                .type(com.be.common.enums.ComplaintType.SHOP_COMPLAINT)
                                .title("Shop giao hàng nhái, nghi ngờ hàng giả thương hiệu")
                                .content("Tôi đặt mua sản phẩm được ghi là chính hãng vintage Chanel nhưng khi nhận hàng da rất khét mùi nhựa, logo bị lệch và bong tróc. Shop từ chối hoàn trả tiền.")
                                .status(com.be.common.enums.ComplaintStatus.PENDING)
                                .severity(com.be.common.enums.ComplaintSeverity.HIGH)
                                .build(),

                        Complaint.builder()
                                .reporter(orderDone != null && orderDone.getCustomer() != null ? orderDone.getCustomer() : customer)
                                .reportedShop(orderDone != null && orderDone.getShop() != null ? orderDone.getShop() : shop)
                                .order(orderDone)
                                .type(com.be.common.enums.ComplaintType.SHOP_COMPLAINT)
                                .title("Yêu cầu hoàn trả hàng do giao chậm trễ")
                                .content("Tôi đặt mua sản phẩm để đi tiệc nhưng shop chuẩn bị hàng quá lâu dẫn đến đơn vị vận chuyển giao trễ 2 ngày. Tôi không còn nhu cầu sử dụng nữa nên muốn hoàn tiền.")
                                .status(com.be.common.enums.ComplaintStatus.REJECTED)
                                .severity(com.be.common.enums.ComplaintSeverity.MEDIUM)
                                .resolution("Từ chối khiếu nại. Qua xác minh hệ thống, đơn hàng vẫn được giao thành công trong vòng 3 ngày làm việc (đúng cam kết thời gian vận chuyển). Việc trễ hẹn cá nhân không thuộc chính sách hoàn trả hàng.")
                                .build()
                );
                complaintRepository.saveAll(complaints);
                list = complaintRepository.findAll();
            }
        }
        return list;
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
        Complaint saved = complaintRepository.save(complaint);
        
        if (saved.getReporter() != null) {
            try {
                java.util.Map<String, Object> eventData = new java.util.HashMap<>();
                eventData.put("complaintId", saved.getId());
                eventData.put("status", saved.getStatus().name());
                eventData.put("resolution", saved.getResolution());
                eventData.put("title", saved.getTitle() != null ? saved.getTitle() : "Khiếu nại sản phẩm/đơn hàng");
                eventData.put("updatedAt", java.time.LocalDateTime.now().toString());
                
                sseEmitterService.sendEvent(
                    "customer-notifications", 
                    saved.getReporter().getId().toString(), 
                    "complaint-processed", 
                    eventData
                );
            } catch (Exception e) {
                // Ignore SSE broadcast failure to not fail the update transaction
            }
        }
        
        return saved;
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> getSalesData(String period) {
        int days = 7;
        if ("month".equalsIgnoreCase(period)) {
            days = 30;
        } else if ("week".equalsIgnoreCase(period)) {
            days = 7;
        }
        
        java.time.LocalDateTime startDate = java.time.LocalDateTime.now().minusDays(days);
        java.util.List<com.be.entity.Order> orders = orderRepository.findOrdersSince(startDate);
        
        // Group by date (dd/MM)
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM");
        
        // Initialize map for all days in the range to ensure we have data points even for days with 0 sales
        java.util.Map<String, java.util.Map<String, Object>> dailyData = new java.util.LinkedHashMap<>();
        for (int i = days - 1; i >= 0; i--) {
            java.time.LocalDateTime d = java.time.LocalDateTime.now().minusDays(i);
            String label = d.format(formatter);
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("label", label);
            map.put("revenue", java.math.BigDecimal.ZERO);
            map.put("orders", 0L);
            dailyData.put(label, map);
        }
        
        // Populate with real DB data
        for (com.be.entity.Order order : orders) {
            if (order.getCreatedAt() != null) {
                String label = order.getCreatedAt().format(formatter);
                if (dailyData.containsKey(label)) {
                    java.util.Map<String, Object> map = dailyData.get(label);
                    java.math.BigDecimal currentRevenue = (java.math.BigDecimal) map.get("revenue");
                    java.math.BigDecimal orderTotal = order.getSubtotal() != null ? order.getSubtotal() : java.math.BigDecimal.ZERO;
                    map.put("revenue", currentRevenue.add(orderTotal));
                    map.put("orders", (Long) map.get("orders") + 1);
                }
            }
        }
        
        return new java.util.ArrayList<>(dailyData.values());
    }
}
