package com.uit.buddy.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Executor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = AsyncConfig.class)
@TestPropertySource(properties = { "thread.core-thread-pool-size=5", "thread.max-thread-pool-size=10",
        "thread.thread-queue-capacity=100" })
@DisplayName("AsyncConfig Tests")
class AsyncConfigTest {

    @Autowired
    @Qualifier("uploadExecutor")
    private Executor uploadExecutor;

    @Test
    @DisplayName("Should create uploadExecutor bean successfully")
    void shouldCreateUploadExecutorBean() {
        // Then
        assertThat(uploadExecutor).isNotNull();
        assertThat(uploadExecutor).isInstanceOf(ThreadPoolTaskExecutor.class);
    }

    @Test
    @DisplayName("Should configure thread pool with correct properties")
    void shouldConfigureThreadPoolWithCorrectProperties() {
        // Given
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) uploadExecutor;

        // Then
        assertThat(executor.getCorePoolSize()).isEqualTo(5);
        assertThat(executor.getMaxPoolSize()).isEqualTo(10);
        assertThat(executor.getThreadNamePrefix()).isEqualTo("Buddy-Media-");
    }
}
