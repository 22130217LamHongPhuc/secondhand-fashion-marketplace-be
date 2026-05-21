package com.be.service.impl;

import com.be.service.SseEmitterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SseEmitterServiceImpl implements SseEmitterService {

    // Cấu hình Timeout: 30 phút (1.800.000 milliseconds)
    private static final Long TIMEOUT = 1800000L;

    // Quản lý các emitter theo composite key: "{channel}::{subscriberId}"
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribe(String channel, String subscriberId) {
        String key = buildKey(channel, subscriberId);

        // Đóng connection cũ nếu có để giữ tính độc quyền (1 connection duy nhất per tab chính/subscriber)
        SseEmitter oldEmitter = emitters.remove(key);
        if (oldEmitter != null) {
            try {
                log.info("[SSE] Closing existing connection for key: {}", key);
                oldEmitter.complete();
            } catch (Exception e) {
                log.warn("[SSE] Error closing old emitter for key: {}", key, e);
            }
        }

        // Tạo connection mới
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitters.put(key, emitter);

        // Thiết lập các lifecycle callbacks
        emitter.onCompletion(() -> {
            log.info("[SSE] Connection completed for key: {}", key);
            emitters.remove(key, emitter);
        });

        emitter.onTimeout(() -> {
            log.warn("[SSE] Connection timeout for key: {}", key);
            emitters.remove(key, emitter);
        });

        emitter.onError((ex) -> {
            log.error("[SSE] Connection error for key: {}, error: {}", key, ex.getMessage());
            emitters.remove(key, emitter);
        });

        // Gửi event khởi tạo ngay lập tức để xác nhận kết nối thành công với frontend
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("SSE connection established successfully on channel: " + channel));
            log.info("[SSE] Subscriber registered successfully for key: {}", key);
        } catch (IOException e) {
            log.error("[SSE] Failed to send initial connection event for key: {}", key, e);
            emitter.completeWithError(e);
            emitters.remove(key, emitter);
        }

        return emitter;
    }

    @Override
    public void sendEvent(String channel, String subscriberId, String eventName, Object data) {
        String key = buildKey(channel, subscriberId);
        SseEmitter emitter = emitters.get(key);

        if (emitter != null) {
            try {
                SseEmitter.SseEventBuilder eventBuilder = SseEmitter.event().name(eventName);
                if (data != null) {
                    eventBuilder.data(data);
                } else {
                    eventBuilder.data("");
                }
                emitter.send(eventBuilder);
                log.info("[SSE] Sent event '{}' to key: {}", eventName, key);
            } catch (IOException e) {
                log.warn("[SSE] Failed to send event '{}' to key: {}. Removing dead connection.", eventName, key);
                emitter.completeWithError(e);
                emitters.remove(key, emitter);
            }
        } else {
            log.debug("[SSE] No active subscriber for key: {}. Event '{}' was not sent.", key, eventName);
        }
    }

    @Override
    public void broadcastToChannel(String channel, String eventName, Object data) {
        String prefix = channel + "::";
        log.info("[SSE] Broadcasting event '{}' to channel: {}", eventName, channel);

        emitters.forEach((key, emitter) -> {
            if (key.startsWith(prefix)) {
                try {
                    SseEmitter.SseEventBuilder eventBuilder = SseEmitter.event().name(eventName);
                    if (data != null) {
                        eventBuilder.data(data);
                    } else {
                        eventBuilder.data("");
                    }
                    emitter.send(eventBuilder);
                } catch (IOException e) {
                    log.warn("[SSE] Failed to broadcast event '{}' to key: {}. Removing dead connection.", eventName, key);
                    emitter.completeWithError(e);
                    emitters.remove(key, emitter);
                }
            }
        });
    }

    /**
     * Tạo composite key cho map lưu trữ.
     */
    private String buildKey(String channel, String subscriberId) {
        return String.format("%s::%s", channel, subscriberId);
    }
}
