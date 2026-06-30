package com.be.service.impl;

import com.be.entity.Product;
import com.be.repository.ProductRepository;
import com.be.service.ProductExportService;
import com.be.service.S3Service;
import com.be.service.SseEmitterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Slf4j
public class ProductExportServiceImpl implements ProductExportService {

    private final ProductRepository productRepository;
    private final S3Service s3Service;
    private final SseEmitterService sseEmitterService;
    private final ExecutorService executorService;
    private final String cloudflareDomain;
    private final TransactionTemplate transactionTemplate;

    public ProductExportServiceImpl(
            ProductRepository productRepository,
            S3Service s3Service,
            SseEmitterService sseEmitterService,
            @Qualifier("exportExecutorService") ExecutorService executorService,
            @Value("${cloudflare.r2.domain}") String cloudflareDomain,
            TransactionTemplate transactionTemplate) {
        this.productRepository = productRepository;
        this.s3Service = s3Service;
        this.sseEmitterService = sseEmitterService;
        this.executorService = executorService;
        this.cloudflareDomain = cloudflareDomain;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void exportProductsAsync(Long shopId, String subscriberId) {
        executorService.submit(() -> processExport(shopId, subscriberId));
    }

    private void processExport(Long shopId, String subscriberId) {
        log.info("[Export] Starting product export for shopId: {}, subscriberId: {}", shopId, subscriberId);
        String channel = "product-export";
        File tempFile = null;
        SXSSFWorkbook workbook = null;

        try {
            long totalRowsLong = productRepository.countByShopId(shopId);
            int totalRows = (int) totalRowsLong;

            if (totalRows == 0) {
                sendError(channel, subscriberId, "Không có sản phẩm nào để xuất.");
                return;
            }

            tempFile = File.createTempFile("products_export_shop_" + shopId + "_", ".xlsx");
            workbook = new SXSSFWorkbook(100); // Giữ 100 dòng trong RAM, sau đó xả xuống đĩa

            Sheet sheet = workbook.createSheet("Products");
            String[] headers = {
                    "Tên sản phẩm", "Giá gốc", "Giá bán", "Tồn kho", "Tình trạng",
                    "Thương hiệu", "Xuất xứ", "Trạng thái", "Đánh giá TB",
                    "Tổng review", "Khối lượng (g)", "Kích thước (cm)", "Ngày tạo"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            long startTime = System.currentTimeMillis();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // Define array to hold current row index inside lambda
            final int[] currentIndex = {0};

            // Thực thi Stream trong một Transaction (Read-Only)
            transactionTemplate.setReadOnly(true);
            transactionTemplate.executeWithoutResult(status -> {
                try (Stream<Product> stream = productRepository.streamAllByShopIdOrderByCreatedAtDesc(shopId)) {
                    stream.forEach(p -> {
                        int i = currentIndex[0];
                        Row row = sheet.createRow(i + 1);

                        row.createCell(0).setCellValue(p.getName() != null ? p.getName() : "");
                        row.createCell(1).setCellValue(formatPrice(p.getBasePrice()));
                        row.createCell(2).setCellValue(formatPrice(p.getSalePrice()));
                        row.createCell(3).setCellValue(p.getStockQuantity() != null ? p.getStockQuantity() : 0);
                        row.createCell(4).setCellValue(p.getCondition() != null ? p.getCondition().name() : "");
                        row.createCell(5).setCellValue(p.getBrand() != null ? p.getBrand() : "");
                        row.createCell(6).setCellValue(p.getOriginCountry() != null ? p.getOriginCountry() : "");
                        row.createCell(7).setCellValue(Boolean.TRUE.equals(p.getIsActive()) ? "Đang bán" : "Đã ẩn");
                        row.createCell(8).setCellValue(p.getRatingAvg() != null ? p.getRatingAvg().doubleValue() : 0.0);
                        row.createCell(9).setCellValue(p.getTotalReviews() != null ? p.getTotalReviews() : 0);
                        row.createCell(10).setCellValue(p.getWeight() != null ? p.getWeight() : 0);
                        
                        String dimension = String.format("%dx%dx%d", 
                                p.getLength() != null ? p.getLength() : 0, 
                                p.getWidth() != null ? p.getWidth() : 0, 
                                p.getHeight() != null ? p.getHeight() : 0);
                        row.createCell(11).setCellValue(dimension);
                        
                        row.createCell(12).setCellValue(p.getCreatedAt() != null ? p.getCreatedAt().format(formatter) : "");

                        // Report progress every 50 rows or at the end
                        if ((i + 1) % 50 == 0 || (i + 1) == totalRows) {
                            sendProgress(channel, subscriberId, i + 1, totalRows, startTime);
                        }
                        
                        currentIndex[0]++;
                    });
                }
            });

            // Ghi dữ liệu từ workbook xuống temp file
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                workbook.write(fos);
            }
            workbook.dispose(); // Xóa các file tạm của SXSSFWorkbook

            // Upload lên Cloudflare
            String fileName = "exports/shop_" + shopId + "/" + tempFile.getName();
            s3Service.uploadFileStream(tempFile, fileName, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            // Build download URL
            String domain = cloudflareDomain.endsWith("/") ? cloudflareDomain : cloudflareDomain + "/";
            String downloadUrl = domain + fileName;
            long fileSizeBytes = tempFile.length();
            
            // Thông báo hoàn thành
            Map<String, Object> completeData = new HashMap<>();
            completeData.put("downloadUrl", downloadUrl);
            completeData.put("fileName", tempFile.getName());
            completeData.put("totalRows", totalRows);
            completeData.put("fileSizeKB", fileSizeBytes / 1024);
            
            sseEmitterService.sendEvent(channel, subscriberId, "export-complete", completeData);
            log.info("[Export] Successfully exported {} rows for shopId: {}. File uploaded to R2.", totalRows, shopId);

        } catch (Exception e) {
            log.error("[Export] Error during export for shopId: {}", shopId, e);
            sendError(channel, subscriberId, "Lỗi tạo file Excel: " + e.getMessage());
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception ignored) {}
            }
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                log.debug("[Export] Temp file deleted: {}", deleted);
            }
        }
    }

    private void sendProgress(String channel, String subscriberId, int currentRow, int totalRows, long startTime) {
        int percent = (int) ((currentRow * 100.0f) / totalRows);
        long elapsedTime = System.currentTimeMillis() - startTime;
        long estimatedTotalTime = (long) ((elapsedTime / (double) currentRow) * totalRows);
        long estimatedTimeLeftMs = Math.max(0, estimatedTotalTime - elapsedTime);
        int estimatedSecondsLeft = (int) (estimatedTimeLeftMs / 1000);

        Map<String, Object> progressData = new HashMap<>();
        progressData.put("percent", percent);
        progressData.put("currentRow", currentRow);
        progressData.put("totalRows", totalRows);
        progressData.put("estimatedSecondsLeft", estimatedSecondsLeft);

        sseEmitterService.sendEvent(channel, subscriberId, "export-progress", progressData);
    }

    private void sendError(String channel, String subscriberId, String message) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("message", message);
        sseEmitterService.sendEvent(channel, subscriberId, "export-error", errorData);
    }

    private double formatPrice(BigDecimal price) {
        return price != null ? price.doubleValue() : 0.0;
    }
}
