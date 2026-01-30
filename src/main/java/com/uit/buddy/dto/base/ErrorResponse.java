package com.uit.buddy.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "statusCode", "message", "errorCode" })
@NoArgsConstructor
public class ErrorResponse extends AbstractBaseResponse {

    private String errorCode;
}
