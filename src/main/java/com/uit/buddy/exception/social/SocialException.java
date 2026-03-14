package com.uit.buddy.exception.social;

import com.uit.buddy.exception.BaseException;
import lombok.Getter;

@Getter
public class SocialException extends BaseException {

  public SocialException(SocialErrorCode errorCode) {
    super(errorCode.getCode(), errorCode.getMessage(), errorCode.getHttpStatus());
  }

  public SocialException(SocialErrorCode errorCode, String customMessage) {
    super(errorCode.getCode(), customMessage, errorCode.getHttpStatus());
  }
}
