package com.uit.buddy.dto.response.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EnrolledCourseResponse(@JsonProperty("shortname") String shortName,
        @JsonProperty("fullname") String fullName, @JsonProperty("idnumber") String idNumber,
        @JsonProperty("id") String id, @JsonProperty("startdate") String startDate) {
}
