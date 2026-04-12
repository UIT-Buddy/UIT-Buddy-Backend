package com.uit.buddy.dto.response.document;

import com.uit.buddy.dto.response.auth.StudentResponse;
import com.uit.buddy.enums.AccessRole;
import java.time.LocalDateTime;

public record SharedUserResponse(StudentResponse student, AccessRole accessRole, LocalDateTime sharedAt) {
}
