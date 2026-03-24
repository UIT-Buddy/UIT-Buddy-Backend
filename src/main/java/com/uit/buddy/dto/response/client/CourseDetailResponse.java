package com.uit.buddy.dto.response.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record CourseDetailResponse(@JsonProperty("id") String id, @JsonProperty("name") String name,
        @JsonProperty("modules") List<CourseDetailModuleResponse> moduleResponse) {
    public record CourseDetailModuleResponse(@JsonProperty("instance") String id, @JsonProperty("url") String url,
            @JsonProperty("name") String name, @JsonProperty("dates") List<CourseDetailModuleDatesResonponse> dates) {
        public record CourseDetailModuleDatesResonponse(@JsonProperty("label") String label,
                @JsonProperty("timestamp") String timestamp) {
        }
    }
}
