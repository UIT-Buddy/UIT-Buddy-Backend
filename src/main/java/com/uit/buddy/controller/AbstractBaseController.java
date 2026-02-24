package com.uit.buddy.controller;

import com.uit.buddy.dto.base.SingleResponse;
import com.uit.buddy.dto.base.SuccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

public abstract class AbstractBaseController {

    @Autowired
    protected ResponseFactory responseFactory;

    protected <T> ResponseEntity<SingleResponse<T>> successSingle(T data, String message) {
        return responseFactory.successSingle(data, message);
    }

    protected ResponseEntity<SuccessResponse> success(String message) {
        return responseFactory.success(message);
    }
}
