package com.be.controller;

import com.be.common.enums.PaymentStatus;
import com.be.entity.Order;
import com.be.repository.OrderRepository;
import com.be.utils.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vnpayment")
@Slf4j
public class VNPayController {

    private final OrderRepository orderRepository;

    @org.springframework.beans.factory.annotation.Value("${app.frontend-url}")
    private String frontendUrl;

    @GetMapping("/return")
    public void vnpayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        // Verify signature
        String signValue = VNPayUtil.hashAllFields(fields);
        boolean isSignatureValid = signValue.equalsIgnoreCase(vnp_SecureHash);

        String redirectUrl = frontendUrl + "/orders";
        if (isSignatureValid) {
            String responseCode = fields.get("vnp_ResponseCode");
            if ("00".equals(responseCode)) {
                String paymentRef = fields.get("vnp_TxnRef");
                List<Order> orders = orderRepository.findByPaymentRef(paymentRef);
                if (!orders.isEmpty()) {
                    LocalDateTime now = LocalDateTime.now();
                    for (Order order : orders) {
                        order.setPaymentStatus(PaymentStatus.PAID);
                        order.setPaidAt(now);
                        orderRepository.save(order);
                    }
                    log.info("Thanh toán thành công qua VNPay cho paymentRef: {}", paymentRef);
                    redirectUrl += "?paymentStatus=success";
                } else {
                    log.warn("Không tìm thấy đơn hàng tương ứng với paymentRef: {}", paymentRef);
                    redirectUrl += "?paymentStatus=error&message=OrderNotFound";
                }
            } else {
                log.warn("Giao dịch VNPay thất bại, responseCode: {}", responseCode);
                redirectUrl += "?paymentStatus=failed&responseCode=" + responseCode;
            }
        } else {
            log.error("Chữ ký VNPay không hợp lệ");
            redirectUrl += "?paymentStatus=error&message=InvalidSignature";
        }

        response.sendRedirect(redirectUrl);
    }
}
