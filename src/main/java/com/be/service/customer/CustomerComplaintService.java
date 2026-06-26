package com.be.service.customer;

import com.be.entity.Complaint;
import com.be.dto.request.customer.ComplaintRequest;

import java.util.List;

public interface CustomerComplaintService {

    Complaint createComplaint(Long customerId, ComplaintRequest request);

    List<Complaint> getCustomerComplaints(Long customerId);

    Complaint getComplaintDetail(Long customerId, Long complaintId);

    Complaint getComplaintByOrder(Long customerId, Long orderId);
}
