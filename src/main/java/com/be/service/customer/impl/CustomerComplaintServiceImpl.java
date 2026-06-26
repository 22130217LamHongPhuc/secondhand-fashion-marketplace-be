package com.be.service.customer.impl;

import com.be.common.enums.ComplaintSeverity;
import com.be.common.enums.ComplaintStatus;
import com.be.common.enums.ComplaintType;
import com.be.common.enums.OrderStatus;
import com.be.dto.request.customer.ComplaintRequest;
import com.be.entity.Complaint;
import com.be.entity.Order;
import com.be.entity.Shop;
import com.be.entity.User;
import com.be.repository.ComplaintRepository;
import com.be.repository.OrderRepository;
import com.be.repository.UserRepository;
import com.be.service.customer.CustomerComplaintService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerComplaintServiceImpl implements CustomerComplaintService {

    private final ComplaintRepository complaintRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Complaint createComplaint(Long customerId, ComplaintRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thông tin tài khoản người dùng với ID: " + customerId));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng với ID: " + request.getOrderId()));

        if (order.getCustomer() == null || !order.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("Không thể gửi khiếu nại vì đơn hàng này không thuộc về tài khoản của bạn.");
        }

        if (order.getStatus() != OrderStatus.DONE) {
            throw new IllegalArgumentException("Chỉ được khiếu nại những đơn hàng đã hoàn tất giao hàng thành công.");
        }

        if (complaintRepository.existsByOrderId(request.getOrderId())) {
            throw new IllegalArgumentException("Đơn hàng này đã được khiếu nại trước đó. Vui lòng kiểm tra lại trạng thái xử lý khiếu nại.");
        }

        Shop shop = order.getShop();
        if (shop == null) {
            throw new IllegalArgumentException("Không tìm thấy thông tin cửa hàng liên kết với đơn hàng này.");
        }

        Complaint complaint = Complaint.builder()
                .reporter(customer)
                .reportedShop(shop)
                .order(order)
                .type(ComplaintType.SHOP_COMPLAINT)
                .title(request.getTitle().trim())
                .content(request.getContent().trim())
                .status(ComplaintStatus.PENDING)
                .severity(ComplaintSeverity.MEDIUM)
                .build();

        return complaintRepository.save(complaint);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Complaint> getCustomerComplaints(Long customerId) {
        return complaintRepository.findByReporterId(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Complaint getComplaintDetail(Long customerId, Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khiếu nại với ID: " + complaintId));

        if (complaint.getReporter() == null || !complaint.getReporter().getId().equals(customerId)) {
            throw new IllegalArgumentException("Bạn không có quyền truy cập vào thông tin khiếu nại này.");
        }

        return complaint;
    }

    @Override
    @Transactional(readOnly = true)
    public Complaint getComplaintByOrder(Long customerId, Long orderId) {
        return complaintRepository.findByOrderId(orderId)
                .map(complaint -> {
                    if (complaint.getReporter() == null || !complaint.getReporter().getId().equals(customerId)) {
                        throw new IllegalArgumentException("Bạn không có quyền truy cập thông tin khiếu nại của đơn hàng này.");
                    }
                    return complaint;
                })
                .orElse(null);
    }
}
