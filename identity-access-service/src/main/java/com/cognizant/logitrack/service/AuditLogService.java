package com.cognizant.logitrack.service;

import com.cognizant.logitrack.dto.AuditLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogService {

    /** Records an action where the actor is also the subject (e.g. LOGIN). */
    AuditLogDTO logAction(Integer userId, String action, String entityType);

    /** Records an action performed BY {@code actorUserId} ON {@code entityId}. */
    AuditLogDTO logAction(Integer actorUserId, String action, String entityType, Integer entityId);

    /** Paginated search with optional filters; any null filter is ignored. */
    Page<AuditLogDTO> search(Integer userId, String action, LocalDateTime from, LocalDateTime to, Pageable pageable);

    List<String> getDistinctActions();

    Page<AuditLogDTO> getAllLogs(Pageable pageable);
    List<AuditLogDTO> getByUserId(Integer userId);
    List<AuditLogDTO> getByAction(String action);
    List<AuditLogDTO> getByDateRange(LocalDateTime from, LocalDateTime to);
}
