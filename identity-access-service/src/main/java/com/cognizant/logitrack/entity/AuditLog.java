package com.cognizant.logitrack.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer auditId;

    /**
     * The user who PERFORMED the action (the actor), resolved from the caller's
     * JWT — not the user the action was performed on. An audit trail that records
     * only the affected row cannot answer "who did this?", which is the whole
     * point of having one.
     */
    // Left EAGER (the ManyToOne default) on purpose: every audit row is rendered
    // with the actor's name/email/role, so lazy loading would only add an N+1.
    @ManyToOne
    @JoinColumn(name = "UserID")
    private User user;

    @Column(length = 50)
    private String action;

    private String entityType;

    /** Id of the entity the action was performed on (e.g. the user being deactivated). */
    private Integer entityId;

    @CreationTimestamp
    private LocalDateTime timestamp;
}
