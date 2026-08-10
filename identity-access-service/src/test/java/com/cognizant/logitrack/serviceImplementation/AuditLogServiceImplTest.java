package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.dto.AuditLogDTO;
import com.cognizant.logitrack.entity.AuditLog;
import com.cognizant.logitrack.entity.User;
import com.cognizant.logitrack.enums.Role;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.repository.AuditLogRepository;
import com.cognizant.logitrack.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    private static User user(Integer id, String name, Role role) {
        return User.builder()
                .userId(id)
                .name(name)
                .email(name.toLowerCase() + "@logitrack.com")
                .role(role)
                .build();
    }

    @Test
    @DisplayName("the ACTOR is recorded as the audit user, with the affected user as the entity")
    void recordsActorNotTarget() {
        User admin = user(1, "Admin", Role.ADMIN);
        when(userRepository.findById(1)).thenReturn(Optional.of(admin));
        when(auditLogRepository.save(any(AuditLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        auditLogService.logAction(1, "USER_DEACTIVATED", "User", 42);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();

        // This is the whole point: "who did this" must be the admin, not the victim.
        assertEquals(1, saved.getUser().getUserId());
        assertEquals(42, saved.getEntityId());
        assertEquals("USER_DEACTIVATED", saved.getAction());
        assertEquals("User", saved.getEntityType());
    }

    @Test
    @DisplayName("the 3-arg overload treats the actor as its own subject (e.g. LOGIN)")
    void threeArgOverloadUsesActorAsEntity() {
        User shipper = user(7, "Shipper", Role.SHIPPER);
        when(userRepository.findById(7)).thenReturn(Optional.of(shipper));
        when(auditLogRepository.save(any(AuditLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        auditLogService.logAction(7, "LOGIN", "User");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        assertEquals(7, captor.getValue().getUser().getUserId());
        assertEquals(7, captor.getValue().getEntityId());
    }

    @Test
    @DisplayName("the DTO carries the actor's name, email and role so the trail is readable")
    void dtoDenormalisesActorDetails() {
        User admin = user(1, "Admin", Role.ADMIN);
        when(userRepository.findById(1)).thenReturn(Optional.of(admin));
        when(auditLogRepository.save(any(AuditLog.class)))
                .thenAnswer(invocation -> {
                    AuditLog log = invocation.getArgument(0);
                    log.setAuditId(99);
                    return log;
                });

        AuditLogDTO dto = auditLogService.logAction(1, "USER_CREATED", "User", 5);

        assertEquals(99, dto.getAuditId());
        assertEquals(1, dto.getUserId());
        assertEquals("Admin", dto.getUserName());
        assertEquals("admin@logitrack.com", dto.getUserEmail());
        assertEquals(Role.ADMIN, dto.getUserRole());
        assertEquals(5, dto.getEntityId());
    }

    @Test
    @DisplayName("an unknown actor is rejected rather than writing an orphan audit row")
    void unknownActorIsRejected() {
        when(userRepository.findById(404)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> auditLogService.logAction(404, "LOGIN", "User"));

        verify(auditLogRepository, never()).save(any());
    }
}
