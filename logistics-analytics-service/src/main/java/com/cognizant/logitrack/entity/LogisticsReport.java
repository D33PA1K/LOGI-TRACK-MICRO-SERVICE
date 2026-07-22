package com.cognizant.logitrack.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
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
    @Column(columnDefinition = "TEXT")
    private String metrics;
    @CreationTimestamp
    private LocalDateTime generatedDate;
}
