package com.be.security;

import com.be.entity.Shop;
import com.be.entity.User;
import com.be.repository.ShopRepository;
import com.be.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthHelper {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("Unauthorized: No authenticated user found in SecurityContext");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }

        throw new RuntimeException("Unauthorized: Principal is not of type User");
    }

    /**
     * Lấy thông tin Shop của Seller hiện tại từ JWT Token.
     * @return Shop
     * @throws RuntimeException nếu không có token hợp lệ
     * @throws EntityNotFoundException nếu User không tồn tại hoặc không có Shop
     */
    public Shop getCurrentSellerShop() {
        User user = getCurrentUser();

        return shopRepository.findBySellerId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Shop not found for user: " + user.getEmail()));
    }
    
    /**
     * Lấy thông tin Shop dựa vào currentUser (Dùng trong các service đã lấy sẵn User)
     * @param currentUser User hiện tại
     * @return Shop
     * @throws EntityNotFoundException nếu không tìm thấy Shop
     */
    public Shop getCurrentSellerShop(User currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return shopRepository.findBySellerId(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Shop not found for user: " + currentUser.getEmail()));
    }
}
