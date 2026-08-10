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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        return logAction(userId, action, entityType, userId);
    }

    @Override
    public AuditLogDTO logAction(Integer actorUserId, String action, String entityType, Integer entityId) {
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + actorUserId));
        AuditLog auditLog = AuditLog.builder()
                .user(actor)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .build();
        AuditLog saved = auditLogRepository.save(auditLog);
        log.debug("Audit log recorded: actorUserId={}, action={}, entityType={}, entityId={}",
                actorUserId, action, entityType, entityId);
        return toDTO(saved);
    }

    /**
     * Optional filters are composed as a Specification, so an absent filter
     * simply contributes no predicate. Newest entries first — an audit trail is
     * almost always read from the most recent event backwards.
     */
    @Override
    public Page<AuditLogDTO> search(Integer userId, String action, LocalDateTime from, LocalDateTime to,
                                    Pageable pageable) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("userId"), userId));
            }
            if (action != null && !action.isBlank()) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), to));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return auditLogRepository.findAll(spec, pageable).map(this::toDTO);
    }

    @Override
    public List<String> getDistinctActions() {
        return auditLogRepository.findDistinctActions();
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
        return auditLogRepository.findByTimestampBetween(from, to).stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    private AuditLogDTO toDTO(AuditLog auditLog) {
        User actor = auditLog.getUser();
        return AuditLogDTO.builder()
                .auditId(auditLog.getAuditId())
                .userId(actor != null ? actor.getUserId() : null)
                .userName(actor != null ? actor.getName() : null)
                .userEmail(actor != null ? actor.getEmail() : null)
                .userRole(actor != null ? actor.getRole() : null)
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .timestamp(auditLog.getTimestamp())
                .build();
    }
}
