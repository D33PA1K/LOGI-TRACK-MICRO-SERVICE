package com.cognizant.logitrack.repository;

import com.cognizant.logitrack.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Extends JpaSpecificationExecutor so the audit search can combine optional
 * filters (actor, action, date range) without hand-writing one JPQL query per
 * combination — and without the untyped-null pitfalls of ":param IS NULL" JPQL.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Integer>, JpaSpecificationExecutor<AuditLog> {

    @Query("SELECT a FROM AuditLog a WHERE a.user.userId = :userId")
    List<AuditLog> findByUserId(@Param("userId") Integer userId);

    List<AuditLog> findByAction(String action);

    List<AuditLog> findByTimestampBetween(LocalDateTime from, LocalDateTime to);

    Page<AuditLog> findAll(Pageable pageable);

    /** Distinct action names, used to populate the audit filter dropdown. */
    @Query("SELECT DISTINCT a.action FROM AuditLog a ORDER BY a.action")
    List<String> findDistinctActions();
}
