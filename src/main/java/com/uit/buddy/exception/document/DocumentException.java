package com.uit.buddy.exception.document;

import com.uit.buddy.exception.BaseException;
import lombok.Getter;

@Getter
public class DocumentException extends BaseException {
    public DocumentException(DocumentErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage(), errorCode.getHttpStatus());
    }

    public DocumentException(DocumentErrorCode errorCode, String customMessage) {
        super(errorCode.getCode(), customMessage, errorCode.getHttpStatus());
    }
}
