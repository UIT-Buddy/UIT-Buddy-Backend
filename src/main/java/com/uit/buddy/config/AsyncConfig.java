package com.uit.buddy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {
    @Value("${thread.core-thread-pool-size}")
    private int corePoolSize;
    @Value("${thread.max-thread-pool-size}")
    private int maxPoolSize;
    @Value("${thread.thread-queue-capacity}")
    private int queueCapacity;
    @Value("${thread.thread-name-prefix}")
    private String threadNamePrefix;
    @Bean(name = "uploadExecutor")
    public Executor taskExecutor()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();
        return executor;
    }
}
