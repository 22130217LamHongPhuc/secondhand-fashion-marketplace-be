package com.be.service.seller.impl;

import com.be.dto.response.seller.SellerAnalyticsResponse;
import com.be.dto.response.seller.SellerDashboardResponse;
import com.be.entity.Shop;
import com.be.entity.User;
import com.be.repository.SellerStatisticRepository;
import com.be.repository.ShopRepository;
import com.be.service.seller.SellerStatisticService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.be.repository.UserRepository;
import com.be.security.JwtTokenProvider;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    private static final String[] CATEGORY_COLORS = {"#c75c2e", "#d4724a", "#f5c9a8", "#e8e5de", "#4b5563"};

    @Override
    public SellerDashboardResponse getDashboardData(String revenuePeriod, LocalDate startDate, LocalDate endDate) {
        Shop shop = getCurrentSellerShop();

        Long resolvedShopId = shop.getId();

        // Summary
        BigDecimal totalRevenue = statisticRepository.getTotalRevenue(resolvedShopId);
        long pendingOrders = statisticRepository.countPendingOrders(resolvedShopId);
        long activeProducts = statisticRepository.countActiveProducts(resolvedShopId);
        long pendingProducts = statisticRepository.countPendingProducts(resolvedShopId);
        long totalProducts = activeProducts + pendingProducts;

        // Determine startDateTime, endDateTime, prevPeriodStart, prevPeriodEnd
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;
        LocalDateTime prevPeriodStart;
        LocalDateTime prevPeriodEnd;

        if (startDate != null && endDate != null) {
            startDateTime = startDate.atStartOfDay();
            endDateTime = endDate.plusDays(1).atStartOfDay();
            
            long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            prevPeriodStart = startDateTime.minusDays(days);
            prevPeriodEnd = startDateTime;
        } else {
            // Use revenuePeriod
            endDateTime = LocalDate.now().plusDays(1).atStartOfDay(); // exclusive end
            
            long days = 30; // default to 30 days
            if ("7_DAYS".equals(revenuePeriod)) {
                days = 7;
            } else if ("90_DAYS".equals(revenuePeriod)) {
                days = 90;
            }
            
            startDateTime = endDateTime.minusDays(days);
            prevPeriodStart = startDateTime.minusDays(days);
            prevPeriodEnd = startDateTime;
        }

        BigDecimal currentPeriodRevenue = statisticRepository.getRevenueByPeriod(resolvedShopId, startDateTime, endDateTime);
        BigDecimal prevPeriodRevenue = statisticRepository.getRevenueByPeriod(resolvedShopId, prevPeriodStart, prevPeriodEnd);

        Double growth = 0.0;
        if (prevPeriodRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growth = currentPeriodRevenue.subtract(prevPeriodRevenue)
                    .divide(prevPeriodRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue();
        } else if (currentPeriodRevenue.compareTo(BigDecimal.ZERO) > 0) {
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

        // Trend - Calculate dynamically based on daily revenue of the last 7 days
        List<BigDecimal> trend = new ArrayList<>();
        BigDecimal maxDailyRevenue = BigDecimal.ZERO;
        List<BigDecimal> dailyRevenues = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime startOfDay = LocalDate.now().minusDays(i).atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            BigDecimal dayRevenue = statisticRepository.getRevenueByPeriod(resolvedShopId, startOfDay, endOfDay);
            dailyRevenues.add(dayRevenue);
            if (dayRevenue.compareTo(maxDailyRevenue) > 0) {
                maxDailyRevenue = dayRevenue;
            }
        }
        for (BigDecimal dayRevenue : dailyRevenues) {
            if (maxDailyRevenue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percent = dayRevenue.multiply(BigDecimal.valueOf(90))
                        .divide(maxDailyRevenue, 2, RoundingMode.HALF_UP)
                        .add(BigDecimal.valueOf(10));
                trend.add(percent);
            } else {
                trend.add(BigDecimal.valueOf(10));
            }
        }

        SellerDashboardResponse.DashboardSummary summary = new SellerDashboardResponse.DashboardSummary(
                totalRevenue, growth, trend, pendingOrders, avatars, extraOrdersCount, totalProducts, activeProducts, pendingProducts
        );

        // Revenue Chart (4 segments/weeks)
        long totalSeconds = ChronoUnit.SECONDS.between(startDateTime, endDateTime);
        long segmentSeconds = totalSeconds / 4;

        BigDecimal currentSeg1 = statisticRepository.getRevenueByPeriod(resolvedShopId, startDateTime, startDateTime.plusSeconds(segmentSeconds));
        BigDecimal currentSeg2 = statisticRepository.getRevenueByPeriod(resolvedShopId, startDateTime.plusSeconds(segmentSeconds), startDateTime.plusSeconds(2 * segmentSeconds));
        BigDecimal currentSeg3 = statisticRepository.getRevenueByPeriod(resolvedShopId, startDateTime.plusSeconds(2 * segmentSeconds), startDateTime.plusSeconds(3 * segmentSeconds));
        BigDecimal currentSeg4 = statisticRepository.getRevenueByPeriod(resolvedShopId, startDateTime.plusSeconds(3 * segmentSeconds), endDateTime);

        BigDecimal prevSeg1 = statisticRepository.getRevenueByPeriod(resolvedShopId, prevPeriodStart, prevPeriodStart.plusSeconds(segmentSeconds));
        BigDecimal prevSeg2 = statisticRepository.getRevenueByPeriod(resolvedShopId, prevPeriodStart.plusSeconds(segmentSeconds), prevPeriodStart.plusSeconds(2 * segmentSeconds));
        BigDecimal prevSeg3 = statisticRepository.getRevenueByPeriod(resolvedShopId, prevPeriodStart.plusSeconds(2 * segmentSeconds), prevPeriodStart.plusSeconds(3 * segmentSeconds));
        BigDecimal prevSeg4 = statisticRepository.getRevenueByPeriod(resolvedShopId, prevPeriodStart.plusSeconds(3 * segmentSeconds), prevPeriodEnd);

        List<SellerDashboardResponse.RevenueChartEntry> chart = List.of(
                new SellerDashboardResponse.RevenueChartEntry("TUẦN 1", prevSeg1, currentSeg1),
                new SellerDashboardResponse.RevenueChartEntry("TUẦN 2", prevSeg2, currentSeg2),
                new SellerDashboardResponse.RevenueChartEntry("TUẦN 3", prevSeg3, currentSeg3),
                new SellerDashboardResponse.RevenueChartEntry("TUẦN 4", prevSeg4, currentSeg4)
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
    public SellerAnalyticsResponse getAnalyticsData( int page, int size) {
        Shop shop = getCurrentSellerShop();

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

    private Shop getCurrentSellerShop() {
        User user = null;
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtTokenProvider.validateToken(token)) {
                    String email = jwtTokenProvider.getEmailFromToken(token);
                    user = userRepository.findByEmail(email).orElse(null);
                }
            }
        } catch (Exception e) {
            // RequestContext not active or token invalid
        }

        if (user != null) {
            final User finalUser = user;
            return shopRepository.findBySellerId(finalUser.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Shop not found for user: " + finalUser.getEmail()));
        }

        return shopRepository.findBySellerId(2L)
                .orElseThrow(() -> new EntityNotFoundException("Shop not found for current seller"));
    }
}
