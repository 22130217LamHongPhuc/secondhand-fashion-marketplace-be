package com.be.repository.specification;

import com.be.entity.Product;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> buildFilter(Long shopId, String keyword, Boolean isActive,
                                                     LocalDateTime fromDate, LocalDateTime toDate,
                                                     BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Required: shop ownership
            if (shopId != null) {
                predicates.add(cb.equal(root.get("shop").get("id"), shopId));
            }

            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + keyword.trim().toLowerCase() + "%"));
            }

            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
            }

            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDate));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        cb.coalesce(root.get("salePrice"), root.get("basePrice")), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        cb.coalesce(root.get("salePrice"), root.get("basePrice")), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
