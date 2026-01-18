package com.uit.buddy.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DetailedErrorResponse extends ErrorResponse {
    @Schema(name = "items", description = "Error message", type = "Map", nullable = true, example = "{\"foo\": \"Bar\"}")
    private Map<String, String> items;
}
