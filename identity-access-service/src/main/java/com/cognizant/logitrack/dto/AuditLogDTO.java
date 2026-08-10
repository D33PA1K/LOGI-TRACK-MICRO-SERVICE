package com.cognizant.logitrack.dto;

import com.cognizant.logitrack.enums.Role;
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

    /** The actor: who performed the action. */
    private Integer userId;

    // Denormalised actor details so the audit table is readable without the
    // client having to resolve each userId against /api/users.
    private String userName;
    private String userEmail;
    private Role userRole;

    private String action;
    private String entityType;

    /** The entity the action was performed on. */
    private Integer entityId;

    private LocalDateTime timestamp;
}
