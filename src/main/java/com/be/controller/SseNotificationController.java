package com.be.controller;

import com.be.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
@Slf4j
public class SseNotificationController {

    private final SseEmitterService sseEmitterService;

    /**
     * Endpoint đăng ký nhận Server-Sent Events (SSE).
     *
     * URL ví dụ: GET /api/sse/subscribe?channel=seller-orders&subscriberId=1
     * Header trả về luôn có 'Content-Type: text/event-stream'
     *
     * @param channel      Tên kênh đăng ký (vd: "seller-orders", "customer-notifications")
     * @param subscriberId ID người dùng đăng ký nhận tin (thường là userId)
     * @return SseEmitter kết nối dài hạn tới client
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @RequestParam String channel,
            @RequestParam String subscriberId
    ) {
        log.info("[SSE] Incoming subscription request for channel '{}' from subscriberId '{}'", channel, subscriberId);
        return sseEmitterService.subscribe(channel, subscriberId);
    }
}
