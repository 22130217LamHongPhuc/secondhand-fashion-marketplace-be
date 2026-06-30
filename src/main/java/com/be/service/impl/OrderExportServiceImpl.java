package com.be.service.impl;

import com.be.entity.Order;
import com.be.repository.OrderRepository;
import com.be.service.OrderExportService;
import com.be.service.S3Service;
import com.be.service.SseEmitterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

@Service
@Slf4j
public class OrderExportServiceImpl implements OrderExportService {

    private final OrderRepository orderRepository;
    private final S3Service s3Service;
    private final SseEmitterService sseEmitterService;
    private final ExecutorService executorService;
    private final String cloudflareDomain;
    private final TransactionTemplate transactionTemplate;

    public OrderExportServiceImpl(
            OrderRepository orderRepository,
            S3Service s3Service,
            SseEmitterService sseEmitterService,
            @Qualifier("exportExecutorService") ExecutorService executorService,
            @Value("${cloudflare.r2.domain}") String cloudflareDomain,
            TransactionTemplate transactionTemplate) {
        this.orderRepository = orderRepository;
        this.s3Service = s3Service;
        this.sseEmitterService = sseEmitterService;
        this.executorService = executorService;
        this.cloudflareDomain = cloudflareDomain;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void exportOrdersAsync(Long shopId, String subscriberId) {
        executorService.submit(() -> {
            processExport(shopId, subscriberId);
        });
    }

    private void processExport(Long shopId, String subscriberId) {
        log.info("[Export] Starting order export for shopId: {}, subscriberId: {}", shopId, subscriberId);
        String channel = "order-export";
        File tempFile = null;
        SXSSFWorkbook workbook = null;

        try {
            long totalRowsLong = orderRepository.countByShopId(shopId);
            int totalRows = (int) totalRowsLong;

            if (totalRows == 0) {
                sendError(channel, subscriberId, "Không có đơn hàng nào để xuất.");
                return;
            }

            File exportDir = new File("export_temp");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            } else {
                File[] oldFiles = exportDir.listFiles((dir, name) -> name.startsWith("orders_export_shop_" + shopId + "_"));
                if (oldFiles != null) {
                    for (File f : oldFiles) {
                        f.delete();
                    }
                }
            }
            tempFile = new File(exportDir, "orders_export_shop_" + shopId + "_" + System.currentTimeMillis() + ".xlsx");
            workbook = new SXSSFWorkbook(100);

            Sheet sheet = workbook.createSheet("Orders");
            String[] headers = {
                    "Mã đơn hàng", "Khách hàng", "Tổng tiền hàng", "Phí vận chuyển", "Tiền giảm giá",
                    "Thanh toán", "Hình thức TT", "Trạng thái", "Mã vận đơn", "Phí GHN", "Thời gian tạo"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            long startTime = System.currentTimeMillis();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            final int[] currentIndex = {0};

            transactionTemplate.setReadOnly(true);
            transactionTemplate.executeWithoutResult(status -> {
                try (Stream<Order> stream = orderRepository.streamAllByShopIdOrderByCreatedAtDesc(shopId)) {
                    stream.forEach(o -> {
                        int i = currentIndex[0];
                        Row row = sheet.createRow(i + 1);

                        row.createCell(0).setCellValue(o.getOrderCode() != null ? o.getOrderCode() : "");
                        row.createCell(1).setCellValue(o.getCustomer() != null ? o.getCustomer().getFullName() : "N/A");
                        row.createCell(2).setCellValue(formatPrice(o.getSubtotal()));
                        row.createCell(3).setCellValue(formatPrice(o.getShippingFee()));
                        row.createCell(4).setCellValue(formatPrice(o.getDiscountAmount()));
                        row.createCell(5).setCellValue(o.getPaymentStatus() != null ? o.getPaymentStatus().name() : "");
                        row.createCell(6).setCellValue(o.getPaymentMethod() != null ? o.getPaymentMethod().name() : "");
                        row.createCell(7).setCellValue(o.getStatus() != null ? o.getStatus().name() : "");
                        row.createCell(8).setCellValue(o.getGhnOrderCode() != null ? o.getGhnOrderCode() : "");
                        row.createCell(9).setCellValue(formatPrice(o.getGhnTotalFee()));
                        row.createCell(10).setCellValue(o.getCreatedAt() != null ? o.getCreatedAt().format(formatter) : "");

                        if ((i + 1) % 50 == 0 || (i + 1) == totalRows) {
                            sendProgress(channel, subscriberId, i + 1, totalRows, startTime);
                        }
                        
                        currentIndex[0]++;
                    });
                }
            });

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                workbook.write(fos);
            }
            workbook.dispose();

            String fileName = "exports/shop_" + shopId + "/" + tempFile.getName();
            s3Service.uploadFileStream(tempFile, fileName, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            String domain = cloudflareDomain.endsWith("/") ? cloudflareDomain : cloudflareDomain + "/";
            String downloadUrl = domain + fileName;
            long fileSizeBytes = tempFile.length();
            
            Map<String, Object> completeData = new HashMap<>();
            completeData.put("downloadUrl", downloadUrl);
            completeData.put("fileName", tempFile.getName());
            completeData.put("totalRows", totalRows);
            completeData.put("fileSizeKB", fileSizeBytes / 1024);
            
            sseEmitterService.sendEvent(channel, subscriberId, "export-complete", completeData);

            log.info("[Export] Order export completed successfully for shopId: {}", shopId);

        } catch (Exception e) {
            log.error("[Export] Error during order export for shopId: {}", shopId, e);
            sendError(channel, subscriberId, "Có lỗi xảy ra: " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    log.warn("Could not delete temporary file: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    private void sendProgress(String channel, String subscriberId, int processed, int total, long startTime) {
        int percent = (int) ((processed * 100.0) / total);
        long elapsedMs = System.currentTimeMillis() - startTime;
        int etaSeconds = 0;
        
        if (processed > 0 && processed < total) {
            double msPerRow = (double) elapsedMs / processed;
            long remainingRows = total - processed;
            etaSeconds = (int) ((remainingRows * msPerRow) / 1000);
        }

        Map<String, Object> progressData = new HashMap<>();
        progressData.put("percent", percent);
        progressData.put("processed", processed);
        progressData.put("total", total);
        progressData.put("etaSeconds", etaSeconds);

        sseEmitterService.sendEvent(channel, subscriberId, "export-progress", progressData);
    }

    private void sendError(String channel, String subscriberId, String errorMessage) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("message", errorMessage);
        sseEmitterService.sendEvent(channel, subscriberId, "export-error", errorData);
    }

    private double formatPrice(BigDecimal price) {
        if (price == null) return 0.0;
        return price.doubleValue();
    }
}
