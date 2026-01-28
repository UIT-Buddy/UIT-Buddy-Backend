package com.uit.buddy.dto.base;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({ "statusCode", "message", "data", "paging" })
public class PageResponse<T> extends AbstractBaseResponse {
    private List<T> data;
    private PagingResponse paging;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagingResponse {
        private int page;
        private int limit;
        private long total;
        private int totalPages;
    }
}
