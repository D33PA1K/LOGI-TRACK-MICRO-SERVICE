package com.cognizant.logitrack.service;

import com.cognizant.logitrack.dto.NotificationDTO;
import java.util.List;

public interface NotificationService {
    NotificationDTO sendNotification(NotificationDTO dto);
    List<NotificationDTO> getByUser(Integer userId);
    List<NotificationDTO> getUnreadByUser(Integer userId);
    NotificationDTO markAsRead(Integer id);
    void markAllAsRead(Integer userId);
    long getUnreadCount(Integer userId);
}
