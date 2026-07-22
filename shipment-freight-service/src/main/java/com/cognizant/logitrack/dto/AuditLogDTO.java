package com.cognizant.logitrack.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private Integer auditId;
    private Integer userId;
    private String action;
    private String entityType;
    private LocalDateTime timestamp;
}
