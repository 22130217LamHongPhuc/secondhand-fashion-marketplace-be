package com.be.repository.specification;

import com.be.common.enums.OrderStatus;
import com.be.entity.Order;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderSpecification {

    public static Specification<Order> buildFilter(Long shopId, OrderStatus status, String orderCode,
                                                   LocalDateTime fromDate, LocalDateTime toDate,
                                                   BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Fetch customer and shop to avoid N+1 issues (matches @EntityGraph or JOIN FETCH)
            if (Long.class != query.getResultType()) {
                root.fetch("customer", jakarta.persistence.criteria.JoinType.LEFT);
            }

            // Required: shop ownership
            if (shopId != null) {
                predicates.add(cb.equal(root.get("shop").get("id"), shopId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (orderCode != null && !orderCode.isBlank()) {
                predicates.add(cb.equal(root.get("orderCode"), orderCode.trim()));
            }

            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
            }

            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDate));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("subtotal"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("subtotal"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
