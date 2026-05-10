package com.be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review_images", indexes = {
    @Index(name = "idx_review_images_review", columnList = "review_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewImage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String url;
}

