package com.cognizant.logitrack.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "logistics_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reportId;
    @Column(length = 50)
    private String scope;

    // The period the report covers, persisted so a stored report is
    // self-describing. An SLA report you cannot reproduce is worthless, and
    // without these columns "last Tuesday's report" has no defined meaning.
    private LocalDate fromDate;
    private LocalDate toDate;

    @Column(columnDefinition = "TEXT")
    private String metrics;

    @CreationTimestamp
    private LocalDateTime generatedDate;
}
