package com.be.repository;

import com.be.entity.Complaint;
import com.be.common.enums.ComplaintStatus;
import com.be.common.enums.ComplaintType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByStatus(ComplaintStatus status);
    List<Complaint> findByType(ComplaintType type);
    List<Complaint> findByReportedShopId(Long shopId);
}
