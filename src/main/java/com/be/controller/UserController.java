package com.be.controller;

import com.be.dto.request.UpdateProfileRequest;
import com.be.dto.response.ApiResponse;
import com.be.dto.response.UserResponse;
import com.be.entity.User;
import com.be.entity.UserAddress;
import com.be.repository.UserRepository;
import com.be.repository.UserAddressRepository;
import com.be.service.GhnShippingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final GhnShippingService ghnShippingService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + id));
        return ResponseEntity.ok(ApiResponse.success(UserResponse.fromEntity(user), "Lấy thông tin tài khoản thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @PathVariable Long id,
            @Validated @RequestBody UpdateProfileRequest request
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + id));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setAvatarUrl(request.getAvatarUrl());

        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(UserResponse.fromEntity(savedUser), "Cập nhật thông tin tài khoản thành công"));
    }

    @GetMapping("/{id}/addresses")
    public ResponseEntity<ApiResponse<List<UserAddress>>> getUserAddresses(@PathVariable Long id) {
        List<UserAddress> addresses = userAddressRepository.findByUserId(id);
        return ResponseEntity.ok(ApiResponse.success(addresses, "Lấy danh sách địa chỉ thành công"));
    }

    @PostMapping("/{id}/addresses")
    public ResponseEntity<ApiResponse<UserAddress>> createUserAddress(
            @PathVariable Long id,
            @Validated @RequestBody UserAddress request
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + id));
        
        request.setUser(user);
        ghnShippingService.enrichAddressCodes(request);
        
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            List<UserAddress> existing = userAddressRepository.findByUserId(id);
            for (UserAddress addr : existing) {
                if (Boolean.TRUE.equals(addr.getIsDefault())) {
                    addr.setIsDefault(false);
                    userAddressRepository.save(addr);
                }
            }
        } else {
            List<UserAddress> existing = userAddressRepository.findByUserId(id);
            if (existing.isEmpty()) {
                request.setIsDefault(true);
            }
        }
        
        UserAddress saved = userAddressRepository.save(request);
        return ResponseEntity.ok(ApiResponse.success(saved, "Thêm địa chỉ thành công"));
    }
}
