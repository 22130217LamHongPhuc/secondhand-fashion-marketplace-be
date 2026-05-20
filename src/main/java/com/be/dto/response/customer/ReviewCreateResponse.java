package com.be.dto.response.customer;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewCreateResponse(
		Long id,
		Long orderId,
		Long productId,
		Byte rating,
		String comment,
		Long reviewerId,
		String reviewerName,
		String reviewerAvatarUrl,
		List<String> imageUrls,
		LocalDateTime createdAt
) {
}

