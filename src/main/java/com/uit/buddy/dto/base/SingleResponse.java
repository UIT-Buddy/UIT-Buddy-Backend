package com.uit.buddy.dto.base;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@JsonPropertyOrder({ "statusCode", "message", "data" })
@NoArgsConstructor
@AllArgsConstructor
public class SingleResponse<T> extends AbstractBaseResponse {
    private T data;
}