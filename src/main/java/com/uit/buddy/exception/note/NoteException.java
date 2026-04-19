package com.uit.buddy.exception.note;

import com.uit.buddy.exception.BaseException;
import lombok.Getter;

@Getter
public class NoteException extends BaseException {

    public NoteException(NoteErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage(), errorCode.getHttpStatus());
    }

    public NoteException(NoteErrorCode errorCode, String customMessage) {
        super(errorCode.getCode(), customMessage, errorCode.getHttpStatus());
    }
}
