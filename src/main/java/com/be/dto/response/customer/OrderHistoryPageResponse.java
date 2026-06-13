package com.be.dto.response.customer;

import java.util.List;

public record OrderHistoryPageResponse(
                List<OrderHistoryItemResponse> orders,
                int page,
                int size,
                long totalElements,
                int totalPages,
                boolean hasNext,
                boolean hasPrevious) {
}
