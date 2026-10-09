package com.optiflow.crm.notification.mapper;

import com.optiflow.crm.notification.dto.NotificationDto;
import com.optiflow.crm.notification.model.Notification;

import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationDto toDto(Notification notification) {

        NotificationDto dto = new NotificationDto();

        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setFollowUpId(notification.getFollowUpId());
        dto.setQuoteId(notification.getQuoteId());
        dto.setRead(notification.getRead());
        dto.setReadAt(notification.getReadAt());
        dto.setCreatedAt(notification.getCreatedAt());

        return dto;
    }
}