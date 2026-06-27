package com.be.dto.request.seller;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PromotionUpdateDTO {
    private String name;
    private String description;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderValue;
    private Integer minOrderItems;
    private Integer quantity;
    private LocalDateTime endDate;
}
