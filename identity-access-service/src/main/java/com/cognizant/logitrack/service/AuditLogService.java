package com.cognizant.logitrack.service;

import com.cognizant.logitrack.dto.AuditLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogService {
    AuditLogDTO logAction(Integer userId, String action, String entityType);
    Page<AuditLogDTO> getAllLogs(Pageable pageable);
    List<AuditLogDTO> getByUserId(Integer userId);
    List<AuditLogDTO> getByAction(String action);
    List<AuditLogDTO> getByDateRange(LocalDateTime from, LocalDateTime to);
}
