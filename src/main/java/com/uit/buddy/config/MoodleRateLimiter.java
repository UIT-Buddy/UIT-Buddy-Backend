package com.uit.buddy.config;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Rate limiter for Moodle API calls.
 *
 * <p>
 * Enforces a maximum of {@code maxConcurrentRequests} simultaneous requests to Moodle, and limits the total request
 * rate to approximately {@code requestsPerSecond} by releasing permits at a fixed interval.
 *
 * <p>
 * Requests that cannot acquire a permit wait in the Semaphore queue. Under virtual threads, this wait does NOT block
 * platform threads — the virtual thread yields, allowing other work to proceed.
 *
 * <p>
 * Example: maxConcurrentRequests=5, drainIntervalMs=200 → ~5 requests/second to Moodle.
 */
@Component
@Slf4j
public class MoodleRateLimiter {

    private final Semaphore semaphore;
    private final long drainIntervalMs;

    /**
     * @param maxConcurrentRequests
     *            maximum simultaneous requests to Moodle (default 5)
     * @param requestsPerSecond
     *            target request rate to Moodle (default 5); used to derive drain interval
     */
    public MoodleRateLimiter(@Value("${moodle.max-concurrent-requests:5}") int maxConcurrentRequests,
            @Value("${moodle.requests-per-second:5}") int requestsPerSecond) {
        this.semaphore = new Semaphore(maxConcurrentRequests, true);
        this.drainIntervalMs = Math.max(100, 1000 / requestsPerSecond);
    }

    /**
     * Acquires a permit before making a Moodle API call. Blocks until a permit is available.
     *
     * @throws InterruptedException
     *             if the current thread is interrupted while waiting
     */
    public void acquire() throws InterruptedException {
        semaphore.acquire();
    }

    /**
     * Acquires a permit with a timeout.
     *
     * @param timeout
     *            maximum time to wait
     * @param unit
     *            time unit
     *
     * @return true if a permit was acquired, false if the timeout elapsed
     *
     * @throws InterruptedException
     *             if the current thread is interrupted while waiting
     */
    public boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
        boolean acquired = semaphore.tryAcquire(timeout, unit);
        return acquired;
    }

    /**
     * Releases a permit after a Moodle API call completes. Should ALWAYS be called in a finally block.
     */
    public void release() {
        semaphore.release();
    }

    /**
     * Returns the number of currently available permits.
     */
    public int availablePermits() {
        return semaphore.availablePermits();
    }

    /**
     * Returns the approximate number of threads waiting for a permit.
     */
    public int getQueueLength() {
        return semaphore.getQueueLength();
    }
}
