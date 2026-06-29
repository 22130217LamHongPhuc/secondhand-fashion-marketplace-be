package com.be.repository.specification;

import com.be.entity.Promotion;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PromotionSpecification {

    public static Specification<Promotion> buildFilter(Long shopId, String keyword,
                                                       LocalDateTime fromDate, LocalDateTime toDate,
                                                       BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Required: shop ownership
            if (shopId != null) {
                predicates.add(cb.equal(root.get("shop").get("id"), shopId));
            }

            if (keyword != null && !keyword.isBlank()) {
                String searchKeyword = "%" + keyword.trim().toLowerCase() + "%";
                Predicate codeLike = cb.like(cb.lower(root.get("code")), searchKeyword);
                Predicate nameLike = cb.like(cb.lower(root.get("name")), searchKeyword);
                predicates.add(cb.or(codeLike, nameLike));
            }

            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
            }

            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDate));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("discountValue"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("discountValue"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
