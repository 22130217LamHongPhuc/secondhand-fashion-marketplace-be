package com.be.constant;

public enum PromotionStatus {
    DRAFT,      // Bản nháp, chưa công khai
    ACTIVE,     // Đang hoạt động (người dùng có thể thấy và claim/áp dụng nếu trong hạn)
    PAUSED,     // Tạm dừng (chủ shop chủ động dừng chiến dịch trước hạn)
    EXPIRED     // Đã hết hạn (hệ thống tự động nhận diện hoặc cập nhật)
}
