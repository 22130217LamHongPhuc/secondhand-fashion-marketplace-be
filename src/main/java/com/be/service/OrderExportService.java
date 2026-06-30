package com.be.service;

public interface OrderExportService {
    /**
     * Xuất danh sách đơn hàng ra file Excel (thực thi bất đồng bộ)
     * và upload lên Cloudflare R2
     *
     * @param shopId       ID của shop cần xuất đơn hàng
     * @param subscriberId ID của user (để gửi thông báo qua SSE)
     */
    void exportOrdersAsync(Long shopId, String subscriberId);
}
