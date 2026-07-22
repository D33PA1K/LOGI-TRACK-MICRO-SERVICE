package com.cognizant.logitrack.entity;
 
import com.cognizant.logitrack.enums.FlagSeverity;
import com.cognizant.logitrack.enums.FlagStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Entity
@Table(name = "compliance_flags")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceFlag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer flagId;
    
    private Integer shipmentId;
    
    @Column(length = 100)
    private String flagType;
    @Enumerated(EnumType.STRING)
    private FlagSeverity severity;
    @CreationTimestamp
    private LocalDate raisedDate;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FlagStatus status = FlagStatus.OPEN;
}
