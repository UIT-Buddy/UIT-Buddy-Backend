package com.uit.buddy.dto.response.social;

import com.uit.buddy.enums.FileType;

public record MediaResponse(FileType type, String url) {
}
