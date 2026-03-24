package com.uit.buddy.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingResponse {
    private boolean enableNotification;
    private boolean enableScheduleReminder;
}
