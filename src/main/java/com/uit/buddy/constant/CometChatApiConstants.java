package com.uit.buddy.constant;

public final class CometChatApiConstants {

    private CometChatApiConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // API Endpoints
    public static final String USERS_ENDPOINT = "/users";
    public static final String USER_BY_UID_ENDPOINT = "/users/%s";
    public static final String ADD_FRIENDS_ENDPOINT = "/users/%s/friends";
    public static final String REMOVE_FRIEND_ENDPOINT = "/users/%s/friends/%s";
    public static final String AUTH_TOKEN_ENDPOINT = "/users/%s/auth_tokens";
    public static final String UPDATE_USER_ENDPOINT = "/users/%s";

    // Header Keys
    public static final String API_KEY_HEADER = "apiKey";
    public static final String APP_ID_HEADER = "appId";
}
