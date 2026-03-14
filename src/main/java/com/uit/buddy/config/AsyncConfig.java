package com.uit.buddy.config;

import com.uit.buddy.constant.AsyncConstants;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {
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
}
