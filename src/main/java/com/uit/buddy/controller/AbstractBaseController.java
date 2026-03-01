package com.uit.buddy.controller;

import com.uit.buddy.dto.base.PageResponse;
import com.uit.buddy.dto.base.SingleResponse;
import com.uit.buddy.dto.base.SuccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
    protected <T> ResponseEntity<PageResponse<T>> paging(Page<T> page, String message)
    {
        PageResponse<T> response = responseFactory.createPageResponse(
                HttpStatus.OK,
                message,
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
        return ResponseEntity.ok(response);
    }

    protected Pageable createPageable(int page, int limit, String sortType, String sortBy)
    {
        int offset = (page - 1) * limit;
        int pageNumber = offset / limit;
        if (sortBy == null) {
            return PageRequest.of(pageNumber, limit);
        }
        Sort.Direction direction = (sortType != null && sortType.equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = !sortBy.isEmpty() ? sortBy : "id";
        Sort sort = Sort.by(direction, sortField);
        return PageRequest.of(pageNumber, limit, sort);
    }
}
