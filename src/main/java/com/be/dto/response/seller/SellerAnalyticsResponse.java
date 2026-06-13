package com.be.dto.response.seller;

import java.util.List;

public record SellerAnalyticsResponse(
        ReputationSummary reputation,
        OperationalMetrics metrics,
        ReputationHistoryPage reputationHistory
) {
    public record ReputationSummary(
            Integer currentScore,
            Integer maxScore,
            String currentRank,
            String nextRank,
            Integer pointsNeededForNextRank,
            String statusMessage
    ) {}

    public record OperationalMetrics(
            MetricEntry successRate,
            MetricEntry averageRating,
            MetricEntry responseTime,
            MetricEntry returnRate
    ) {}

    public record MetricEntry(
            String value,
            String growth,
            String suffix,
            String status
    ) {}

    public record ReputationHistoryPage(
            List<ReputationHistoryEntry> content,
            PageableInfo pageable,
            Integer totalPages,
            Long totalElements,
            Boolean last
    ) {}

    public record ReputationHistoryEntry(
            String date,
            String event,
            String pointsChange,
            String pointsType,
            Integer totalAccumulated
    ) {}

    public record PageableInfo(
            Integer pageNumber,
            Integer pageSize
    ) {}
}
