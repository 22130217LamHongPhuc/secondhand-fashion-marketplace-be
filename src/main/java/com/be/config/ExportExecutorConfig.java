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
public class ExportExecutorConfig {

    @Value("${executor.export.core-pool-size:2}")
    private int corePoolSize;

    @Value("${executor.export.max-pool-size:4}")
    private int maxPoolSize;

    @Value("${executor.export.queue-capacity:10}")
    private int queueCapacity;

    @Value("${executor.export.keep-alive-seconds:60}")
    private long keepAliveSeconds;

    @Bean(name = "exportExecutorService", destroyMethod = "shutdown")
    public ExecutorService exportExecutorService() {
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                exportThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    private ThreadFactory exportThreadFactory() {
        AtomicInteger counter = new AtomicInteger(1);
        return runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("export-worker-" + counter.getAndIncrement());
            thread.setDaemon(false);
            return thread;
        };
    }
}
