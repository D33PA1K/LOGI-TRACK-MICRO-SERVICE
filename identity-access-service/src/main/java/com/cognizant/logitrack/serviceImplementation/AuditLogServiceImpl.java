package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.service.AuditLogService;
import com.cognizant.logitrack.dto.AuditLogDTO;
import com.cognizant.logitrack.entity.AuditLog;
import com.cognizant.logitrack.entity.User;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.repository.AuditLogRepository;
import com.cognizant.logitrack.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service

@Slf4j
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AuditLogDTO logAction(Integer userId, String action, String entityType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        AuditLog auditLog = AuditLog.builder().user(user).action(action).entityType(entityType).build();
        AuditLog saved = auditLogRepository.save(auditLog);
        log.debug("Audit log recorded: userId={}, action={}, entityType={}", userId, action, entityType);
        return toDTO(saved);
    }

    @Override
    public Page<AuditLogDTO> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(this::toDTO);
    }

    @Override
    public List<AuditLogDTO> getByUserId(Integer userId) {
        return auditLogRepository.findByUserId(userId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<AuditLogDTO> getByAction(String action) {
        return auditLogRepository.findByAction(action).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<AuditLogDTO> getByDateRange(LocalDateTime from, LocalDateTime to) {
        return auditLogRepository.findByTimestampBetween(from, to).stream().map(this::toDTO).collect(Collectors.toList());
    }

    private AuditLogDTO toDTO(AuditLog auditLog) {
        Integer userId = auditLog.getUser() != null ? auditLog.getUser().getUserId() : null;
        return AuditLogDTO.builder().auditId(auditLog.getAuditId()).userId(userId).action(auditLog.getAction()).entityType(auditLog.getEntityType()).timestamp(auditLog.getTimestamp()).build();
    }
}


