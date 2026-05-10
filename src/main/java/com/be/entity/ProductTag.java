package com.be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_tags", indexes = {
    @Index(name = "idx_product_tags_product", columnList = "product_id"),
    @Index(name = "idx_product_tags_tag", columnList = "tag")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductTag {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 100)
    private String tag;
}

