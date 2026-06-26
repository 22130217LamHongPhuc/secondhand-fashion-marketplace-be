package com.be.controller.customer;

import com.be.dto.request.customer.ComplaintRequest;
import com.be.dto.response.ApiResponse;
import com.be.entity.Complaint;
import com.be.entity.User;
import com.be.repository.UserRepository;
import com.be.security.JwtTokenProvider;
import com.be.service.customer.CustomerComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/customer/complaints")
@CrossOrigin(origins = "*")
public class CustomerComplaintController {

    private final CustomerComplaintService customerComplaintService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    private Long resolveCustomerId(Long customerId, String authHeader) {
        if (customerId != null) {
            return customerId;
        }
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtTokenProvider.validateToken(token)) {
                try {
                    String email = jwtTokenProvider.getEmailFromToken(token);
                    return userRepository.findByEmail(email)
                            .map(User::getId)
                            .orElse(null);
                } catch (Exception e) {
                }
            }
        }
        return null;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Complaint>> createComplaint(
            @RequestParam(required = false) Long customerId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Validated @RequestBody ComplaintRequest request
    ) {
        Long resolvedCustomerId = resolveCustomerId(customerId, authHeader);
        if (resolvedCustomerId == null) {
            return ResponseEntity.status(401).body(ApiResponse.success(null, "Yêu cầu đăng nhập để gửi khiếu nại."));
        }
        Complaint result = customerComplaintService.createComplaint(resolvedCustomerId, request);
        return ResponseEntity.ok(ApiResponse.success(result, "Gửi khiếu nại đơn hàng thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Complaint>>> getCustomerComplaints(
            @RequestParam(required = false) Long customerId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long resolvedCustomerId = resolveCustomerId(customerId, authHeader);
        if (resolvedCustomerId == null) {
            return ResponseEntity.status(401).body(ApiResponse.success(null, "Yêu cầu đăng nhập để xem danh sách khiếu nại."));
        }
        List<Complaint> result = customerComplaintService.getCustomerComplaints(resolvedCustomerId);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách khiếu nại thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Complaint>> getComplaintDetail(
            @PathVariable Long id,
            @RequestParam(required = false) Long customerId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long resolvedCustomerId = resolveCustomerId(customerId, authHeader);
        if (resolvedCustomerId == null) {
            return ResponseEntity.status(401).body(ApiResponse.success(null, "Yêu cầu đăng nhập để xem chi tiết khiếu nại."));
        }
        Complaint result = customerComplaintService.getComplaintDetail(resolvedCustomerId, id);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy thông tin chi tiết khiếu nại thành công"));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<Complaint>> getComplaintByOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) Long customerId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long resolvedCustomerId = resolveCustomerId(customerId, authHeader);
        if (resolvedCustomerId == null) {
            return ResponseEntity.status(401).body(ApiResponse.success(null, "Yêu cầu đăng nhập để xem thông tin khiếu nại của đơn hàng."));
        }
        Complaint result = customerComplaintService.getComplaintByOrder(resolvedCustomerId, orderId);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy khiếu nại theo đơn hàng thành công"));
    }
}
