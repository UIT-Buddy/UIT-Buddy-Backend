package com.uit.buddy.constant;

public final class RedisConstants {

  private RedisConstants() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  // Key patterns
  public static final String REFRESH_TOKEN_KEY_PATTERN = "refresh_token:*";
  public static final String PASSWORD_RESET_OTP_KEY_PATTERN = "password_reset_otp:*";
  public static final String PENDING_ACCOUNT_KEY_PATTERN = "pending_account:*";

  // Token types
  public static final String TOKEN_TYPE_ACCESS = "ACCESS_TOKEN";
  public static final String TOKEN_TYPE_REFRESH = "REFRESH_TOKEN";
}
