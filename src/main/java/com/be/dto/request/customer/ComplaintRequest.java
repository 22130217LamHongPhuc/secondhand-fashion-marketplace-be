package com.be.dto.request.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintRequest {

    @NotNull(message = "Mã đơn hàng không được để trống")
    private Long orderId;

    @NotBlank(message = "Tiêu đề khiếu nại không được để trống")
    private String title;

    @NotBlank(message = "Nội dung khiếu nại không được để trống")
    private String content;
}
