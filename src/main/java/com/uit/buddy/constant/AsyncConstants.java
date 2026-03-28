package com.uit.buddy.constant;

public final class AsyncConstants {

    private AsyncConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String MEDIA_THREAD_PREFIX = "Buddy-Media-";
    public static final String COMET_CHAT_THREAD_PREFIX = "Buddy-CometChat-";
    public static final String NOTIFICATION_THREAD_PREFIX = "Buddy-Notification-";
    public static final String FETCH_THREAD_PREFIX = "Buddy-Fetch-";
}
