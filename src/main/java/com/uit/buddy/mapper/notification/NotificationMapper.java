package com.uit.buddy.mapper.notification;

import com.uit.buddy.dto.response.notification.NotificationResponse;
import com.uit.buddy.entity.notification.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationResponse toResponse(Notification notification);
}
