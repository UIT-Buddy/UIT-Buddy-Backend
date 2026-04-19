package com.uit.buddy.exception.grade;

import com.uit.buddy.exception.BaseException;
import lombok.Getter;

@Getter
public class GradeException extends BaseException {

    public GradeException(GradeErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage(), errorCode.getHttpStatus());
    }

    public GradeException(GradeErrorCode errorCode, String customMessage) {
        super(errorCode.getCode(), customMessage, errorCode.getHttpStatus());
    }
}
