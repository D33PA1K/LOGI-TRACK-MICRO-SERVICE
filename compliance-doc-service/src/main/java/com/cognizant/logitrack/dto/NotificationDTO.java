package com.cognizant.logitrack.dto;

import com.cognizant.logitrack.enums.NotificationCategory;
import com.cognizant.logitrack.enums.NotificationStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Integer notificationId;
    @NotNull
    private Integer userId;
    private String message;
    @NotNull
    private NotificationCategory category;
    private NotificationStatus status;
    private LocalDateTime createdDate;
}
