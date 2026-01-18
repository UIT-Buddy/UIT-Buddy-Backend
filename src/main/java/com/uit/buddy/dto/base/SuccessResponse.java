package com.uit.buddy.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class SuccessResponse extends AbstractBaseResponse {

    @Schema(name = "message", type = "String", description = "Response message field", example = "Success!")
    private String message;
}
