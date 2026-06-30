package com.be.controller;

import com.be.utils.VNPayUtil;
import com.be.service.customer.CustomerOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vnpayment")
@Slf4j
public class VNPayController {

    private final CustomerOrderService customerOrderService;

    @org.springframework.beans.factory.annotation.Value("${app.frontend-url}")
    private String frontendUrl;

    @GetMapping("/return")
    public void vnpayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            if (!fieldName.startsWith("vnp_")) continue;
            String rawValue = request.getParameter(fieldName);
            if (rawValue != null && !rawValue.isEmpty()) {
                // Theo chuẩn chính thức VNPAY Java: encode cả fieldName và fieldValue trước khi hash
                String encodedName = java.net.URLEncoder.encode(fieldName, java.nio.charset.StandardCharsets.US_ASCII);
                String encodedValue = java.net.URLEncoder.encode(rawValue, java.nio.charset.StandardCharsets.US_ASCII);
                fields.put(encodedName, encodedValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        // Xóa theo key đã encode
        fields.remove(java.net.URLEncoder.encode("vnp_SecureHash", java.nio.charset.StandardCharsets.US_ASCII));
        fields.remove(java.net.URLEncoder.encode("vnp_SecureHashType", java.nio.charset.StandardCharsets.US_ASCII));

        log.info("[VNPay Return] Received callback, TxnRef={}, ResponseCode={}",
                request.getParameter("vnp_TxnRef"),
                request.getParameter("vnp_ResponseCode"));

        // 1. Verify signature
        String signValue = VNPayUtil.hashAllFields(fields);
        boolean isSignatureValid = signValue.equalsIgnoreCase(vnp_SecureHash);

        String redirectUrl = frontendUrl + "/orders";

        if (!isSignatureValid) {
            log.error("[VNPay Return] Chữ ký không hợp lệ! Received: {}, Computed: {}", vnp_SecureHash, signValue);
            redirectUrl += "?paymentStatus=error&message=InvalidSignature";
            response.sendRedirect(redirectUrl);
            return;
        }

        String paymentRef = request.getParameter("vnp_TxnRef");
        String responseCode = request.getParameter("vnp_ResponseCode");
        String amountStr = request.getParameter("vnp_Amount");

        if ("00".equals(responseCode)) {
            try {
                customerOrderService.processVNPayPayment(paymentRef, responseCode, amountStr);
                log.info("[VNPay Return] Cập nhật thanh toán thành công cho paymentRef={}", paymentRef);
                redirectUrl += "?paymentStatus=success";
            } catch (IllegalStateException e) {
                if ("AlreadyConfirmed".equals(e.getMessage())) {
                    log.info("[VNPay Return] Giao dịch đã được xác nhận trước đó cho paymentRef={}", paymentRef);
                    redirectUrl += "?paymentStatus=success";
                } else if ("OrderCancelled".equals(e.getMessage())) {
                    log.warn("[VNPay Return] Giao dịch thất bại vì đơn hàng đã bị hủy cho paymentRef={}", paymentRef);
                    redirectUrl += "?paymentStatus=error&message=OrderCancelled";
                } else {
                    log.error("[VNPay Return] Lỗi nghiệp vụ: {}", e.getMessage());
                    redirectUrl += "?paymentStatus=error&message=" + e.getMessage();
                }
            } catch (IllegalArgumentException e) {
                log.error("[VNPay Return] Sai tham số: {}", e.getMessage());
                redirectUrl += "?paymentStatus=error&message=" + e.getMessage();
            } catch (Exception e) {
                log.error("[VNPay Return] Lỗi hệ thống: {}", e.getMessage());
                redirectUrl += "?paymentStatus=error&message=SystemError";
            }
        } else {
            log.warn("[VNPay Return] Giao dịch thất bại tại cổng VNPay, vnp_ResponseCode={}", responseCode);
            redirectUrl += "?paymentStatus=failed&responseCode=" + responseCode;
        }

        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/ipn")
    public Map<String, String> vnpayIpn(HttpServletRequest request) {
        Map<String, String> result = new HashMap<>();
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            if (!fieldName.startsWith("vnp_")) continue;
            String rawValue = request.getParameter(fieldName);
            if (rawValue != null && !rawValue.isEmpty()) {
                // Theo chuẩn chính thức VNPAY Java: encode cả fieldName và fieldValue trước khi hash
                String encodedName = java.net.URLEncoder.encode(fieldName, java.nio.charset.StandardCharsets.US_ASCII);
                String encodedValue = java.net.URLEncoder.encode(rawValue, java.nio.charset.StandardCharsets.US_ASCII);
                fields.put(encodedName, encodedValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        // Xóa theo key đã encode
        fields.remove(java.net.URLEncoder.encode("vnp_SecureHash", java.nio.charset.StandardCharsets.US_ASCII));
        fields.remove(java.net.URLEncoder.encode("vnp_SecureHashType", java.nio.charset.StandardCharsets.US_ASCII));

        // 1. Verify signature
        String signValue = VNPayUtil.hashAllFields(fields);
        boolean isSignatureValid = signValue.equalsIgnoreCase(vnp_SecureHash);

        if (!isSignatureValid) {
            log.error("[VNPay IPN] Chữ ký không hợp lệ!");
            result.put("RspCode", "97");
            result.put("Message", "Invalid signature");
            return result;
        }

        String paymentRef = request.getParameter("vnp_TxnRef");
        String responseCode = request.getParameter("vnp_ResponseCode");
        String amountStr = request.getParameter("vnp_Amount");

        try {
            customerOrderService.processVNPayPayment(paymentRef, responseCode, amountStr);
            log.info("[VNPay IPN] Confirm success cho paymentRef={}", paymentRef);
            result.put("RspCode", "00");
            result.put("Message", "Confirm success");
        } catch (IllegalStateException e) {
            if ("AlreadyConfirmed".equals(e.getMessage())) {
                log.info("[VNPay IPN] Giao dịch đã xác nhận trước đó cho paymentRef={}", paymentRef);
                result.put("RspCode", "02");
                result.put("Message", "Order already confirmed");
            } else if ("OrderCancelled".equals(e.getMessage())) {
                log.info("[VNPay IPN] Đơn hàng đã bị hủy trước đó cho paymentRef={}", paymentRef);
                result.put("RspCode", "02");
                result.put("Message", "Order already cancelled");
            } else {
                log.error("[VNPay IPN] Lỗi nghiệp vụ: {}", e.getMessage());
                result.put("RspCode", "99");
                result.put("Message", "Input data required");
            }
        } catch (IllegalArgumentException e) {
            String errorMsg = e.getMessage();
            if ("OrderNotFound".equals(errorMsg)) {
                log.warn("[VNPay IPN] Không tìm thấy đơn hàng cho paymentRef={}", paymentRef);
                result.put("RspCode", "01");
                result.put("Message", "Order not found");
            } else if ("AmountMismatch".equals(errorMsg)) {
                log.warn("[VNPay IPN] Số tiền không khớp cho paymentRef={}", paymentRef);
                result.put("RspCode", "04");
                result.put("Message", "Invalid Amount");
            } else {
                log.error("[VNPay IPN] Lỗi tham số: {}", errorMsg);
                result.put("RspCode", "99");
                result.put("Message", "Input data required");
            }
        } catch (Exception e) {
            log.error("[VNPay IPN] Lỗi không xác định: {}", e.getMessage());
            result.put("RspCode", "99");
            result.put("Message", "Unknown error");
        }

        return result;
    }
}