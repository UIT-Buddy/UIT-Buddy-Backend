package com.uit.buddy.dto.base;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@JsonPropertyOrder({ "statusCode", "message" })
public class SuccessResponse extends AbstractBaseResponse {

    @Schema(name = "message", type = "String", description = "Response message field", example = "Success!")
    private String message;
}
