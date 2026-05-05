package com.be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_attributes", indexes = {
    @Index(name = "idx_product_attributes_product", columnList = "product_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAttribute {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "attr_key", nullable = false, length = 100)
    private String attrKey;

    @Column(name = "attr_value", nullable = false, length = 255)
    private String attrValue;
}

