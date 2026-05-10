package com.uit.buddy.config;

/**
 * ThreadLocal context to identify if the current thread is executing a background scheduler task. Used by
 * MoodleRateLimiter to apply separate concurrency limits for the scheduler.
 */
public class MoodleRateLimiterContext {
    private static final ThreadLocal<Boolean> IS_SCHEDULER = ThreadLocal.withInitial(() -> false);

    public static void setScheduler(boolean isScheduler) {
        IS_SCHEDULER.set(isScheduler);
    }

    public static boolean isScheduler() {
        return IS_SCHEDULER.get();
    }

    public static void clear() {
        IS_SCHEDULER.remove();
    }
}
