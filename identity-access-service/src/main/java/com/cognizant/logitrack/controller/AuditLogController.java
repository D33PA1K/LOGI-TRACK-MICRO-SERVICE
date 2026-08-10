package com.cognizant.logitrack.controller;

import com.cognizant.logitrack.dto.AuditLogDTO;
import com.cognizant.logitrack.service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Read-only view over the audit trail. Restricted to ANALYST and ADMIN by
 * SecurityConfig — there is deliberately no write endpoint, because audit rows
 * are only ever produced as a side effect of a real action inside this service.
 */
@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private static final int MAX_PAGE_SIZE = 200;

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * Filters are all optional and combine with AND. Dates are accepted as plain
     * calendar dates and widened to cover the whole day, so "from 2026-08-08 to
     * 2026-08-08" returns that day's activity rather than nothing.
     */
    @GetMapping
    public ResponseEntity<Page<AuditLogDTO>> search(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        LocalDateTime from = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime to = toDate != null ? toDate.atTime(LocalTime.MAX) : null;

        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);

        Pageable pageable = PageRequest.of(safePage, safeSize,
                Sort.by(Sort.Direction.DESC, "timestamp"));

        return ResponseEntity.ok(auditLogService.search(userId, action, from, to, pageable));
    }

    /** Distinct action names present in the trail, for the UI filter dropdown. */
    @GetMapping("/actions")
    public ResponseEntity<List<String>> getActions() {
        return ResponseEntity.ok(auditLogService.getDistinctActions());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLogDTO>> getByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(auditLogService.getByUserId(userId));
    }
}
