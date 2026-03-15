package com.uit.buddy.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "statusCode", "message", "data", "paging" })
public record CursorPageResponse<T>(Integer statusCode, String message, List<T> data, PagingInfo paging) {
    public record PagingInfo(String nextCursor, boolean hasMore, int limit) {
    }
}
