package com.be.service.seller.impl;

import com.be.dto.response.seller.SellerAnalyticsResponse;
import com.be.dto.response.seller.SellerDashboardResponse;
import com.be.entity.Shop;
import com.be.entity.User;
import com.be.repository.SellerStatisticRepository;
import com.be.repository.ShopRepository;
import com.be.service.seller.SellerStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerStatisticServiceImpl implements SellerStatisticService {
    private final SellerStatisticRepository statisticRepository;
    private final ShopRepository shopRepository;

    private static final String[] CATEGORY_COLORS = {"#c75c2e", "#d4724a", "#f5c9a8", "#e8e5de", "#4b5563"};

    @Override
    public SellerDashboardResponse getDashboardData(Long shopId, String revenuePeriod) {
        Shop shop = resolveShop(shopId);
        if (shop == null) return null;

        Long resolvedShopId = shop.getId();

        // Summary
        BigDecimal totalRevenue = statisticRepository.getTotalRevenue(resolvedShopId);
        long pendingOrders = statisticRepository.countPendingOrders(resolvedShopId);
        long activeProducts = statisticRepository.countActiveProducts(resolvedShopId);
        long pendingProducts = statisticRepository.countPendingProducts(resolvedShopId);
        long totalProducts = activeProducts + pendingProducts;

        // Revenue Growth
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfCurrentMonth = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime startOfPreviousMonth = startOfCurrentMonth.minusMonths(1);

        BigDecimal currentMonthRevenue = statisticRepository.getRevenueByPeriod(resolvedShopId, startOfCurrentMonth, now);
        BigDecimal prevMonthRevenue = statisticRepository.getRevenueByPeriod(resolvedShopId, startOfPreviousMonth, startOfCurrentMonth);

        Double growth = 0.0;
        if (prevMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growth = currentMonthRevenue.subtract(prevMonthRevenue)
                    .divide(prevMonthRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue();
        } else if (currentMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growth = 100.0;
        }

        // Recent avatars
        List<SellerStatisticRepository.IRecentOrderProjection> recentPendingOrders = statisticRepository.getRecentPendingOrders(resolvedShopId);
        List<String> avatars = recentPendingOrders.stream()
                .map(SellerStatisticRepository.IRecentOrderProjection::getAvatarUrl)
                .filter(url -> url != null && !url.isEmpty())
                .limit(3)
                .toList();
        long extraOrdersCount = Math.max(0, pendingOrders - avatars.size());

        // Trend - Mock with typical ascending numbers for visualization
        List<BigDecimal> trend = List.of(
                BigDecimal.valueOf(35), BigDecimal.valueOf(38), BigDecimal.valueOf(42),
                BigDecimal.valueOf(48), BigDecimal.valueOf(80), BigDecimal.valueOf(50), BigDecimal.valueOf(55)
        );

        SellerDashboardResponse.DashboardSummary summary = new SellerDashboardResponse.DashboardSummary(
                totalRevenue, growth, trend, pendingOrders, avatars, extraOrdersCount, totalProducts, activeProducts, pendingProducts
        );

        // Revenue Chart (4 weeks)
        SellerStatisticRepository.IWeeklyRevenueProjection currentWeeks = statisticRepository.getWeeklyRevenue(resolvedShopId, startOfCurrentMonth, startOfCurrentMonth.plusMonths(1));
        SellerStatisticRepository.IWeeklyRevenueProjection prevWeeks = statisticRepository.getWeeklyRevenue(resolvedShopId, startOfPreviousMonth, startOfCurrentMonth);

        List<SellerDashboardResponse.RevenueChartEntry> chart = List.of(
                new SellerDashboardResponse.RevenueChartEntry("TUẦN 1", prevWeeks.getWeek1(), currentWeeks.getWeek1()),
                new SellerDashboardResponse.RevenueChartEntry("TUẦN 2", prevWeeks.getWeek2(), currentWeeks.getWeek2()),
                new SellerDashboardResponse.RevenueChartEntry("TUẦN 3", prevWeeks.getWeek3(), currentWeeks.getWeek3()),
                new SellerDashboardResponse.RevenueChartEntry("TUẦN 4", prevWeeks.getWeek4(), currentWeeks.getWeek4())
        );

        // Category Breakdown
        List<SellerStatisticRepository.ICategoryDistributionProjection> categoryProjections = statisticRepository.getCategoryDistribution(resolvedShopId);
        BigDecimal sumCategories = categoryProjections.stream().map(SellerStatisticRepository.ICategoryDistributionProjection::getTotalSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<SellerDashboardResponse.CategoryBreakdownEntry> breakdown = new ArrayList<>();
        int colorIdx = 0;
        for (SellerStatisticRepository.ICategoryDistributionProjection cat : categoryProjections) {
            int percent = sumCategories.compareTo(BigDecimal.ZERO) > 0 
                ? cat.getTotalSubtotal().multiply(new BigDecimal("100")).divide(sumCategories, RoundingMode.HALF_UP).intValue() 
                : 0;
            breakdown.add(new SellerDashboardResponse.CategoryBreakdownEntry(
                    cat.getCategoryName(), percent, CATEGORY_COLORS[colorIdx % CATEGORY_COLORS.length]
            ));
            colorIdx++;
        }

        // Recent Notifications
        List<SellerDashboardResponse.NotificationEntry> notifications = new ArrayList<>();
        long notifId = 1;
        for (int i = 0; i < Math.min(recentPendingOrders.size(), 3); i++) {
            var order = recentPendingOrders.get(i);
            String title = "Đơn hàng #" + order.getOrderCode() + " mới!";
            String desc = order.getCustomerName() + " vừa đặt \"" + order.getFirstProductName() + "\". Cần xác nhận ngay.";
            long mins = Math.max(1, ChronoUnit.MINUTES.between(order.getCreatedAt(), now));
            String timeStr = mins < 60 ? mins + " phút trước" : (mins / 60) + " giờ trước";
            notifications.add(new SellerDashboardResponse.NotificationEntry(notifId++, "ORDER", title, desc, timeStr));
        }
        if (notifications.isEmpty()) {
            notifications.add(new SellerDashboardResponse.NotificationEntry(1L, "MESSAGE", "Chào mừng!", "Hãy hoàn thành thông tin shop để bắt đầu bán hàng.", "1 phút trước"));
        }

        return new SellerDashboardResponse(summary, chart, breakdown, notifications);
    }

    @Override
    public SellerAnalyticsResponse getAnalyticsData(Long shopId, int page, int size) {
        Shop shop = resolveShop(shopId);
        if (shop == null) return null;

        Long resolvedShopId = shop.getId();
        Long sellerId = shop.getSeller().getId();

        // Fetch logs
        List<SellerStatisticRepository.IReputationLogProjection> logs = statisticRepository.getReputationLogs(resolvedShopId, sellerId);

        int score = 1000;
        int doneCount = 0;
        int cancelledCount = 0;
        List<SellerAnalyticsResponse.ReputationHistoryEntry> history = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (var log : logs) {
            boolean isDone = "DONE".equals(log.getStatus());
            int pointsChange = isDone ? 10 : -15;
            score += pointsChange;
            if (isDone) doneCount++; else cancelledCount++;

            String event = isDone ? "Hoàn thành đơn #" + log.getOrderCode() 
                                  : "Hủy đơn #" + log.getOrderCode() + (log.getCancelReason() != null ? " - " + log.getCancelReason() : "");
            
            history.add(new SellerAnalyticsResponse.ReputationHistoryEntry(
                    log.getCreatedAt().format(dateFormatter),
                    event,
                    (pointsChange > 0 ? "+" : "") + pointsChange,
                    pointsChange > 0 ? "positive" : "negative",
                    score
            ));
        }

        // Metrics
        String successRateVal = "100.0%";
        if (doneCount + cancelledCount > 0) {
            double rate = (doneCount * 100.0) / (doneCount + cancelledCount);
            successRateVal = String.format("%.1f%%", rate).replace(",", ".");
        }

        SellerAnalyticsResponse.MetricEntry successRate = new SellerAnalyticsResponse.MetricEntry(successRateVal, "+0.0%", "", "GOOD");
        SellerAnalyticsResponse.MetricEntry avgRating = new SellerAnalyticsResponse.MetricEntry(shop.getRatingAvg().toString(), "", "/ 5.0", shop.getRatingAvg().doubleValue() >= 4.5 ? "EXCELLENT" : "GOOD");
        SellerAnalyticsResponse.MetricEntry responseTime = new SellerAnalyticsResponse.MetricEntry("< 15", "", "phút", "IMPROVED");
        SellerAnalyticsResponse.MetricEntry returnRate = new SellerAnalyticsResponse.MetricEntry("1.2%", "", "", "LOW");

        SellerAnalyticsResponse.OperationalMetrics metrics = new SellerAnalyticsResponse.OperationalMetrics(successRate, avgRating, responseTime, returnRate);

        // Reputation summary
        int maxScore = 2000;
        String currentRank, nextRank;
        int pointsNeeded = 0;
        String statusMessage;
        if (score >= 1500) {
            currentRank = "HẠNG VÀNG";
            nextRank = "TỐI ĐA";
            statusMessage = "Xuất sắc! Cửa hàng đạt chuẩn uy tín vàng.";
        } else if (score >= 1200) {
            currentRank = "HẠNG BẠC";
            nextRank = "HẠNG VÀNG";
            pointsNeeded = 1500 - score;
            statusMessage = "Tuyệt vời! Cửa hàng đang hoạt động rất tốt.";
        } else {
            currentRank = "HẠNG ĐỒNG";
            nextRank = "HẠNG BẠC";
            pointsNeeded = 1200 - score;
            statusMessage = "Cần cải thiện! Hãy hoàn thành nhiều đơn hàng hơn và hạn chế hủy đơn.";
        }

        SellerAnalyticsResponse.ReputationSummary repSummary = new SellerAnalyticsResponse.ReputationSummary(
                score, maxScore, currentRank, nextRank, pointsNeeded, statusMessage
        );

        // Pagination for history
        Collections.reverse(history);
        int totalElements = history.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        if (totalPages == 0) totalPages = 1;
        int start = Math.min(page * size, totalElements);
        int end = Math.min(start + size, totalElements);
        List<SellerAnalyticsResponse.ReputationHistoryEntry> content = history.subList(start, end);

        SellerAnalyticsResponse.ReputationHistoryPage pageData = new SellerAnalyticsResponse.ReputationHistoryPage(
                content,
                new SellerAnalyticsResponse.PageableInfo(page, size),
                totalPages,
                (long) totalElements,
                page >= totalPages - 1
        );

        return new SellerAnalyticsResponse(repSummary, metrics, pageData);
    }

    private Shop resolveShop(Long shopId) {
        if (shopId != null) {
            return shopRepository.findById(shopId).orElse(null);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return shopRepository.findBySellerId(user.getId()).orElse(null);
        }

        // Fallback for permitAll or anonymous access
        return shopRepository.findAll().stream().findFirst().orElse(null);
    }
}
