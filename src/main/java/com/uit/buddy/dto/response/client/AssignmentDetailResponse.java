package com.uit.buddy.dto.response.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AssignmentDetailResponse(@JsonProperty("lastattempt") AssignmentSubmissionResponse lastAttempt) {
    public record AssignmentSubmissionResponse(@JsonProperty("submission") SubmissionDetailResponse submission) {
        public record SubmissionDetailResponse(@JsonProperty("status") String status) {
        }
    }
}
