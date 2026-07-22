package com.cognizant.logitrack.repository;

import com.cognizant.logitrack.entity.Notification;
import com.cognizant.logitrack.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserId(Integer userId);
    List<Notification> findByUserIdAndStatus(Integer userId, NotificationStatus status);
    long countByUserIdAndStatus(Integer userId, NotificationStatus status);
}
