package com.be.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class ImageUploadExecutorConfig {

    @Value("${executor.image-upload.core-pool-size:4}")
    private int corePoolSize;

    @Value("${executor.image-upload.max-pool-size:8}")
    private int maxPoolSize;

    @Value("${executor.image-upload.queue-capacity:100}")
    private int queueCapacity;

    @Value("${executor.image-upload.keep-alive-seconds:60}")
    private long keepAliveSeconds;

    @Bean(name = "imageUploadExecutorService", destroyMethod = "shutdown")
    public ExecutorService imageUploadExecutorService() {
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                imageUploadThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    private ThreadFactory imageUploadThreadFactory() {
        AtomicInteger counter = new AtomicInteger(1);
        return runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("image-upload-" + counter.getAndIncrement());
            thread.setDaemon(false);
            return thread;
        };
    }
}
