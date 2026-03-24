package com.uit.buddy.exception.schedule;

import com.uit.buddy.exception.BaseException;
import lombok.Getter;

@Getter
public class ScheduleException extends BaseException {

    public ScheduleException(ScheduleErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage(), errorCode.getHttpStatus());
    }

    public ScheduleException(ScheduleErrorCode errorCode, String customMessage) {
        super(errorCode.getCode(), customMessage, errorCode.getHttpStatus());
    }
}
