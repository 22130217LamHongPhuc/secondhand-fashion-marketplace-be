package com.be.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Service generic quản lý Server-Sent Events (SSE).
 * Hỗ trợ nhiều channel và subscriber khác nhau (seller, customer, admin,...).
 * Mỗi subscriber+channel chỉ được phép giữ 1 connection duy nhất.
 */
public interface SseEmitterService {
    
    /**
     * Đăng ký subscriber vào channel.
     * Nếu subscriber đã có connection cũ đang chạy trong channel này,
     * hệ thống sẽ tự động đóng connection cũ và thay bằng connection mới.
     *
     * @param channel      Tên kênh (vd: "seller-orders", "customer-notifications")
     * @param subscriberId ID người đăng ký (thường là userId dưới dạng String)
     * @return SseEmitter đã được cấu hình timeout và listener
     */
    SseEmitter subscribe(String channel, String subscriberId);

    /**
     * Gửi event tới một subscriber cụ thể trong channel.
     *
     * @param channel      Tên kênh
     * @param subscriberId ID người nhận
     * @param eventName    Tên event (vd: "new-order", "order-updated")
     * @param data         Dữ liệu gửi kèm (có thể null nếu chỉ cần trigger tín hiệu)
     */
    void sendEvent(String channel, String subscriberId, String eventName, Object data);

    /**
     * Gửi event tới tất cả các subscriber thuộc cùng một channel (Broadcast).
     * Hữu ích cho các thông báo mang tính hệ thống toàn kênh.
     *
     * @param channel   Tên kênh
     * @param eventName Tên event
     * @param data      Dữ liệu gửi kèm (nullable)
     */
    void broadcastToChannel(String channel, String eventName, Object data);
}
