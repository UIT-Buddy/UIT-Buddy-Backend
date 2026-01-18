package com.uit.buddy.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class ErrorResponse extends AbstractBaseResponse {

    @Schema(name = "statusCode", description = "HTTP status code", type = "Integer", example = "400")
    private int statusCode;

    @Schema(name = "message", description = "Error message", type = "String", example = "Bad Request")
    private String message;
}
