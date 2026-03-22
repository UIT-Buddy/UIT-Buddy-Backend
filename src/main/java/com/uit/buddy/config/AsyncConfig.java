package com.uit.buddy.config;

import com.uit.buddy.constant.AsyncConstants;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@Slf4j
public class AsyncConfig implements AsyncConfigurer {
    @Value("${thread.core-thread-pool-size}")
    private int corePoolSize;

    @Value("${thread.max-thread-pool-size}")
    private int maxPoolSize;

    @Value("${thread.thread-queue-capacity}")
    private int queueCapacity;

    @Bean(name = "uploadExecutor")
    public Executor uploadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(AsyncConstants.MEDIA_THREAD_PREFIX);
        executor.initialize();
        return executor;
    }

    @Bean(name = "cometChatExecutor")
    public Executor cometChatExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(AsyncConstants.COMET_CHAT_THREAD_PREFIX);
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            log.error("================ ASYNC ERROR ================");
            log.error("Exception Message: {}", ex.getMessage());
            log.error("Method Name: {}", method.getName());
            for (Object param : params) {
                log.error("Parameter value: {}", param);
            }
            log.error("=============================================");
        };
    }
}
